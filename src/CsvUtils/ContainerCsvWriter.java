package CsvUtils;

import JsonUtils.JsonWriter;
import JsonUtils.MetricNames;
import Server.TracerConf;
import info.App;
import info.ContainerMetrics;
import info.Job;
import info.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Eddie on 2017/5/19.
 */
public class ContainerCsvWriter {
    App app;
    TracerConf conf;
    String storagePrefix;
    String storageSuffix;
    Map<String, List<ContainerMetrics>> containerMetricsMap = new HashMap();

    public ContainerCsvWriter(TracerConf conf, App app) {
        this.app = app;
        this.conf = conf;
        storagePrefix = conf.getStringOrDefault("tracer.storage.root", "./").trim() + "/";
        storageSuffix = ".csv";
    }

    public void write() {
        String appId = app.appId;   // app_1
        // we cannot add suffix here. we need to add it after .CPU/execMemory...
        String appStoragePath = storagePrefix + appId; // ./app_1
        // fetchAllMetrics(appId, appStoragePath);
        for(Job job: app.getAllJobs()) {
            //fetchAllMetrics(completeJobId, jobStoragePath);
            for(Stage stage: job.getAllStage()) {
                //fetchAllMetrics(completeStageId, stageStoragePath);
                for(Map.Entry<String, List<ContainerMetrics>> entry: stage.containerMetricsMap.entrySet()) {
                    List<ContainerMetrics> curMetricList = containerMetricsMap.get(entry.getKey());
                    if(curMetricList == null) {
                        curMetricList = new ArrayList<>();
                    }
                    curMetricList.addAll(entry.getValue());
                    containerMetricsMap.put(entry.getKey(), curMetricList);
                }
            }
        }
        CsvWriter csvWriter = new CsvWriter();
        for(Map.Entry<String, List<ContainerMetrics>> entry:
                containerMetricsMap.entrySet()) {
            String containerStoragePath = appStoragePath + "/" + entry.getKey();
            String completeContainerId = appId + "." + entry.getKey();
            csvWriter.setNewMetric(containerStoragePath,
                    completeContainerId + storageSuffix);
            csvWriter.writeDataPoints(entry.getValue());
        }
    }
}
