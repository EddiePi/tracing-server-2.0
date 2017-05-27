package docker;

/**
 * Created by Eddie on 2017/2/23.
 */
public class DockerMetrics {
    public String dockerId;
    public String containerId;
    // docker taskMetrics
    // unit: second
    public Long timestamp;
    // disk taskMetrics
    public Long diskReadBytes = 0L;
    public Long diskWriteBytes = 0L;
    public Double diskReadRate = 0.0;
    public Double diskWriteRate = 0.0;

    // network taskMetrics
    public Long netRecBytes = 0L;
    public Long netTransBytes = 0L;
    public Double netRecRate = 0.0;
    public Double netTransRate = 0.0;

    public DockerMetrics(String dockerId, String containerId) {
        timestamp = System.currentTimeMillis() / 1000;
        this.dockerId = dockerId;
        this.containerId = containerId;
    }
}
