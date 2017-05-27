package MetricsSender;

import Server.TracerConf;
import info.*;

/**
 * Created by cwei on 4/3/17.
 */
public abstract class MetricsSender {
    protected TracerConf conf = TracerConf.getInstance();
    static String SPARK_PREFIX = "spark.";
    // public abstract void sendTaskMetrics(Task task);

    public abstract void sendContainerMetrics(ContainerMetrics cm);

    public abstract void sendStageMetrics(StageMetrics sm);

    public abstract void sendJobMetrics(JobMetrics jm);

    public abstract void sendAppMetrics(AppMetrics am);
}
