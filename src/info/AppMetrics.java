package info;

/**
 * Created by Eddie on 2017/3/6.
 */
public class AppMetrics extends Metrics {
    public String appId;
    public AppMetrics(String appId) {
        super();
        this.appId = appId;
    }

    @Override
    public Metrics clone() {
        AppMetrics amclone = new AppMetrics(this.appId);
        amclone.timestamp = this.timestamp;
        amclone.cpuUsage = this.cpuUsage;
        amclone.execMemoryUsage = this.execMemoryUsage;
        amclone.storeMemoryUsage = this.storeMemoryUsage;
        amclone.diskReadBytes = this.diskReadBytes;
        amclone.diskReadRate = this.diskReadRate;
        amclone.diskWriteBytes = this.diskWriteBytes;
        amclone.diskWriteRate = this.diskWriteRate;
        amclone.netRecBytes = this.netRecBytes;
        amclone.netRecRate = this.netRecRate;
        amclone.netTransBytes = this.netTransBytes;
        amclone.netTransRate = this.netTransRate;
        return amclone;
    }
}
