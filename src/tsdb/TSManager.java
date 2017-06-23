package tsdb;

/**
 * Created by Eddie on 2017/6/22.
 */
public class TSManager {
    MetricKafkaChannel metricKafkaChannel;
    public TSManager() {
        metricKafkaChannel = new MetricKafkaChannel();
    }

    public void start() {
        metricKafkaChannel.start();
    }

    public void stop() {
        metricKafkaChannel.stop();
    }
}
