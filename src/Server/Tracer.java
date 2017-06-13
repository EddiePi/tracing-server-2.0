package Server;

import docker.DockerMonitorManager;
import log.LogReaderManager;

/**
 * Created by Eddie on 2017/2/21.
 */
public class Tracer {
    private LogReaderManager logReaderManager;

    private DockerMonitorManager dockerMonitorManager;
    private boolean isTest = false;

    private class TracingRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    System.out.printf("number of LogReader: %d; number of DockerMonitor: %d\n",
                            logReaderManager.runningContainerMap.size(), dockerMonitorManager.containerIdToDM.size());
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    // do nothing
                }
            }
        }
    }
    TracingRunnable runnable = new TracingRunnable();

    Thread tThread = new Thread(runnable);

    private static final Tracer instance = new Tracer();
    private Tracer() {}

    public static Tracer getInstance() {
        return instance;
    }

    public void init() {
        logReaderManager = new LogReaderManager();
        dockerMonitorManager = new DockerMonitorManager();
        logReaderManager.start();
        tThread.start();
    }

    /**
     * This method is called by <code>LogReaderManager</code>.
     * Used to create a new <code>DockerMonitor</code>.
     *
     * @param containerId
     */
    public void addContainerMonitor(String containerId) {
        dockerMonitorManager.addDockerMonitor(containerId);
    }

    /**
     * This method is called by <code>DockerMonitorManager</code>.
     * Used to stop a <code>LogReaderManager</code>
     *
     * @param containerId
     */
    public void removeContainerLogReader(String containerId) {
        logReaderManager.stopContainerLogReaderById(containerId);
    }
}
