package info;

import docker.DockerMetrics;

/**
 * Created by Eddie on 2017/3/2.
 */
public abstract class Metrics {
    public Metrics() {
        timestamp = System.currentTimeMillis() / 1000;
    }
    public Long timestamp;
    // cpu
    public Double cpuUsage = 0.0;

    // memory
    public Long execMemoryUsage = 0L;
    public Long storeMemoryUsage = 0L;

    // disk
    public Long diskWriteBytes = 0L;
    public Long diskReadBytes = 0L;
    public Double diskWriteRate = 0.0;
    public Double diskReadRate = 0.0;

    // network
    public Long netRecBytes = 0L;
    public Long netTransBytes = 0L;
    public Double netRecRate = 0.0;
    public Double netTransRate = 0.0;

    public void reset() {
        timestamp = System.currentTimeMillis() / 1000;
        cpuUsage = 0.0D;
        execMemoryUsage = 0L;
        storeMemoryUsage = 0L;
    }

    public void plus(Metrics otherMetrics) {
        this.cpuUsage += otherMetrics.cpuUsage;

        this.execMemoryUsage += otherMetrics.execMemoryUsage;
        this.storeMemoryUsage += otherMetrics.storeMemoryUsage;

        this.diskWriteBytes += otherMetrics.diskWriteBytes;
        this.diskReadBytes += otherMetrics.diskReadBytes;
        this.diskWriteRate += otherMetrics.diskWriteRate;
        this.diskReadRate += otherMetrics.diskReadRate;

        this.netRecBytes += otherMetrics.netRecBytes;
        this.netTransBytes += otherMetrics.netTransBytes;
        this.netRecRate += otherMetrics.netRecRate;
        this.netTransRate += otherMetrics.netTransRate;
    }

    public void fraction(Double rate) {
        this.cpuUsage *= rate;


        this.execMemoryUsage = new Double(this.execMemoryUsage * rate).longValue();
        this.storeMemoryUsage = new Double(this.storeMemoryUsage * rate).longValue();

        this.diskWriteBytes = new Double(this.diskWriteBytes * rate).longValue();
        this.diskReadBytes = new Double(this.diskReadBytes * rate).longValue();
        this.diskWriteRate = this.diskWriteRate * rate;
        this.diskReadRate = this.diskReadRate * rate;

        this.netRecBytes = new Double(this.netRecBytes * rate).longValue();
        this.netTransBytes = new Double(this.netTransBytes * rate).longValue();
        this.netRecRate = this.netRecRate * rate;
        this.netTransRate = this.netTransBytes * rate;
    }

    public abstract Metrics clone();
}
