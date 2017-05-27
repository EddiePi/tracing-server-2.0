package MetricsSender;

import Server.TracerConf;
import info.AppMetrics;
import info.ContainerMetrics;
import info.JobMetrics;
import info.StageMetrics;
import org.apache.kafka.clients.producer.*;

import java.util.Properties;

/**
 * Created by Eddie on 2017/5/24.
 * This is a Kafka Producer from Kafka aspect
 */
public class KafkaMetricsSender extends MetricsSender {
    Properties props;
    Producer<String, String> producer;
    TracerConf conf;
    String kafkaTopic;

    public KafkaMetricsSender() {
        conf = TracerConf.getInstance();
        props = new Properties();
        props.put("acks", "0");
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        String servers = conf.getStringOrDefault("tracer.kafka.bootstrap.servers", "localhost:9092");
        props.put("bootstrap.servers", servers);
        kafkaTopic = conf.getStringOrDefault("tracer.kafka.topic", "trace");
        producer = new KafkaProducer<>(props);
    }

    @Override
    public void sendContainerMetrics(ContainerMetrics cm) {
        producer.send(new ProducerRecord<String, String>(kafkaTopic, cm.getFullId(), buildMetricString(cm)));
    }

    private String buildMetricString(ContainerMetrics cm) {
        String res;
        res = cm.getFullId() + "," +
                cm.timestamp.toString() + "," +
                cm.cpuUsage.toString() + "," +
                cm.execMemoryUsage.toString() + "," +
                cm.storeMemoryUsage.toString() + "," +
                cm.diskReadRate.toString() + "," +
                cm.diskWriteRate.toString() + "," +
                cm.netRecRate.toString() + "," +
                cm.netRecRate.toString();
        return res;
    }

    @Override
    public void sendStageMetrics(StageMetrics sm) {

    }

    @Override
    public void sendJobMetrics(JobMetrics jm) {

    }

    @Override
    public void sendAppMetrics(AppMetrics am) {

    }
}
