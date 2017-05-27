package info;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Eddie on 2017/1/23.
 */
public class Stage {
    public Integer stageId; // required
    public String type; // required
    public Integer jobId; // required
    public String appId; // required
    public Map<Long, Task> taskIdToTask;
    // this is aggregated from task metrics
    // public List<StageMetrics> stageMetrics;

    public TimeStamps stageStamps;

    public StageMetrics currentStageMetrics;
    public boolean isReporting = false;

    // for now this is only used for app construction.
    public ConcurrentMap<String, List<ContainerMetrics>> containerMetricsMap;

    public Stage (int stageId, String type, Integer jobId, String appId) {
        this.stageId = stageId;
        this.type = type;
        this.jobId = jobId;
        this.appId = appId;
        // stageMetrics = new LinkedList<>();
        this.taskIdToTask = new HashMap<>();
        this.stageStamps = new TimeStamps();
        currentStageMetrics = new StageMetrics(appId, jobId, stageId);
        containerMetricsMap = new ConcurrentHashMap<String, List<ContainerMetrics>>();
    }

    public Stage(int stageId, Integer jobId, String appId) {
        this(stageId, "not-assigned", jobId, appId);
    }

    public boolean addTask (Task taskInfo) {
        if (!taskIdToTask.containsKey(taskInfo.taskId)) {
            taskIdToTask.put(taskInfo.taskId, taskInfo);
            return true;
        }
        return false;
    }

    public void updateTask (Task taskInfo) {
        taskIdToTask.put(taskInfo.taskId, taskInfo);
    }

    // get a task by its taskId. return null if the task is not in the stage.
    public Task getTaskById (Long taskId) {
        Task task = taskIdToTask.get(taskId);
        return task;
    }

    public void resetCurrentStageMetrics() {
        for (Task t: taskIdToTask.values()) {
            t.resetCurrentTaskMetrics();
        }
        currentStageMetrics.reset();
    }

    //get all tasks belong to this stage.
    public List<Task> getAllTasks() {
        return new ArrayList<>(taskIdToTask.values());
    }
}
