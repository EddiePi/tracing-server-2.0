package log;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Eddie on 2017/6/6.
 */
public class ContainerLogReader {

    File containerDir;
    File[] logFiles;
    List<String> doneFileList = new LinkedList<>();
    Map<String, Integer> readingFileCount = new HashMap<>();
    List<FileReadRunnable> fileReadRunnableList = new ArrayList<>();
    ExecutorService fileReadingThreadPool = Executors.newCachedThreadPool();
    volatile Boolean isChecking = true;
    String containerId;
    Thread checkingThread;
    ContainerStateRecorder recorder = ContainerStateRecorder.getInstance();

    public int timeoutCount;

    public ContainerLogReader(String containerPath) {
        this.containerDir = new File(containerPath);
        timeoutCount = 0;

        containerId = parseContainerId(containerPath);
        checkingThread = new Thread(new ContainerCheckingRunnable());
        checkingThread.start();
    }

    private class ContainerCheckingRunnable implements Runnable {

        @Override
        public void run() {
            while (isChecking) {
                logFiles = containerDir.listFiles();
                for(int i = 0; i < logFiles.length; i++) {
                    try {
                        String filePath = logFiles[i].getCanonicalPath();
                        if(!doneFileList.contains(filePath)
                                && !readingFileCount.containsKey(filePath)) {
                            readingFileCount.put(filePath, 0);
                            FileReadRunnable newFile = new FileReadRunnable(filePath);
                            fileReadRunnableList.add(newFile);
                            fileReadingThreadPool.execute(newFile);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class FileReadRunnable implements Runnable {

        String filePath;
        //List<String> messageBuffer = new ArrayList<>();
        List<String> messageBuffer = new ArrayList<>();
        BufferedReader bufferedReader = null;
        Boolean isReading = true;
        KafkaLogSender logSender = new KafkaLogSender(containerId);

        public FileReadRunnable(String path) {
            this.filePath = path;
        }

        @Override
        public void run() {
            try {
                if(bufferedReader == null) {
                    InputStream is = new FileInputStream(filePath);
                    Reader reader = new InputStreamReader(is, "GBK");
                    bufferedReader = new BufferedReader(reader);
                }
                while (isReading) {
                    String line;
                    messageBuffer.clear();
                    while((line = bufferedReader.readLine()) != null) {
                        recordContainerState(line);
                        messageBuffer.add(line);
                    }
                    for(String message: messageBuffer) {
                        // TEST
                        // System.out.printf("%s\n", message);
                        logSender.send(message);
                    }
                    Thread.sleep(1000);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                Thread.currentThread().interrupt();
            }
        }


        public void destroy() throws IOException {
            isReading = false;
            logSender.close();
            bufferedReader.close();
        }
    }

    private String parseContainerId(String containerPath) {
        String[] paths = containerPath.split("/");
        return paths[paths.length - 1];
    }

    private void recordContainerState(String logStr) {
        System.out.printf("raw message: %s\n", logStr);
        if(!logStr.matches(".*Container.*transitioned from.*")) {
            return;
        }
        String[] words = logStr.split("\\s+");
        Long timestamp = Timestamp.valueOf(words[0] + " " + words[1]).getTime();
        String nextState = words[words.length - 1];
        String containerId = words[words.length - 6];
        System.out.printf("filtered message: %s\ncontainerId: %s, state: %s\n", logStr, containerId, nextState);
        recorder.putState(containerId, nextState, timestamp);
    }

    /**
     * This method will stop all the log readers in this container directory
     */
    public void stop() {
        isChecking = false;
        //checkingThread.interrupt();
        try {
            for (FileReadRunnable runnable : fileReadRunnableList) {
                runnable.destroy();
            }
            fileReadingThreadPool.awaitTermination(2, TimeUnit.SECONDS);
            System.out.print("all log readers of container: " + containerDir.getAbsolutePath() + " are stopped.\n");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
