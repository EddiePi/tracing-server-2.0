package info;

/**
 * Created by Eddie on 2017/3/6.
 */
public class JobMetrics extends Metrics {
    public String appId;
    public Integer jobId;

    public JobMetrics(String appId, Integer jobId) {
        super();
        this.appId = appId;
        this.jobId = jobId;
    }



    @Override
    public Metrics clone() {
        JobMetrics jmclone = new JobMetrics(this.appId, this.jobId);
        jmclone.timestamp = this.timestamp;
        jmclone.cpuUsage = this.cpuUsage;
        jmclone.execMemoryUsage = this.execMemoryUsage;
        jmclone.storeMemoryUsage = this.storeMemoryUsage;
        jmclone.diskReadBytes = this.diskReadBytes;
        jmclone.diskReadRate = this.diskReadRate;
        jmclone.diskWriteBytes = this.diskWriteBytes;
        jmclone.diskWriteRate = this.diskWriteRate;
        jmclone.netRecBytes = this.netRecBytes;
        jmclone.netRecRate = this.netRecRate;
        jmclone.netTransBytes = this.netTransBytes;
        jmclone.netTransRate = this.netTransRate;
        return jmclone;
    }
}
