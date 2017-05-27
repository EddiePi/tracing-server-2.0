package KafkaUtils;

import info.ContainerMetrics;
import org.apache.kafka.common.serialization.*;

import java.util.Map;

/**
 * Created by Eddie on 2017/5/25.
 */
public class MetricEncoder implements Serializer<ContainerMetrics> {

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String s, ContainerMetrics containerMetrics) {
        return new byte[0];
    }

    @Override
    public void close() {

    }
}
