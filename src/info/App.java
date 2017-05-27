package info;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Eddie on 2017/1/25.
 */
public class App {
    public String appId;
    public Map<Integer, Job> jobIdToJob;
    private ConcurrentMap<Long, Task> tasks;
    private ConcurrentMap<Long, Task> tasksToReport;
    public ConcurrentMap<Integer, List<StageMetrics>> stageMetricsToReport;
    public ConcurrentMap<Integer, List<JobMetrics>> jobMetricsToReport;
    public List<AppMetrics> appMetricsToReport;
    public boolean hasReportingTask = false;
    public boolean fetched = false;

    TimeStamps appStamps;
    AppMetrics currentAppMetrics;

    public App(String appId) {
        this.appId = appId;

        jobIdToJob = new HashMap<>();
        tasks = new ConcurrentHashMap<>();
        tasksToReport = new ConcurrentHashMap<>();
        stageMetricsToReport = new ConcurrentHashMap<>();
        jobMetricsToReport = new ConcurrentHashMap<>();
        appMetricsToReport = new ArrayList<>();
        appStamps = new TimeStamps();
        currentAppMetrics = new AppMetrics(appId);
    }

    // this is an method in 'App'
    // task metrics should be all set before this method is called!
    public void addOrUpdateTask(Task task) {
        synchronized (this) {
            tasks.put(task.taskId, task);
            // update corresponding job and stage
            Job j = getJobById(task.jobId);
            if (j == null) {
                j = new Job(task.jobId, appId);
                jobIdToJob.put(task.jobId, j);
            }
            Stage s = getStageById(task.jobId, task.stageId);
            if (s == null) {
                s = new Stage(task.stageId, task.jobId, appId);
                j.stageIdToStage.put(task.stageId, s);
            }
            s.taskIdToTask.put(task.taskId, task);

            if (task.taskMetrics.isEmpty()) {
                return;
            }
            TaskMetrics metricsToReport = task.taskMetrics.get(task.taskMetrics.size() - 1);
            // quick return if the task is in INIT status.
            if(metricsToReport.status.equals("INIT")) {
                return;
            }

            // update metrics to report
            Task newReportingTask = tasksToReport.get(task.taskId);
            if (newReportingTask == null) {
                newReportingTask = task.clone();
                newReportingTask.taskMetrics.clear();
                tasksToReport.put(newReportingTask.taskId, newReportingTask);
            }
            newReportingTask.appendMetrics(metricsToReport);
            hasReportingTask = true;

        }
        //tasksToReport.put(task.taskId, task);
    }

    // we don't want other classes to change tasks map. so clone it.
    public Map<Long, Task> getAllTasks() {
        Map<Long, Task> taskClone = new HashMap<>(tasks);
        return taskClone;
    }

    private void buildHighLevelMetricsToReport(Map<Long, Task> tasksToReport) {
        this.resetCurrentAppMetrics();
        Job curJob;
        Stage curStage;
        for (Task t: tasksToReport.values()) {
            curJob = getJobById(t.jobId);
            curJob.isReporting = true;
            curStage = curJob.getStageById(t.stageId);
            curStage.isReporting = true;
            curJob.currentJobMetrics.plus(t.currentMetrics);
            curStage.currentStageMetrics.plus(t.currentMetrics);
            currentAppMetrics.plus(t.currentMetrics);
        }
        appMetricsToReport.add(currentAppMetrics);
        int jobCount = 0;
        for(Job j: jobIdToJob.values()) {
            if (!j.isReporting) {
                continue;
            }
            jobCount++;
            List<JobMetrics> jm = jobMetricsToReport.get(j.jobId);
            if (jm == null) {
                jm = new ArrayList<>();
                jobMetricsToReport.put(j.jobId, jm);
            }

            int stageCount = 0;
            for (Stage s: j.stageIdToStage.values()) {
                if (!s.isReporting) {
                    continue;
                }
                stageCount++;
                List<StageMetrics> sm = stageMetricsToReport.get(s.stageId);
                if (sm == null) {
                    sm = new ArrayList<>();
                    stageMetricsToReport.put(s.stageId, sm);
                }
                sm.add(s.currentStageMetrics);
                s.isReporting = false;
            }
            j.currentJobMetrics.fraction(1D / stageCount);
            currentAppMetrics.fraction(1D / jobCount);
            jm.add(j.currentJobMetrics);
            j.isReporting = false;
        }
    }

