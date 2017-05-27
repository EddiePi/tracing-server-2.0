package JsonUtils;

import Server.TracerConf;
import info.App;
import info.Job;
import info.Stage;
import info.Task;

/**
 * Created by Eddie on 2017/3/30.
 */
public class AppJsonFetcher {
    App app;
    TracerConf conf;
    String storagePrefix;
    String storageSuffix;
    String urlPrefix;
    String urlSuffix;
    public AppJsonFetcher(TracerConf conf, App app) {
        this.app = app;
        this.conf = conf;
        urlPrefix = "http://" +
                conf.getStringOrDefault("tracer.database.host", "localhost") +
                "/render?target=spark.";
        urlSuffix = "&format=json";
        storagePrefix = conf.getStringOrDefault("tracer.storage.root", "./");
        storageSuffix = ".json";
    }

    public void fetch() {
        String appId = app.appId;   // app_1
        // we cannot add suffix here. we need to add it after .CPU/execMemory...
        String appStoragePath = storagePrefix + appId; // ./app_1
        fetchAllMetrics(appId, appStoragePath);
        for(Job job: app.getAllJobs()) {
            String completeJobId = appId + ".job_" + job.jobId.toString();  // app_1.job_1
            String jobStoragePath = appStoragePath + "/job_" + job.jobId.toString();
            fetchAllMetrics(completeJobId, jobStoragePath);
            for(Stage stage: job.getAllStage()) {
                String completeStageId = completeJobId + ".stage_" + stage.stageId.toString();
                String stageStoragePath = jobStoragePath + "/stage_" + stage.stageId.toString();
                fetchAllMetrics(completeStageId, stageStoragePath);
                for(Task task: stage.getAllTasks()) {
                    String completeTaskId = completeStageId + "." +
                            task.containerId + ".task_" + task.taskId.toString();
                    String taskStoragePath = stageStoragePath +
                            "/" + task.containerId + "/task_" + task.taskId.toString();
                    fetchAllMetrics(completeTaskId, taskStoragePath);
                }
            }
        }
    }

    private void fetchAllMetrics(String identifier, String destPath) {
        for(String name: MetricNames.names) {
            String url = urlPrefix + identifier + "." + name + urlSuffix;
            //JsonCopier.copyJsonFromURL(url, destPath, identifier + "." + name + storageSuffix);

            //TEST
            // System.out.print("url, " + url + " destPath: " + destPath + " name: " + identifier + "\n");
        }
    }
}

