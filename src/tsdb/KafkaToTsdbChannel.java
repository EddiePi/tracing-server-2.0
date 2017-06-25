package tsdb;

import Server.TracerConf;
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
                    if (key.contains("metric")) {
                        hasMessage = metricTransformer(value);
                    } else if (key.contains("nodemanager")) {
                        hasMessage = nodemanagerLogTransformer(value);
                    }
                    if (hasMessage) {
                        try {
                            System.out.println();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.printf("offset = %d, key = %s, value = %s\n", record.offset(), record.key(), record.value());

                }
                consumer.close(5, TimeUnit.SECONDS);
            }
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
        String containerId = metrics[0];
        Long timestamp = Timestamp.valueOf(metrics[1]).getTime();
        Double cpuUsage = Double.valueOf(metrics[2]);
        Long memoryUsage = Long.valueOf(metrics[3]);
        Double diskRate = Double.valueOf(metrics[4]) + Double.valueOf(metrics[5]);
        Double netRate = Double.valueOf(metrics[6]) + Double.valueOf(metrics[7]);

        boolean hasMessage = false;
        // cpu
        builder.addMetric("cpu").setDataPoint(timestamp, cpuUsage).addTag("container", containerId);

        // memory
        builder.addMetric("memory").setDataPoint(timestamp, memoryUsage).addTag("container", containerId);

        // disk
        builder.addMetric("disk").setDataPoint(timestamp, diskRate).addTag("container", containerId);

        // network
        builder.addMetric("network").setDataPoint(timestamp, netRate).addTag("container", containerId);

        return hasMessage;
    }


    private boolean nodemanagerLogTransformer(String logStr) {
        return false;
    }

}
