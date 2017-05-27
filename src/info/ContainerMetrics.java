package info;

/**
 * Created by Eddie on 2017/4/3.
 */
public class ContainerMetrics extends Metrics {
    public String containerId;
    public String appId;
    public Integer jobId;
    public Integer stageId;
    private String containerFullId;

    public ContainerMetrics(String containerId) {
        this.containerId = containerId;
    }

    public void buildFullId() {
        containerFullId = appId + "." + "job_" + jobId.toString() + "." + "stage_" + stageId + "." + containerId;
    }

    public String getFullId() {
        return containerFullId;
    }

    @Override
    public Metrics clone() {
        ContainerMetrics cmclone = new ContainerMetrics(this.containerId);
        cmclone.timestamp = this.timestamp;
        cmclone.cpuUsage = this.cpuUsage;
        cmclone.execMemoryUsage = this.execMemoryUsage;
        cmclone.storeMemoryUsage = this.storeMemoryUsage;
        cmclone.diskReadBytes = this.diskReadBytes;
        cmclone.diskReadRate = this.diskReadRate;
        cmclone.diskWriteBytes = this.diskWriteBytes;
        cmclone.diskWriteRate = this.diskWriteRate;
        cmclone.netRecBytes = this.netRecBytes;
        cmclone.netRecRate = this.netRecRate;
        cmclone.netTransBytes = this.netTransBytes;
        cmclone.netTransRate = this.netTransRate;
        return null;
    }

    public String toString() {
        String res;
        res = containerFullId + "|" +
                timestamp.toString() + "|" +
                cpuUsage.toString() + "|" +
                execMemoryUsage.toString() + "|" +
                storeMemoryUsage.toString() + "|" +
                diskReadRate.toString() + "|" +
                diskWriteRate.toString() + "|" +
                netRecRate.toString() + "|" +
                netTransRate.toString() + "|";
        return res;
    }
}
