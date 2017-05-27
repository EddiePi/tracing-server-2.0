package info;

import docker.DockerMetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eddie on 2017/1/23.
 */
public class Task {
    // these fields are task identifiers
    public Long taskId; // required
    public Integer stageId; // required
    public Integer stageAttemptId; // required
    public Integer jobId; // required
    public String appId; // required
    public String containerId;

    public List<TaskMetrics> taskMetrics;
    public TaskMetrics lastMetrics;
    public TaskMetrics currentMetrics;
    public TimeStamps taskStamps;

    public Task (long taskId, int stageId, int stageAttemptId, int jobId, String appId, String containerId) {
        this.taskId = taskId;
        this.stageId = stageId;
        this.stageAttemptId = stageAttemptId;
        this.jobId = jobId;
        this.appId = appId;
        this.containerId = containerId;

        this.taskMetrics = new ArrayList<>();
        this.taskStamps = new TimeStamps();
    }

    public void initTask (long startTime) {
        this.taskStamps.startTimeMillis = startTime;
        TaskMetrics initTaskMetrics = new TaskMetrics();
        initTaskMetrics.status = "RUNNING";
        this.taskMetrics.add(initTaskMetrics);
    }

    //
    public void appendMetrics(Double cpuUsage, Long execMemoryUsage, Long storeMemoryUsage) {
        TaskMetrics m = new TaskMetrics();
        m.cpuUsage = cpuUsage;
        m.execMemoryUsage = execMemoryUsage;
        m.storeMemoryUsage = execMemoryUsage;
        taskMetrics.add(m);
    }

    public void appendMetrics(TaskMetrics m) {
        taskMetrics.add(m);
        lastMetrics = m;
        currentMetrics = m;
    }

    public void setMetricsFromDocker(DockerMetrics dockerMetrics, Integer tasksInDocker) {
        lastMetrics.setInfoFromDockerMetrics(dockerMetrics, tasksInDocker);
    }

    public Task clone() {
        Task taskClone = new Task(this.taskId, this.stageId, this.stageAttemptId,
                this.jobId, this.appId, this.containerId);
        taskClone.taskMetrics = new ArrayList<>(this.taskMetrics);
        taskClone.taskStamps = this.taskStamps.clone();
        taskClone.currentMetrics = this.currentMetrics;
        taskClone.lastMetrics = this.lastMetrics;
        return taskClone;
    }

    public void resetCurrentTaskMetrics() {
        this.currentMetrics = new TaskMetrics();
    }
//    // update the task  taskMetrics
//    public void updateTask (long finishTime, double cpuUsage, int peakMemoryUsage, String status) {
//        this.taskStamps.finishTimeMillis = finishTime;
//        this.taskMetrics.cpuUsage = cpuUsage;
//        this.taskMetrics.peakMemoryUsage = peakMemoryUsage;
//        updateTaskStatus(status);
//    }
//
//    // maybe we want to update the status alone
//    public void updateTaskStatus (String status) {
//        this.taskMetrics.status = status;
//    }
//
//    public void updateCpuUsage (double cpuUsage) {
//        this.taskMetrics.cpuUsage = cpuUsage;
//    }
//
//    public void updatePeakMemory (int peakMemoryUsage) {
//        this.taskMetrics.peakMemoryUsage = peakMemoryUsage;
//    }

    // TEST
    public void printTaskMetrics() {
        TaskMetrics last;
        if (taskMetrics.size() > 0) {
            last = taskMetrics.get(taskMetrics.size() - 1);
        } else {
            return;
        }
        System.out.print("task: " + taskId + "start time: " + last.startTimeStamp +
        " cpu: " + last.cpuUsage +
        " exec mem: " + last.execMemoryUsage +
        " store mem: " + last.storeMemoryUsage + "\n" +
        " disk read: " + last.diskReadBytes +
        " disk write: " + last.diskWriteBytes +
        " net rec: " + last.netRecBytes +
        " net trans: " + last.netTransBytes + "\n\n");
    }
}
