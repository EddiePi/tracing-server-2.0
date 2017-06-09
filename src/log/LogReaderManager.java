package log;

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
    TracerConf conf = TracerConf.getInstance();
    File rootDir;
    File[] applicationDirs;
    Boolean isChecking;
    public ConcurrentMap<String, ContainerLogReader> runningContainerMap = new ConcurrentHashMap<>();
    Thread checkingThread;

    public LogReaderManager() {
        rootDir = new File(conf.getStringOrDefault("tracer.log.root", "~/hadoop-2.7.3/logs/userlogs"));
        applicationDirs = rootDir.listFiles();
        if (applicationDirs == null) {
            applicationDirs = new File[0];
        }
        isChecking = true;

        checkingThread = new Thread(new CheckAppDirRunnable());

    }

    private class CheckAppDirRunnable implements Runnable {

        @Override
        public void run() {
            System.out.print("app checking thread started.\n");
            try {
                new WatchDir(rootDir, true, new FileActionCallback() {
                    @Override
                    public void create(File file) {
                        System.out.println("file created\t" + file.getAbsolutePath());
                        String name = file.getName();
                        if(name.contains("container")) {
                            runningContainerMap.put(name, new ContainerLogReader(file.getAbsolutePath()));
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


    //TODO
    public void stop() {

    }

    private void updateLogThreadPool(File appDir) {

    }

}
