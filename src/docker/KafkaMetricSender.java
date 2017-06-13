package docker;

import MetricsSender.MetricsSender;
import Server.TracerConf;
import info.ContainerMetrics;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Created by Eddie on 2017/6/13.
 */
public class KafkaMetricSender {
    Properties props;
    Producer<String, String> producer;
    TracerConf conf;
    String kafkaTopic;

    public KafkaMetricSender() {
        conf = TracerConf.getInstance();
        props = new Properties();
        props.put("acks", "0");
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        String servers = conf.getStringOrDefault("tracer.kafka.bootstrap.servers", "localhost:9092");
        props.put("bootstrap.servers", servers);
        kafkaTopic = "trace";
        producer = new KafkaProducer<>(props);
    }

    public void send(DockerMetrics dm) {
        producer.send(new ProducerRecord<String, String>(kafkaTopic, dm.containerId, buildMetricString(dm)));
    }

    private String buildMetricString(DockerMetrics dm) {
        String res;
        res = dm.containerId + "," +
                dm.timestamp.toString() + "," +
                dm.cpuRate.toString() + "," +
                dm.memoryUsage.toString() + "," +
                dm.diskReadRate.toString() + "," +
                dm.diskWriteRate.toString() + "," +
                dm.netRecRate.toString() + "," +
                dm.netRecRate.toString();
        return res;
    }

    public void close() {
        producer.close();
    }
}
