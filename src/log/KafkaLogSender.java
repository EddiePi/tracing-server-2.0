package log;

import Server.TracerConf;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Created by Eddie on 2017/6/8.
 */
public class KafkaLogSender {
    Properties props;
    Producer<String, String> producer;
    TracerConf conf;
    String kafkaTopic;
    String containerId;

    public KafkaLogSender(String containerId) {
        conf = TracerConf.getInstance();
        props = new Properties();
        props.put("acks", "0");
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        String servers = conf.getStringOrDefault("tracer.kafka.bootstrap.servers", "localhost:9092");
        props.put("bootstrap.servers", servers);
        kafkaTopic = "log";
        producer = new KafkaProducer<>(props);

        this.containerId = containerId;

    }

    public void send(String message) {
        producer.send(new ProducerRecord<String, String>(kafkaTopic, containerId, message));
    }

    public void close() {
        producer.close();
    }
}
