package tsdb;

import java.io.Serializable;
import java.util.Formatter;
import java.util.Map;

/**
 * Created by Eddie on 2017/7/19.
 */
public class PackedMessage implements Serializable {
    public String containerId;
    public Long timestamp;
    public String name;
    public Map<String, String> tagMap;
    public Double doubleValue;

    public PackedMessage(String containerId,
                         Long timeStamp,
                         String name,
                         Map<String, String> tagMap,
                         Double value) {
        this.containerId = containerId;
        this.timestamp = timeStamp;
        this.name = name;
        this.tagMap = tagMap;
        this.doubleValue = value;
    }

    @Override
    public String toString() {
        Formatter formatter = new Formatter();
        formatter.format("containerId: %s, timestamp: %d, name: %s, tag: %s, value: %f",
                containerId, timestamp, name, tagMap.toString(), doubleValue);
        return formatter.toString();
    }
}
