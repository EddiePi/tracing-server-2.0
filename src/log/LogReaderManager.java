package log;

import Server.Tracer;
import Server.TracerConf;
import Utils.FileWatcher.FileActionCallback;
import Utils.FileWatcher.WatchDir;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Eddie on 2017/6/6.
 */
public class LogReaderManager {
    TracerConf conf;
    File rootDir;
    File[] applicationDirs;
    volatile Boolean isChecking;
    // Key is the container's id.
    public ConcurrentMap<String, ContainerLogReader> runningContainerMap = new ConcurrentHashMap<>();
    Thread checkingThread;

    public LogReaderManager() {
        conf = TracerConf.getInstance();
        //tracer = Tracer.getInstance();
        rootDir = new File(conf.getStringOrDefault("tracer.log.root", "~/hadoop-2.7.3/logs/userlogs"));
        applicationDirs = rootDir.listFiles();
        if (applicationDirs == null) {
            applicationDirs = new File[0];
        }
        isChecking = true;

        checkingThread = new Thread(new CheckAppDirRunnable());

    }

    private class CheckAppDirRunnable implements Runnable {
        Tracer tracer = Tracer.getInstance();

        @Override
        public void run() {
            System.out.print("app checking thread started.\n");
            try {
                new WatchDir(rootDir, true, new FileActionCallback() {
                    @Override
                    public void create(File file) {
                        System.out.println("file created\t" + file.getAbsolutePath());

                        // The file name is also the containerId.
                        String name = file.getName();
                        if(name.contains("container")) {
                            runningContainerMap.put(name, new ContainerLogReader(file.getAbsolutePath()));
                            tracer.addContainerMonitor(name);
                        }
                    }

                    @Override
                    public void delete(File file) {}

                    @Override
                    public void modify(File file) {}
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        checkingThread.start();
    }

    public void stopContainerLogReaderById(String containerId) {
        ContainerLogReader logReaderToRemove =
                runningContainerMap.remove(containerId);
        if(logReaderToRemove != null) {
            logReaderToRemove.stop();
        }

    }
}
