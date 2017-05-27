package docker;

import Server.TracerConf;
import Utils.ShellCommandExecutor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Eddie on 2017/2/15.
 */
public class DockerMonitor {
    TracerConf conf = TracerConf.getInstance();
    private String dockerId;
    String containerId;
    List<Integer> taksInContainer = new LinkedList<>();

    // NOTE: type of dockerPid is String, NOT int
    String dockerPid = null;
    private String blkioPath;
    private String netFilePath;
    private MonitorThread monitorThread;

    private String ifaceName;
    // docker taskMetrics
    private List<DockerMetrics> metrics;
    int metricsCount = 0;

    private boolean isRunning;

    public DockerMonitor(String containerId) {
        this.containerId = containerId;
        this.dockerId = runShellCommand("docker inspect --format={{.Id}} " + containerId);
        this.dockerPid = runShellCommand("docker inspect --format={{.State.Pid}} " + containerId).trim();
        this.blkioPath= "/sys/fs/cgroup/blkio/docker/" + dockerId + "/";
        this.netFilePath = "/proc/" + dockerPid + "/net/dev";
        metrics = new ArrayList<>();
        ifaceName  = conf.getStringOrDefault("tracer.docker.iface-name", "eth0");
        monitorThread = new MonitorThread();
    }

    public void start() {
        isRunning = true;
        //monitorThread.start();
    }

    public void stop() {
        try {
            isRunning = false;
            // monitorThread.interrupt();
        }
        catch (Exception e) {
        }
    }

    public String getDockerId() {
        return dockerId;
    }

    public String getDockerPid() {
        return dockerPid;
    }

    public DockerMetrics getLatestDockerMetrics() {
        int index = metrics.size() - 1;
        if (index < 0) {
            return null;
        }
        return metrics.get(index);
    }

    // Run a given shell command. return a string as the result
    private String runShellCommand(String command){

        ShellCommandExecutor shExec = null;
        int count = 1;
        while(count < 110){
            //we try 10 times if fails due to device busy
            try {
                shExec = new ShellCommandExecutor(command);
                shExec.execute();

            } catch (IOException e) {
                count++;
                try {
                    Thread.sleep(100*count);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                continue;

            }
            break;
        }
        return shExec.getOutput().trim();
    }

    private class MonitorThread extends Thread {

        @Override
        public void run(){
            isRunning = true;
            try {
                Thread.sleep(1100);
            } catch (InterruptedException e) {
                // do nothing
            }
            //int count = 3;
            //int index = 0;
            while (isRunning) {
                // monitor the docker info
                updateCgroupValues();
                // printStatus();
                //if we come here it means we need to sleep for 1s
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //do nothing
                }
            }
            isRunning = false;
        }
    }

    public void updateCgroupValues() {
        DockerMetrics m = new DockerMetrics(dockerId, containerId);
        // calculate the disk rate
        calculateCurrentDiskRate(m);

        // calculate the network rate
        calculateCurrentNetRate(m);
        metrics.add(m);
        metricsCount++;

        // TEST
        // printStatus();
    }

//        private void updatePreviousTime() {
//            previousProfileTime = System.currentTimeMillis() / 1000;
//        }

    // calculate the disk I/O rate
    private void calculateCurrentDiskRate(DockerMetrics m) {
        if (!getDiskServicedBytes(m)) {
            return;
        }
        DockerMetrics previousMetrics = metrics.get(metricsCount - 1);
        // init timestamps
        Double deltaTime = (m.timestamp - previousMetrics.timestamp) * 1.0;

        // calculate rate
        Long deltaRead = m.diskReadBytes - previousMetrics.diskReadBytes;
        m.diskReadRate = deltaRead / deltaTime;
        Long deltaWrite = m.diskWriteBytes - previousMetrics.diskWriteBytes;
        m.diskWriteRate = deltaWrite / deltaTime;
        //System.out.print("deltaTime: " + deltaTime + " deltaRead: " + deltaRead + " deltaWrite: " + deltaWrite + "\n");
    }