    private void buildStageMetricsToReport(Map<Long, Task> tasksToReport) {
        Map<Integer, StageMetrics> currentStageMetricsMap = new HashMap<>();
        StageMetrics stageMetrics;
        for(Task task: tasksToReport.values()) {
            // if this task has been reported we go to next task
            if (task.lastMetrics == null) {
                continue;
            }
            Integer stageId = task.stageId;
            stageMetrics = currentStageMetricsMap.get(stageId);
            if(stageMetrics == null) {
                stageMetrics = new StageMetrics(task.appId, task.jobId, task.stageId);
                currentStageMetricsMap.put(stageId, stageMetrics);
            }
            stageMetrics.cpuUsage += task.lastMetrics.cpuUsage;
            stageMetrics.execMemoryUsage += task.lastMetrics.execMemoryUsage;
            stageMetrics.storeMemoryUsage += task.lastMetrics.storeMemoryUsage;
        }
        for (Map.Entry<Integer, StageMetrics> smentry: currentStageMetricsMap.entrySet()) {
            Integer key = smentry.getKey();
            StageMetrics value = smentry.getValue();
            List<StageMetrics> sml;
            sml = stageMetricsToReport.get(key);
            if (sml == null) {
                sml = new ArrayList<>();
                stageMetricsToReport.put(key, sml);
            }
            sml.add(value);
        }
    }

    public List<AppMetrics> getAndClearReportingAppMetrics() {
        List<AppMetrics> aml = new ArrayList<>(appMetricsToReport);
        appMetricsToReport.clear();
        return aml;
    }

    public List<StageMetrics> getAndClearReportingStageMetrics() {
        synchronized (this) {
            List<StageMetrics> stageMetricsList = new ArrayList<>();
            Map<Integer, List<StageMetrics>> stageMetricsClone = new HashMap<>(stageMetricsToReport);
            stageMetricsToReport.clear();
            for(List<StageMetrics> sml: stageMetricsClone.values()) {
                for(StageMetrics sm: sml) {
                    stageMetricsList.add(sm);
                }
            }
            return stageMetricsList;
        }
    }

    public List<JobMetrics> getAndClearReportingJobMetrics() {
        synchronized (this) {
            List<JobMetrics> jobMetricsList = new ArrayList<>();
            Map<Integer, List<JobMetrics>> stageMetricsClone = new HashMap<>(jobMetricsToReport);
            jobMetricsToReport.clear();
            for(List<JobMetrics> jml: stageMetricsClone.values()) {
                for(JobMetrics jm: jml) {
                    jobMetricsList.add(jm);
                }
            }
            return jobMetricsList;
        }
    }

    public Map<Long, Task> getReporingTasks() {
        return tasksToReport;
    }

    public Map<Long, Task> getAndClearReportingTasks() {
        synchronized (this) {
            Map<Long, Task> taskMapClone = new HashMap<>(tasksToReport);
            buildHighLevelMetricsToReport(taskMapClone);
            // buildStageMetricsToReport(taskMapClone);
            tasksToReport.clear();
            hasReportingTask = false;
            return taskMapClone;
        }
    }

    public Task getTaskbyId(Long taskId) {
        return tasks.get(taskId);
    }





    // TODO all work related to the abstract phase (stage, job) will be refined later.
    public void resetCurrentAppMetrics() {
        for(Job j: jobIdToJob.values()) {
            j.resetCurrentJobMetrics();
        }
        currentAppMetrics.reset();
    }

    public boolean addJob(Job jobInfo) {
        if (!jobIdToJob.containsKey(jobInfo.jobId)) {
            jobIdToJob.put(jobInfo.jobId, jobInfo);
            return true;
        }
        return false;
    }

    public void updateJob(Job jobInfo) {
        jobIdToJob.put(jobInfo.jobId, jobInfo);
    }

    public Job getJobById (Integer jobId) {
        Job job = jobIdToJob.get(jobId);
        return job;
    }

    public Stage getStageById(Integer jobId, Integer stageId) {
        Job j = jobIdToJob.get(jobId);
        if (j == null) {
            return null;
        }
        return j.getStageById(stageId);
    }

    public Task getTaskById(Integer jobId, Integer stageId, Long taskId) {
        Job j = jobIdToJob.get(jobId);
        if (j == null) {
            return null;
        }
        Stage s = j.getStageById(stageId);
        if (s == null) {
            return null;
        }
        return s.getTaskById(taskId);
    }

    public List<Job> getAllJobs() {
        return new ArrayList<>(jobIdToJob.values());
    }
}

