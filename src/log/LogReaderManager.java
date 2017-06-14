package log;

import Server.Tracer;
import Server.TracerConf;
import Utils.FileWatcher.FileActionCallback;
import Utils.FileWatcher.WatchDir;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Eddie on 2017/6/6.
 */
public class LogReaderManager {
    TracerConf conf;
    File appRootDir;
    File[] applicationDirs;
    File nodeManagerLog;
    // Key is the container's id.
    public ConcurrentMap<String, ContainerLogReader> runningContainerMap = new ConcurrentHashMap<>();
    NodeManagerLogReaderRunnable nodeManagerReaderRunnable;
    CheckAppDirRunnable checkingRunnable;
    Thread checkingThread;
    Thread nodeManagerReadThread;

    public LogReaderManager() {
        conf = TracerConf.getInstance();
        appRootDir = new File(conf.getStringOrDefault("tracer.log.app.root", "~/hadoop-2.7.3/logs/userlogs"));
        applicationDirs = appRootDir.listFiles();
        if (applicationDirs == null) {
            applicationDirs = new File[0];
        }
        String username = System.getProperty("user.name");
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String nodeManagerDir = conf.getStringOrDefault("tracer.log.nodemanager.root", "~/hadoop-2.7.3/logs");
        nodeManagerLog = new File(nodeManagerDir + "/yarn-" + username + "-nodemanager-" + hostname + ".log");

        nodeManagerReaderRunnable = new NodeManagerLogReaderRunnable();
        checkingRunnable = new CheckAppDirRunnable();
        nodeManagerReadThread = new Thread(nodeManagerReaderRunnable);
        checkingThread = new Thread(checkingRunnable);
    }

    private class NodeManagerLogReaderRunnable implements Runnable {
        KafkaLogSender logSender = new KafkaLogSender("nodemanager");

        boolean isReading = true;
        List<String> messageBuffer = new ArrayList<>();
        BufferedReader bufferedReader = null;
        boolean isNavigating = true;
        @Override
        public void run() {
            while(isReading) {
                try {
                    if (!nodeManagerLog.exists()) {
                        Thread.sleep(1000);
                        isNavigating = false;
                        continue;
                    }
                    String line;
                    messageBuffer.clear();
                    if (bufferedReader == null) {
                        InputStream is = new FileInputStream(nodeManagerLog);
                        Reader reader = new InputStreamReader(is, "GBK");
                        bufferedReader = new BufferedReader(reader);
                    }
                    if(isNavigating) {
                        while(bufferedReader.readLine() != null);
                        isNavigating = false;
                    }
                    while ((line = bufferedReader.readLine()) != null) {
                        messageBuffer.add(line);

                    }
                    for (String message : messageBuffer) {
                        // TEST
                        // System.out.printf("%s\n", message);
                        logSender.send(message);
                    }
                    Thread.sleep(1000);

                } catch (InterruptedException e) {

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void destroy() throws IOException {
            isReading = false;
            logSender.close();
            bufferedReader.close();
        }
    }

    private class CheckAppDirRunnable implements Runnable {
        Tracer tracer = Tracer.getInstance();
        WatchDir watchDir;
        @Override
        public void run() {
            System.out.print("app checking thread started.\n");
            try {
                watchDir = new WatchDir(appRootDir, true, new FileActionCallback() {
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
        nodeManagerReadThread.start();
    }

    public void stopContainerLogReaderById(String containerId) {
        ContainerLogReader logReaderToRemove =
                runningContainerMap.remove(containerId);
        if(logReaderToRemove != null) {
            logReaderToRemove.stop();
        }
    }

    public void stop() throws IOException {
        nodeManagerReaderRunnable.destroy();
        for(ContainerLogReader logReader: runningContainerMap.values()) {
            logReader.stop();
        }
    }
}