    // read the disk usages from cgroup files
    // and update the taskMetrics in the monitor.
    // if it is not running or first read, return false.
    private boolean getDiskServicedBytes(DockerMetrics m) {
        if(!isRunning) {
            return false;
        }
        boolean calRate = true;
        if (metricsCount == 0) {
            calRate = false;
        }

        String url = blkioPath + "blkio.throttle.io_service_bytes";
        List<String> readLines = readFileLines(url);
        if (readLines != null) {
            String readStr = readLines.get(0).split(" ")[2];
            m.diskReadBytes = Long.parseLong(readStr);


            String writeStr = readLines.get(1).split(" ")[2];
            m.diskWriteBytes = Long.parseLong(writeStr);
            //System.out.print("diskRead: " + m.diskReadBytes + " diskWrite: " + m.diskWriteBytes + "\n");
        }
        return calRate;
    }
    // calculate the network I/O rate
    private void calculateCurrentNetRate(DockerMetrics m) {
        if(!getNetServicedBytes(m)) {
            return;
        }
        DockerMetrics previousMetrics = metrics.get(metricsCount - 1);
        Double deltaTime = (m.timestamp - previousMetrics.timestamp) * 1.0;

        Long deltaReceive = m.netRecBytes - previousMetrics.netRecBytes;
        m.netRecRate = deltaReceive / deltaTime;
        Long deltaTransmit = m.netTransBytes - previousMetrics.netTransBytes;
        m.netTransRate = deltaTransmit / deltaTime;
        //System.out.print("deltaTime: " + deltaTime + " deltaRec: " + deltaReceive + " deltaTrans: " + deltaTransmit + "\n");
    }

    // read the network usage from 'proc' files
    // and update the taskMetrics in the monitor.
    private boolean getNetServicedBytes(DockerMetrics m) {
        if (!isRunning) {
            return false;
        }
        boolean calRate = true;
        if (metricsCount == 0) {
            calRate = false;
        }
        String[] results = runShellCommand("cat " + netFilePath).split("\n");
        String resultLine = null;
        for (String r: results) {
            if (r.matches(".*"+ifaceName+".*")) {
                resultLine = r;
                break;
            }
        }

        if (resultLine != null) {
            resultLine = resultLine.trim();
            String receiveStr = resultLine.split("\\s+")[1];
            m.netRecBytes = Long.parseLong(receiveStr);

            String transmitStr = resultLine.split("\\s+")[8];
            m.netTransBytes = Long.parseLong(transmitStr);
            //System.out.print("netRec: " + m.netRecBytes + " netTrans: " + m.netTransBytes + "\n");
        }
        return calRate;
    }

    private List<String> readFileLines(String path){
        ArrayList<String> results= new ArrayList<String>();
        File file = new File(path);
        BufferedReader reader = null;
        boolean isError=false;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            while ((tempString = reader.readLine()) != null) {
                results.add(tempString);
                line++;
            }
            reader.close();
        } catch (IOException e) {
            //if we come to here, then means read file causes errors;
            //if reports this errors mission errors, it means this containers
            //has terminated, but nodemanager did not delete it yet. we stop monitoring
            //here
            if(e.toString().contains("FileNotFoundException")){
                isRunning=false;
            }
            isError=true;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

        if(!isError){
            return results;
        }else{
            return null;
        }
    }

    private void sendInfoToDatabase() {
        if (metricsCount == 0) {
            return;
        }
        DockerMetrics last = metrics.get(metricsCount - 1);

    }

    // TEST
    public void printStatus() {
        if (metricsCount == 0) {
            return;
        }
        DockerMetrics last = metrics.get(metricsCount - 1);
        System.out.print("docker pid: " + dockerPid +
        " total read: " + last.diskReadBytes +
        " total write: " + last.diskWriteBytes +
        " read rate: " + last.diskReadRate +
        " write rate: " + last.diskWriteRate + "\n" +
        "total receive: " + last.netRecBytes +
        " total transmit: " + last.netTransBytes +
        " receive rate: " + last.netRecRate +
        " transmit rate: " + last.netTransRate + "\n");
    }
}
