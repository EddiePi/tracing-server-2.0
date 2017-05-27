package JsonUtils;

import Server.TracerConf;
import info.*;

import java.util.List;

/**
 * Created by Eddie on 2017/4/3.
 */
public class ContainerJsonFetcher {
    App app;
    List<String> containerIdList;
    TracerConf conf;
    String storagePrefix;
    String storageSuffix;
    String urlPrefix;
    String urlSuffix;

    public ContainerJsonFetcher(TracerConf conf, App app, List<String> containerIdList) {
        this.containerIdList = containerIdList;
        this.app = app;
        this.conf = conf;
        urlPrefix = "http://" +
                conf.getStringOrDefault("tracer.database.host", "localhost") +
                "/render?target=spark.";
        urlSuffix = "&format=json";
        storagePrefix = conf.getStringOrDefault("tracer.storage.root", "./").trim() + "/";
        storageSuffix = ".json";
    }

    public void fetch() {
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
                for(String containerId: containerIdList) {
                    String completeContainerId = completeStageId + "." + containerId;
                    String containerStoragePath = stageStoragePath + "/" + containerId;
                    fetchAllMetrics(completeContainerId, containerStoragePath);
                }
            }
        }
    }

    private void fetchAllMetrics(String identifier, String destPath) {
        for(String name: MetricNames.names) {
            String url = urlPrefix + identifier + "." + name + urlSuffix;
            String fullName = identifier + "." + name + storageSuffix;
            JsonCopier.copyJsonFromURL(url, destPath, fullName);
            trim(destPath + "/" + fullName);

            //TEST
            //System.out.print("url, " + url + " destPath: " + destPath + " name: " + identifier + "." + name + storageSuffix + "\n");
        }
    }

    private void trim(String path) {
        JsonReader reader = new JsonReader(path);
        reader.dataTrim();
    }
}
