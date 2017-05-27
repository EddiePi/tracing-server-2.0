package info;

import docker.DockerMetrics;

/**
 * Created by Eddie on 2017/1/25.
 */
public class TaskMetrics extends Metrics {

    public TaskMetrics() {
        super();
    }

    // time
    public Long startTimeStamp;
    public Long finishTimeStamp;

    public String status;   //INIT, RUNNING, SUCCESS, FAILED

    @Override
    public Metrics clone() {
        TaskMetrics tmclone = new TaskMetrics();
        tmclone.timestamp = this.timestamp;
        tmclone.cpuUsage = this.cpuUsage;
        tmclone.execMemoryUsage = this.execMemoryUsage;
        tmclone.storeMemoryUsage = this.storeMemoryUsage;
        tmclone.diskReadBytes = this.diskReadBytes;
        tmclone.diskReadRate = this.diskReadRate;
        tmclone.diskWriteBytes = this.diskWriteBytes;
        tmclone.diskWriteRate = this.diskWriteRate;
        tmclone.netRecBytes = this.netRecBytes;
        tmclone.netRecRate = this.netRecRate;
        tmclone.netTransBytes = this.netTransBytes;
        tmclone.netTransRate = this.netTransRate;
        tmclone.startTimeStamp = this.startTimeStamp;
        tmclone.finishTimeStamp = this.finishTimeStamp;
        tmclone.status = this.status;
        return tmclone;
    }

    public void setInfoFromDockerMetrics(DockerMetrics dockerMetrics, Integer tasksInDocker) {
        Double rate = 1D / tasksInDocker;
        this.diskWriteBytes = new Double(dockerMetrics.diskWriteBytes * rate).longValue();
        this.diskReadBytes = new Double(dockerMetrics.diskReadBytes * rate).longValue();
        this.diskWriteRate = dockerMetrics.diskWriteRate * rate;
        this.diskReadRate = dockerMetrics.diskReadRate * rate;

        this.netRecBytes = new Double(dockerMetrics.netRecBytes * rate).longValue();
        this.netTransBytes = new Double(dockerMetrics.netTransBytes * rate).longValue();
        this.netRecRate = dockerMetrics.netRecRate * rate;
        this.netTransRate = dockerMetrics.netTransBytes * rate;
    }
}
