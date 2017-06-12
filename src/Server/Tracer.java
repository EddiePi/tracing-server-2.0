package Server;

import CsvUtils.ContainerCsvWriter;
import MetricsSender.KafkaMetricsSender;
import MetricsSender.PickleMetricsSender;
import docker.DockerMonitorManager;
import info.*;
import log.LogReaderManager;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Eddie on 2017/2/21.
 */
public class Tracer {
    private TracerConf conf = TracerConf.getInstance();
    public ConcurrentMap<String, App> applications = new ConcurrentHashMap<>();
    private boolean needFetch = false;
    private boolean fetchEnabled = false;

    private LogReaderManager logReaderManager;

    private DockerMonitorManager dockerMonitorManager;

    public ConcurrentMap<String, List<ContainerMetrics>> containerIdToMetrics = new ConcurrentHashMap<>();
    public List<String> containerToReport = new ArrayList<>();
    private int runningAppCount = 0;
    private boolean isTest = false;
    Integer reportInterval = conf.getIntegerOrDefault("tracer.report-interval", 1000);
    private boolean analyzerEnabled = conf.getBooleanOrDefault("tracer.ML.analyzer.enabled", false);


    private Analyzer analyzer = null;
    private class TracingRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    updateRunningApp();
                    if (runningAppCount > 0) {
                        if (isTest) {
                            printTaskInfo();
                            printHighLevelInfo();
                        } else {
                            sendInfo();
                        }
                    }
                    Thread.sleep(reportInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    // do nothing
                }
            }
        }
    }
    TracingRunnable runnable = new TracingRunnable();

    Thread tThread = new Thread(runnable);

    public List<TaskMetrics> getTaskMetrics(Task t) {
        return t.taskMetrics;
    }

    private static final Tracer instance = new Tracer();

    private PickleMetricsSender pms;

    private KafkaMetricsSender kms;

    private Tracer() {
        fetchEnabled = conf.getBooleanOrDefault("tracer.fetch.enabled", false);
        kms = new KafkaMetricsSender();
        logReaderManager = new LogReaderManager();
        dockerMonitorManager = new DockerMonitorManager();
    }

    public static Tracer getInstance() {
        return instance;
    }

    public void init() {
        logReaderManager.start();
    }

    /**
     * This method is called by <code>LogReaderManager</code>.
     * Used to create a new <code>DockerMonitor</code>.
     *
     * @param containerId
     */
    public void addContainerMonitor(String containerId) {
        dockerMonitorManager.addDockerMonitor(containerId);
    }

    /**
     * This method is called by <code>DockerMonitor</code>.
     * Used to stop a <code>LogReaderManager</code>
     *
     * @param containerId
     */
    public void removeContainerLogReader(String containerId) {
        logReaderManager.stopContainerLogReaderById(containerId);
    }

    public App getOrCreateApp(String appId) {
        App app;
        if (applications.containsKey(appId)) {
            app = applications.get(appId);
        } else {
            app = new App(appId);
            applications.put(appId, app);
            needFetch = true;
        }
        return app;
    }

    // TEST
    public void printTaskInfo() {
        DecimalFormat df = new DecimalFormat("0.000");
        for(App app: applications.values()) {
            Double cpuUsage = 0D;
            Long execMem = 0L;
            Long storeMem = 0L;
            Double diskReadRate = 0D;
            Double diskWriteRate = 0D;
            Double netRecRate = 0D;
            Double netTransRate = 0D;
            Map<Long, Task> taskMap = app.getAndClearReportingTasks();
            for(Task task: taskMap.values()) {
                System.out.print("task metrics size: " + task.taskMetrics.size() + "\n");
                for (TaskMetrics m : task.taskMetrics) {
                    if (m.cpuUsage < 0) {
                        continue;
                    }
                    cpuUsage += m.cpuUsage;
                    execMem += m.execMemoryUsage;
                    storeMem += m.storeMemoryUsage;
                    diskReadRate += m.diskReadRate;
                    diskWriteRate += m.diskWriteRate;
                    netRecRate += m.netRecRate;
                    netTransRate += m.netTransRate;

                }
            }
            System.out.print("app: " + app.appId + " has " + taskMap.size() + " tasks. " +
                    "cpu usage: " + df.format(cpuUsage) + " exec mem: " + execMem +
                    " store mem: " + + storeMem + "\n");
            System.out.print("the following info is constructed from docker." +
            " disk read rate: " + diskReadRate + " disk write rate: " + diskWriteRate +
            " net rec rate: " + netRecRate + " net trans rate: " + netTransRate + "\n");
        }
    }

    // TEST
    public void printHighLevelInfo() {
        DecimalFormat df = new DecimalFormat("0.000");
        for (App app: applications.values()) {
            List<StageMetrics> stageMetricsList = app.getAndClearReportingStageMetrics();
            System.out.print("number of stage to report: " + stageMetricsList.size() + "\n");
            for (StageMetrics metrics: stageMetricsList) {
                System.out.print("stages: " + metrics.stageId +
                        " cpu usage: " + df.format(metrics.cpuUsage) + " exec mem: " + metrics.execMemoryUsage +
                        " store mem: " + +metrics.storeMemoryUsage + "\n");

            }
            List<JobMetrics> jobMetricsList = app.getAndClearReportingJobMetrics();
            System.out.print("number of job to report: " + jobMetricsList.size() + "\n");
            for (JobMetrics metrics: jobMetricsList) {
                System.out.print("job: " + metrics.jobId +
                        " cpu usage: " + df.format(metrics.cpuUsage) + " exec mem: " + metrics.execMemoryUsage +
                        " store mem: " + +metrics.storeMemoryUsage + "\n");

            }
            List<AppMetrics> appMetricsList = app.getAndClearReportingAppMetrics();
            System.out.print("number of app to report: " + appMetricsList.size() + "\n");
            for (AppMetrics metrics: appMetricsList) {
                System.out.print("app: " + metrics.appId +
                        " cpu usage: " + df.format(metrics.cpuUsage) + " exec mem: " + metrics.execMemoryUsage +
                        " store mem: " + +metrics.storeMemoryUsage + "\n");
            }
        }
    }

    // we don't need this method.
    // metrics and log info are sent by the collector class respectively.
    public void sendInfo() {
        for(App app: applications.values()) {
            Map<Long, Task> taskMap = app.getAndClearReportingTasks();
            buildContainerMetrics(taskMap);
//            for(Task task: taskMap.values()) {
//                if (pms == null) {
//                    System.out.print("pms is null");
//                }
//                pms.sendTaskMetrics(task);
//            }
            for(List<ContainerMetrics> cmList: containerIdToMetrics.values()) {
                if (cmList.size() > 0) {
                    ContainerMetrics last = cmList.get(cmList.size() - 1);
                    pms.sendContainerMetrics(last);
                    kms.sendContainerMetrics(last);
                    if (analyzerEnabled) {
                        analyzer.addDataToAnalyze(last);
                    }
                }
            }
//            List<StageMetrics> sml = app.getAndClearReportingStageMetrics();
//            for(StageMetrics sm: sml) {
//                pms.sendStageMetrics(sm);
//            }
//            List<JobMetrics> jml = app.getAndClearReportingJobMetrics();
//            for(JobMetrics jm: jml) {
//                pms.sendJobMetrics(jm);
//            }
//            List<AppMetrics> aml = app.getAndClearReportingAppMetrics();
//            for(AppMetrics am: aml) {
//                pms.sendAppMetrics(am);
//            }
        }
    }

    // TODO rewrite this method
    private void buildContainerMetrics(Map<Long, Task> taskMap) {
        Map<String, ContainerMetrics> currentCmMap = new HashMap<>();
        for(Task task: taskMap.values()) {
            ContainerMetrics cMetrics = currentCmMap.get(task.containerId);
            if (cMetrics == null) {
                cMetrics = new ContainerMetrics(task.containerId);
                currentCmMap.put(task.containerId, cMetrics);
            }
            if (task.taskMetrics.size() > 0) {
                cMetrics.appId = task.appId;
                cMetrics.jobId = task.jobId;
                cMetrics.stageId = task.stageId;
                cMetrics.buildFullId();
                cMetrics.timestamp = task.taskMetrics.get(task.taskMetrics.size() - 1).timestamp;
                cMetrics.plus(task.taskMetrics.get(task.taskMetrics.size() - 1));
            }
        }

        // update container metrics in stage at runtime
        for (Map.Entry<String, ContainerMetrics> entry: currentCmMap.entrySet()) {
            ContainerMetrics containerMetrics = entry.getValue();
            String containerId = entry.getKey();
            containerIdToMetrics.get(containerId).add(containerMetrics);
            Stage stageToUpdate = getStageByIdentifier(
                    containerMetrics.appId, containerMetrics.jobId, containerMetrics.stageId);
            List<ContainerMetrics> stageCM =
                    stageToUpdate.containerMetricsMap.get(containerId);
            if (stageCM == null) {
                stageCM = new ArrayList<>();
            }
            stageCM.add(containerMetrics);
            stageToUpdate.containerMetricsMap.put(containerId, stageCM);
        }
    }

    private void updateRunningApp() {
        int runningCount = 0;
        for (App app: applications.values()) {
            if (app.hasReportingTask) {
                runningCount++;
            }
        }
        this.runningAppCount = runningCount;
    }

    public void fetchLastApp() {
        if(needFetch && fetchEnabled) {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //ContainerJsonFetcher containerJsonFetcher;
            //ContainerJsonWriter containerJsonWriter;
            ContainerCsvWriter containerCsvWriter;
            for (App app: applications.values()) {
                if (!app.fetched) {
//                    containerJsonFetcher = new ContainerJsonFetcher(conf, app, containerToReport);
//                    containerJsonFetcher.fetch();
//                    containerJsonWriter = new ContainerJsonWriter(conf, app);
//                    containerJsonWriter.write();
                    containerCsvWriter = new ContainerCsvWriter(conf, app);
                    containerCsvWriter.write();
                    app.fetched = true;
                }
            }
        }
        containerToReport.clear();
        containerIdToMetrics.clear();
        applications.clear();
        needFetch = false;
    }

    public Stage getStageByIdentifier(String appId, Integer jobId, Integer stageId) {
        App app = applications.get(appId);
        if(app == null) {
            return null;
        }
        Job job = app.getJobById(jobId);
        if(job == null) {
            return null;
        }
        Stage stage = job.getStageById(stageId);
        if(stage == null) {
            return null;
        }
        return stage;
    }
}
