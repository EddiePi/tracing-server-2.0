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
class DockerMonitor {
    TracerConf conf = TracerConf.getInstance();
    private String dockerId;
    String containerId;

    // NOTE: type of dockerPid is String, NOT int
    String dockerPid = null;
    private String blkioPath;
    private String netFilePath;
    private String cpuPath;
    private String memoryPath;
    private Thread monitorThread;

    private String ifaceName;
    private DockerMetrics previousMetrics;
    private DockerMetrics currentmMetrics;
    int metricsCount = 0;

    volatile private boolean isRunning;

    public DockerMonitor(String containerId) {
        this.containerId = containerId;
        for(int i = 0; i < 5; i++) {
            this.dockerId = runShellCommand("docker inspect --format={{.Id}} " + containerId);
            if(this.dockerId.contains("Error")) {
                System.out.print("docker for " + containerId + " is not started yet. retry in 1 sec.\n");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if(this.dockerId.contains("Error")) {
            System.out.print("cannot get docker for " + containerId + "aborting\n");
            return;
        }

        this.dockerPid = runShellCommand("docker inspect --format={{.State.Pid}} " + containerId).trim();
        this.blkioPath= "/sys/fs/cgroup/blkio/docker/" + dockerId + "/";
        this.netFilePath = "/proc/" + dockerPid + "/net/dev";
        this.cpuPath = "/sys/fs/cgroup/cpu,cpuacct/docker/" + dockerId + "/";
        ifaceName  = conf.getStringOrDefault("tracer.docker.iface-name", "eth0");
        monitorThread = new Thread(new MonitorRunnable());
    }

    public void start() {
        isRunning = true;
        monitorThread.start();
        System.out.print("docker monitor thread started.\n");
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

    private class MonitorRunnable implements Runnable {

        @Override
        public void run(){
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
        }
    }

    public void updateCgroupValues() {
        previousMetrics = currentmMetrics;
        currentmMetrics = new DockerMetrics(dockerId, containerId);

        // calculate the cpu rate
        calculateCurrentCpuRate(currentmMetrics);

        // calculate the disk rate
        calculateCurrentDiskRate(currentmMetrics);

        // calculate the network rate
        calculateCurrentNetRate(currentmMetrics);
        metricsCount++;

        // TEST
        printStatus();
    }

//        private void updatePreviousTime() {
//            previousProfileTime = System.currentTimeMillis() / 1000;
//        }

    private boolean getCpuTime(DockerMetrics m) {
        if(!isRunning) {
            return false;
        }
        boolean calRate = true;
        if(metricsCount == 0) {
            calRate = false;
        }

        String dockerUrl = cpuPath + "cpuacct.usage";
        String sysUrl = "/proc/stat";

        // read the docker's cpu time
        List<String> readLines = readFileLines(dockerUrl);
        if(readLines != null) {
            String dockerCpuTimeStr = readLines.get(0);
            m.dockerCpuTime = Long.parseLong(dockerCpuTimeStr) / 1000000;
        }
        readLines = readFileLines(sysUrl);
        if(readLines != null) {
            String[] firstLineStr = readLines.get(0).split("\\s+");
            Long sysCpuTime = 0L;
            for(int i = 1; i < firstLineStr.length; i++) {
                sysCpuTime += Long.parseLong(firstLineStr[i]);
            }
            m.sysCpuTime = sysCpuTime;
        }
        return calRate;
    }

    private void calculateCurrentCpuRate(DockerMetrics m) {
        if(!getCpuTime((m))) {
            return;
        }

        //calculate the rate
        Double deltaSysTime = (m.sysCpuTime - previousMetrics.sysCpuTime) * 1.0;
        Double deltaDockerTime = (m.dockerCpuTime - previousMetrics.dockerCpuTime) * 1.0;
        m.cpuRate = deltaDockerTime / deltaSysTime;
    }

    // calculate the disk I/O rate
    private void calculateCurrentDiskRate(DockerMetrics m) {
        if (!getDiskServicedBytes(m)) {
            return;
        }
        // init timestamps
        Double deltaTime = (m.timestamp - previousMetrics.timestamp) * 1.0;

        // calculate the rate
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
        DockerMetrics last = currentmMetrics;

    }

    // TEST
    public void printStatus() {
        if (metricsCount == 0) {
            return;
        }
        DockerMetrics last = currentmMetrics;
        System.out.print("docker pid: " + dockerPid + "\n" +
        "cpu rate: " + last.cpuRate + "\n" +
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
