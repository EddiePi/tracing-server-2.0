package JsonUtils;

import Server.TracerConf;
import info.App;
import info.ContainerMetrics;
import info.Job;
import info.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Eddie on 2017/5/18.
 */
public class ContainerJsonWriter {
    App app;
    TracerConf conf;
    String storagePrefix;
    String storageSuffix;

    public ContainerJsonWriter(TracerConf conf, App app) {
        this.app = app;
        this.conf = conf;
        storagePrefix = conf.getStringOrDefault("tracer.storage.root", "./").trim() + "/";
        storageSuffix = ".json";
    }

    public void write() {
        String appId = app.appId;   // app_1
        // we cannot add suffix here. we need to add it after .CPU/execMemory...
        String appStoragePath = storagePrefix + appId; // ./app_1
        // fetchAllMetrics(appId, appStoragePath);
        for(Job job: app.getAllJobs()) {
            String completeJobId = appId + ".job_" + job.jobId.toString();  // app_1.job_1
            String jobStoragePath = appStoragePath + "/job_" + job.jobId.toString();
            //fetchAllMetrics(completeJobId, jobStoragePath);
            for(Stage stage: job.getAllStage()) {
                String completeStageId = completeJobId + ".stage_" + stage.stageId.toString();
                String stageStoragePath = jobStoragePath + "/stage_" + stage.stageId.toString();
                //fetchAllMetrics(completeStageId, stageStoragePath);
                for(Map.Entry<String, List<ContainerMetrics>> entry: stage.containerMetricsMap.entrySet()) {
                    String completeContainerId = completeStageId + "." + entry.getKey();
                    String containerStoragePath = stageStoragePath + "/" + entry.getKey();
                    writeAllMetrics(completeContainerId, containerStoragePath, entry.getValue());
                }
            }
        }
    }

    private void writeAllMetrics(String identifier, String destPath, List<ContainerMetrics> metrics) {
        List<List<Double>> allMetrics = new ArrayList<>();
        for(int i = 0; i < 7; i++) {
            allMetrics.add(new ArrayList<>());
        }
        List<Long> timestampList = new ArrayList<>();
        for(ContainerMetrics metric: metrics) {
            allMetrics.get(0).add(metric.cpuUsage);
            allMetrics.get(1).add((double)metric.execMemoryUsage);
            allMetrics.get(2).add((double)metric.storeMemoryUsage);
            allMetrics.get(3).add(metric.diskWriteRate);
            allMetrics.get(4).add(metric.diskReadRate);
            allMetrics.get(5).add(metric.netRecRate);
            allMetrics.get(6).add(metric.netTransRate);
            timestampList.add(metric.timestamp);
        }
        JsonWriter jsonWriter = new JsonWriter();
        int index = 0;
        for(String name: MetricNames.names) {
            String fullName = identifier + "." + name;
            jsonWriter.setNewMetric(destPath, fullName + storageSuffix, fullName);
            jsonWriter.writeDataPoints(allMetrics.get(index), timestampList);
            index++;

            //TEST
            //System.out.print("url, " + url + " destPath: " + destPath + " name: " + identifier + "." + name + storageSuffix + "\n");
        }
    }
}
