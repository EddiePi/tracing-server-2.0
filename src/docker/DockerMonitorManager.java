package docker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Eddie on 2017/6/11.
 */
public class DockerMonitorManager {
    // TODO: use thread pool
    public ConcurrentMap<String, DockerMonitor> containerIdToDM = new ConcurrentHashMap<>();

    public DockerMonitorManager () {}

    public void addDockerMonitor(String containerId) {
        if (!containerIdToDM.containsKey(containerId)) {
            DockerMonitor dockerMonitor = new DockerMonitor(containerId);
            dockerMonitor.start();
            containerIdToDM.put(containerId, dockerMonitor);
        }
    }

    public void removeDockerMonitor(String containerId) {
        DockerMonitor dockerMonitorToRemove =
                containerIdToDM.remove(containerId);
        if(dockerMonitorToRemove != null) {
            dockerMonitorToRemove.stop();
        }
    }
}
