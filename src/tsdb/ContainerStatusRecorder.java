package tsdb;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Eddie on 2017/6/25.
 */
public class ContainerStatusRecorder {
    private ConcurrentMap<String, String> statusMap = new ConcurrentHashMap<>();

    private static final ContainerStatusRecorder instance = new ContainerStatusRecorder();

    private ContainerStatusRecorder(){}

    public static ContainerStatusRecorder getInstance() {
        return instance;
    }

    public void putState(String containerId, String state) {
        statusMap.put(containerId, state);
    }

    public String getState(String containerId) {
        String res = statusMap.getOrDefault(containerId, null);
        if(res.equals("DONE")) {
            statusMap.remove(containerId);
        }
        return res;
    }
}
