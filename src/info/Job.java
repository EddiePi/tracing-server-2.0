package info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Eddie on 2017/1/23.
 */
public class Job {
    public Integer jobId;
    public String appId;
    public Map<Integer, Stage> stageIdToStage;

    TimeStamps jobStamps;

    JobMetrics currentJobMetrics;
    public boolean isReporting = false;

    public Job (Integer jobId, String appId) {
        this.jobId = jobId;
        this.appId = appId;
        this.stageIdToStage = new HashMap<>();
        this.jobStamps = new TimeStamps();
        currentJobMetrics = new JobMetrics(appId, jobId);
    }

    public boolean addStage (Stage stageInfo) {
        if (!stageIdToStage.containsKey(stageInfo.stageId)) {
            stageIdToStage.put(stageInfo.stageId, stageInfo);
            return true;
        }
        return false;
    }

    public void resetCurrentJobMetrics() {
        for(Stage s: stageIdToStage.values()) {
            s.resetCurrentStageMetrics();
        }
        currentJobMetrics.reset();
    }

    public void updateStage (Stage stageInfo) {
        stageIdToStage.put(stageInfo.stageId, stageInfo);
    }

    // get a stage by its stageId.
    public Stage getStageById (Integer stageId) {
        Stage stage = stageIdToStage.get(stageId);
        return stage;
    }

    // get all stages belong to this job.
    public List<Stage> getAllStage () {
        return new ArrayList<>(stageIdToStage.values());
    }
}
