package tsdb;

import Server.TracerConf;
import Utils.HTTPRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;


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

    public KafkaToTsdbChannel() {
        props = new Properties();
        props.put("bootstrap.servers", conf.getStringOrDefault("tracer.kafka.bootstrap.servers", "localhost:9092"));
        props.put("group.id", "tracer");
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
                    }
                    if (hasMessage) {
                        try {
                            String message = builder.build(true);
                            System.out.printf("tsdb json: %s\n", message);

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
        if(metrics.length < 9) {
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

    private String containerIdToAppId(String containerId) {
        String[] parts = containerId.split("_");
        String appId = "application_" + parts[parts.length - 4] + "_" + parts[parts.length - 3];
        return appId;
    }

    private Map<String, String> buildAllTags(String[] metrics) {
        String containerId = metrics[0];
        String appId = containerIdToAppId(containerId);
        String containerState = metrics[8];
        Map<String, String> tagMap = new HashMap<>();
        tagMap.put("app", appId);
        tagMap.put("container", containerId);
        tagMap.put("stage", containerState);
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
