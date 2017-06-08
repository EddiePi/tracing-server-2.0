package log;

import Server.TracerConf;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Eddie on 2017/6/6.
 */
public class AppLogReader {

    TracerConf conf = TracerConf.getInstance();
    int timeoutThreshold = conf.getIntegerOrDefault("tracer.log.timeout", 5);
    File appDir;
    public Boolean isChecking;
    File[] containerDirs;
    List<String> doneContainerList = new LinkedList<>();
    Map<String, ContainerLogReader> runningContainerCount = new HashMap<>();
    Thread checkingThread;
    int timeoutCount = 0;

    public AppLogReader(String appPath) {
        isChecking = true;
        appDir = new File(appPath);
        containerDirs = appDir.listFiles();
        checkingThread = new Thread(new CheckContainerDirRunnable());
        checkingThread.start();
    }

    private class CheckContainerDirRunnable implements Runnable {

        private File[] newDirs;
        int newContainerNum;

        @Override
        public void run() {
            newDirs = appDir.listFiles();
            System.out.print("init container:\n");
            for(File newDir: newDirs) {
                if (!newDir.isDirectory() || !newDir.getAbsolutePath().contains("container")) {
                    continue;
                }
                try {
                    String path = newDir.getCanonicalPath();
                    // new containers have been scheduled.
                    if (!doneContainerList.contains(path) && !runningContainerCount.containsKey(path)) {
                        System.out.print("new container detected: " + path + "\n");
                        runningContainerCount.put(path, new ContainerLogReader(path));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            while(isChecking) {
                //System.out.print("checking app dir.\n");
                newDirs = appDir.listFiles();
                if(newDirs == null) {
                    continue;
                }
                newContainerNum = newDirs.length - containerDirs.length;
                if (newContainerNum > 0) {

                    for (File newDir : newDirs) {
                        if (!newDir.isDirectory() || !newDir.getAbsolutePath().contains("container")) {
                            continue;
                        }
                        try {
                            String path = newDir.getCanonicalPath();
                            // new containers have been scheduled.
                            if (!doneContainerList.contains(path) && !runningContainerCount.containsKey(path)) {
                                System.out.print("new container detected: " + path + "\n");
                                runningContainerCount.put(path, new ContainerLogReader(path));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (runningContainerCount.isEmpty()) {
                    timeoutCount++;
                    if (timeoutCount >= timeoutThreshold) {
                        System.out.print("stopping app: " + appDir.getAbsolutePath() + "\n");
                        isChecking = false;
                        break;
                    }
                } else {
                    timeoutCount = 0;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.print("app: " + appDir.getAbsolutePath() + " is stopped\n");
        }
    }

    public void stopContainerLogReaderById(String containerId) {
        try {
            String appPath = appDir.getCanonicalPath();
            if(appPath.charAt(appPath.length() - 1) != '/') {
                appPath += "/";
            }
            String containerPath = appPath + containerId;
            stopContainerLogReaderByPath(containerPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopContainerLogReaderByPath(String containerPath) {
        ContainerLogReader reader = runningContainerCount.remove(containerPath);
        if (reader != null) {
            reader.stop();
            doneContainerList.add(containerPath);
        }
    }

    public void stop() {
        if(runningContainerCount.isEmpty()) {
            isChecking = false;
        }
    }
}
