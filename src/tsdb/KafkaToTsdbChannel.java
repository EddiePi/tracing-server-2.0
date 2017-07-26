package tsdb;

import Server.TracerConf;
import Utils.HTTPRequest;
import log.LogReaderManager;
import logAPI.LogAPICollector;
import logAPI.MessageMark;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Eddie on 2017/6/22.
 * This class converts the message format in kafka to the format for TSDB
 */
public class KafkaToTsdbChannel {
    Properties props;
    KafkaConsumer<String, String> consumer;
    List<String> kafkaTopics;
    TracerConf conf = TracerConf.getInstance();
    TransferRunnable transferRunnable;
    Thread transferThread;
    TsdbMetricBuilder builder = TsdbMetricBuilder.getInstance();
    String databaseURI;

    LogAPICollector collector = LogAPICollector.getInstance();

    public KafkaToTsdbChannel() {
        props = new Properties();
        props.put("bootstrap.servers", conf.getStringOrDefault("tracer.kafka.bootstrap.servers", "localhost:9092"));
        props.put("group.id", "trace");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<>(props);
        kafkaTopics = Arrays.asList("trace", "log");
        consumer.subscribe(kafkaTopics);
        databaseURI = conf.getStringOrDefault("tracer.tasb.server", "localhost:4242");
        if(!databaseURI.matches("http://.*")) {
            databaseURI = "http://" + databaseURI;
        }
        if(!databaseURI.matches(".*/api/put")) {
            databaseURI = databaseURI + "/api/put";
        }

        transferRunnable = new TransferRunnable();
        transferThread = new Thread(transferRunnable);
    }

    private class TransferRunnable implements Runnable {
        boolean isRunning = true;

        @Override
        public void run() {
            while (isRunning) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    String key = record.key();
                    String value = record.value();
                    boolean hasMessage = false;
                    if (key.matches("container.*-metric")) {
                        hasMessage = metricTransformer(value);
                    } else if (key.matches("container.*-log")) {
                        hasMessage = logTransformer(value);
                    }
                    if (hasMessage) {
                        try {
                            String message = builder.build(true);

                            // TODO: maintain the connection for performance
                            String response = HTTPRequest.sendPost(databaseURI, message);
                            if(!response.matches("\\s*")) {
                                System.out.printf("Unexpected response: %s\n" , response);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            consumer.close(5, TimeUnit.SECONDS);
        }
    }

    public void start() {
        transferThread.start();
    }

    public void stop() {
        transferRunnable.isRunning = false;
    }

    private boolean metricTransformer(String metricStr) {
        String[] metrics = metricStr.split(",");
        if(metrics.length < 8) {
            return false;
        }
        System.out.printf("metricStr: %s\n", metricStr);
        try {
            Long timestamp = Timestamp.valueOf(metrics[1]).getTime();
            Double cpuUsage = Double.valueOf(metrics[2]);
            Long memoryUsage = Long.valueOf(metrics[3]);
            Double diskRate = Double.valueOf(metrics[4]) + Double.valueOf(metrics[5]);
            Double netRate = Double.valueOf(metrics[6]) + Double.valueOf(metrics[7]);
            Map<String, String> tagMap = buildAllTags(metrics);
            // cpu
            builder.addMetric("cpu")
                    .setDataPoint(timestamp, cpuUsage)
                    .addTags(tagMap);

            // memory
            builder.addMetric("memory")
                    .setDataPoint(timestamp, memoryUsage)
                    .addTags(tagMap);

            // disk
            builder.addMetric("disk")
                    .setDataPoint(timestamp, diskRate)
                    .addTags(tagMap);

            // network
            builder.addMetric("network")
                    .setDataPoint(timestamp, netRate)
                    .addTags(tagMap);

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean logTransformer(String kafkaMessage) {
        List<PackedMessage> packedMessageList;
        packedMessageList = maybePackMessage(kafkaMessage);
        if(packedMessageList.size() == 0) {
            return false;
        }
        for(PackedMessage packedMessage: packedMessageList) {
            String appId = containerIdToAppId(packedMessage.containerId);
            builder.addMetric(packedMessage.name)
                    .setDataPoint(packedMessage.timestamp, packedMessage.doubleValue)
                    .addTags(packedMessage.tagMap)
                    .addTag("container", packedMessage.containerId)
                    .addTag("app", appId);
        }
        return true;
    }

    private List<PackedMessage> maybePackMessage(String kafkaMessage) {
        int separatorIndex = kafkaMessage.indexOf(' ');
        String logMessage = kafkaMessage.substring(separatorIndex).trim();
        String containerId = kafkaMessage.substring(0, separatorIndex).trim();
        List<PackedMessage> packedMessagesList = new ArrayList<>();
        for(MessageMark messageMark: collector.allRuleMarkList) {
            Pattern pattern = Pattern.compile(messageMark.regex);
            Matcher matcher = pattern.matcher(logMessage);
            if(matcher.matches()) {
                System.out.printf("matched log: %s\n", logMessage);
                for(MessageMark.Group group: messageMark.groups) {
                    try {
                        String name = group.name;
                        String valueStr = group.value;
                        Long timestamp = LogReaderManager.parseTimestamp(logMessage) + messageMark.dateOffset;
                        Double value;
                        if (valueStr.matches("^[-+]?[\\d]*(\\.\\d*)?$")) {
                            value = Double.valueOf(valueStr);
                        } else {
                            value = Double.valueOf(matcher.group(valueStr));
                        }
                        Map<String, String> tagMap = new HashMap<>();
                        for(String tagName: group.tags) {
                            String tagValue = matcher.group(tagName);
                            tagMap.put(tagName, tagValue);
                        }
                        PackedMessage packedMessage =
                                new PackedMessage(containerId, timestamp, name, tagMap, value);
                        packedMessagesList.add(packedMessage);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return packedMessagesList;
    }

    private String containerIdToAppId(String containerId) {
        String[] parts = containerId.split("_");
        String appId = "application_" + parts[parts.length - 4] + "_" + parts[parts.length - 3];
        return appId;
    }

    private Map<String, String> buildAllTags(String[] metrics) {
        String containerId = metrics[0];
        String appId = containerIdToAppId(containerId);
        Map<String, String> tagMap = new HashMap<>();
        tagMap.put("app", appId);
        tagMap.put("container", containerId);
        if(metrics.length > 9) {
            for(int i = 9; i < metrics.length; i++) {
                String[] tagNValue = metrics[i].split(":");
                if(tagNValue.length < 2) {
                    continue;
                }
                tagMap.put(tagNValue[0], tagNValue[1]);
            }
        }
        return tagMap;
    }
}
