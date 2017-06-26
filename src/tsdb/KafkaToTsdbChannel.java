package tsdb;

import Server.TracerConf;
import Utils.HTTPRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 * Created by Eddie on 2017/6/22.
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
    ContainerStatusRecorder containerStatusRecorder;

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
        containerStatusRecorder = ContainerStatusRecorder.getInstance();

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
                    if (key.contains("nodemanager")) {
                        nodemanagerLogParser(value);
                    } else if (key.contains("metric")) {
                        hasMessage = metricTransformer(value);
                    }
                    if (hasMessage) {
                        try {
                            String message = builder.build(true);

                            // TODO: maintain the connection for performance
                            String response = HTTPRequest.sendPost(databaseURI, message);
                            if(response != null || !response.equals("")) {
                                System.out.printf("Unexpected response: %s\n" , response);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
                try {
                    Thread.sleep(500);
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
        try {
            String containerId = metrics[0];
            Long timestamp = Timestamp.valueOf(metrics[1]).getTime();
            Double cpuUsage = Double.valueOf(metrics[2]);
            Long memoryUsage = Long.valueOf(metrics[3]);
            Double diskRate = Double.valueOf(metrics[4]) + Double.valueOf(metrics[5]);
            Double netRate = Double.valueOf(metrics[6]) + Double.valueOf(metrics[7]);
            String containerState = containerStatusRecorder.getState(containerId);
            if (containerState == null) {
                containerState = "NEW";
            }

            boolean hasMessage = false;
            // cpu
            builder.addMetric("cpu")
                    .setDataPoint(timestamp, cpuUsage)
                    .addTag("container", containerId)
                    .addTag("state", containerState);

            // memory
            builder.addMetric("memory")
                    .setDataPoint(timestamp, memoryUsage)
                    .addTag("container", containerId)
                    .addTag("state", containerState);

            // disk
            builder.addMetric("disk").
                    setDataPoint(timestamp, diskRate).
                    addTag("container", containerId)
                    .addTag("state", containerState);

            // network
            builder.addMetric("network")
                    .setDataPoint(timestamp, netRate)
                    .addTag("container", containerId)
                    .addTag("state", containerState);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    private void nodemanagerLogParser(String logStr) {
        if(!logStr.matches(".*Container.*transitioned from.*")) {
            return;
        }
        String[] words = logStr.split("\\s+");
        String nextState = words[words.length - 1];
        String containerId = words[words.length - 6];
        containerStatusRecorder.putState(containerId, nextState);
        return;
    }

}