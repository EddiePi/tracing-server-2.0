package info;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Eddie on 2017/1/25.
 */
public class TracingInfo {

    private static TracingInfo instance = null;

    private TracingInfo() {
        apps = new HashSet<>();
    }

    public static TracingInfo getTracingInfo() {
        if (instance == null) {
            instance = new TracingInfo();
        }
        return instance;
    }

    public Set<App> apps;
}
