package JsonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Eddie on 2017/3/30.
 */
public class MetricNames {
    public final static List<String> names = new ArrayList<>(Arrays.asList(
            "CPU",
            "execution-memory",
            "storage-memory",
            "disk-read-rate",
            "disk-write-rate",
            "net-receive-rate",
            "net-transfer-rate"));
}
