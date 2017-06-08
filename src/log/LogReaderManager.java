package log;

import Server.TracerConf;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
    public ConcurrentMap<String, AppLogReader> runningAppsTimeOutCount = new ConcurrentHashMap<>();
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

        File[] newDirs;
        int newAppNum;

        @Override
        public void run() {
            System.out.print("app checking thread started.\n");
            while (isChecking) {
                //check new app
                newDirs = rootDir.listFiles();
                if (newDirs == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                newAppNum = newDirs.length - applicationDirs.length;
                if (newAppNum > 0) {
                    System.out.print("new app detected\n");
                    int currentAppNum = newDirs.length;
                    // new apps have been scheduled.
                    for (int i = 0; i < currentAppNum; i++) {
                        try {
                            String path = newDirs[i].getCanonicalPath();
                            if (newDirs[i].isDirectory() && !runningAppsTimeOutCount.containsKey(path)) {
                                runningAppsTimeOutCount.put(path, new AppLogReader(path));
                                System.out.print("new app added: " + path);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                applicationDirs = newDirs;

                //check stopped app
                for(Map.Entry<String, AppLogReader> entry: runningAppsTimeOutCount.entrySet()) {
                    String key = entry.getKey();
                    AppLogReader value = entry.getValue();
                    if (value.isChecking == false) {
                        runningAppsTimeOutCount.remove(key);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
