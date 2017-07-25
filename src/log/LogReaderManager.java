package log;

import Server.Tracer;
import Server.TracerConf;
import Utils.FileWatcher.FileActionCallback;
import Utils.FileWatcher.WatchDir;
import logAPI.*;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
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
    Tracer tracer = Tracer.getInstance();
    // Key is the container's id.
    public ConcurrentMap<String, ContainerLogReader> runningContainerMap = new ConcurrentHashMap<>();
    NodeManagerLogReaderRunnable nodeManagerReaderRunnable;
    CheckAppDirRunnable checkingRunnable;
    Thread checkingThread;
    Thread nodeManagerReadThread;
    ContainerStateRecorder recorder = ContainerStateRecorder.getInstance();
    LogAPICollector apiCollector;
    boolean customAPIEnabled;

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
        if(!nodeManagerLog.exists()) {
            nodeManagerLog = new File(nodeManagerDir + "/hadoop-" + username + "-nodemanager-" + hostname + ".log");
        }

        nodeManagerReaderRunnable = new NodeManagerLogReaderRunnable();
        checkingRunnable = new CheckAppDirRunnable();
        nodeManagerReadThread = new Thread(nodeManagerReaderRunnable);
        checkingThread = new Thread(checkingRunnable);

        apiCollector = LogAPICollector.getInstance();
        if (conf.getBooleanOrDefault("tracer.is-master", false)) {
            registerDefaultAPI();
            customAPIEnabled = conf.getBooleanOrDefault("tracer.log.custom-api.enabled", false);
            if (customAPIEnabled) {
                registerCustomAPI();
            }
        }
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
                    Thread.sleep(20);

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

                            // we only update the api if there is no running app.
                            if(runningContainerMap.size() == 0) {
                                apiCollector.clearAllAPI();
                                registerDefaultAPI();
                                if (customAPIEnabled) {
                                    registerCustomAPI();
                                }
                            }
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

    /**
     * @deprecated this method is deprecated. we should do this function on master node
     * @param logStr
     */
    @Deprecated
    private void recordContainerState(String logStr) {
        if(logStr.matches(".*Start request for container.*")) {

            // here we notify the docker monitor to start.
            String[] words = logStr.split("\\s+");
            Long timestamp = parseTimestamp(logStr);
            String firstState = "NEW";
            String containerId = words[words.length - 4];
            System.out.printf("filtered message: %s\ncontainerId: %s, state: %s\n", logStr, containerId, firstState);
            recorder.putState(containerId, firstState, timestamp);

            // start docker monitor
            tracer.addContainerMonitor(containerId);
        } else if(logStr.matches(".*Container.*transitioned from.*")) {
            Long timestamp = parseTimestamp(logStr);
            String[] words = logStr.split("\\s+");
            String nextState = words[words.length - 1];
            String containerId = words[words.length - 6];
            System.out.printf("filtered message: %s\ncontainerId: %s, state: %s\n", logStr, containerId, nextState);
            recorder.putState(containerId, nextState, timestamp);
        }
    }


    // we need to re-register the API after detecting new apps.
    // In this design, we don't have to restart the tracing server to import changed api file
    private void registerDefaultAPI() {
        List<AbstractLogAPI> defaultAPIList = new ArrayList<>();
        defaultAPIList.add(new SparkLogAPI());
        defaultAPIList.add(new MapReduceLogAPI());

        for(AbstractLogAPI api: defaultAPIList) {
            apiCollector.register(api);
        }
    }

    private void registerCustomAPI() {
        String[] filePaths = conf.getStringOrDefault("tracer.log.custom-api.path", "../conf/custom.api").split(",");
        for(String filePath: filePaths) {
            File apiFile = new File(filePath);
            if(apiFile.exists()) {
                apiCollector.register(new CustomLogAPI(apiFile));
            }
        }
    }

    public static Long parseTimestamp(String logMessage) {
        String[] words = logMessage.split("\\s+");
        Long timestamp = Timestamp.valueOf(words[0] + " " + words[1].replace(',', '.')).getTime();
        return timestamp;
    }
}
