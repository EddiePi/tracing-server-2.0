package RPCService;

import JsonUtils.AppJsonFetcher;
import Server.Tracer;
import Server.TracerConf;
import docker.DockerMonitor;
import info.App;
import info.ContainerMetrics;
import info.TaskMetrics;
import info.Task;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Eddie on 2017/1/23.
 */
public class TracingServiceImpl implements TracingService.Iface{

    private Tracer tracer = Tracer.getInstance();

    private ConcurrentMap<String, DockerMonitor> dockerMonitorMap = tracer.containerIdToDM;

    @Override
    public void updateTaskInfo(TaskInfo task) throws TException {
        Task t = tracer.getOrCreateTask(task.appId, task.jobId,
                task.stageId, task.stageAttemptId, task.taskId, task.containerId);
        if (t.containerId == null && task.containerId != null) {
            t.containerId = task.containerId;
        }
        TaskMetrics tTaskMetrics = new TaskMetrics();
        // cpu
        if (Double.isNaN(task.cpuUsage)) {
            tTaskMetrics.cpuUsage = 0.0D;
        } else {
            tTaskMetrics.cpuUsage = Math.max(task.cpuUsage, 0.0);
        }

        // memory
        tTaskMetrics.execMemoryUsage = Math.max(task.execMemory, 0L);
        tTaskMetrics.storeMemoryUsage = Math.max(task.storeMemory, 0L);
        tTaskMetrics.startTimeStamp = Math.max(task.startTime, 0L);
        tTaskMetrics.finishTimeStamp = Math.max(task.finishTime, 0L);
        tTaskMetrics.status = task.status;
        t.appendMetrics(tTaskMetrics);
        tracer.updateTask(t);

        // disk and
//        System.out.print("taskId: " + task.taskId +
//                " containerId: " + task.containerId +
//                " stageId: " + task.stageId +
//                " jobId: " + task.jobId +
//                " appId: " + task.appId +
//                " cpu usage: " + task.cpuUsage +
//                " execution memory: " + task.execMemory +
//                " storage memory: " + task.storeMemory +
//                " start time: " + task.startTime +
//                " end time: " + task.finishTime + "\n");

    }

    @Override
    public void updateJobInfo(JobInfo job) throws TException {

    }

    @Override
    public void updateStageInfo(StageInfo stage) throws TException {

    }

    @Override
    public void notifyCommonEvent(SchedulerEvent event) throws TException {
        String message = "SchedulerEvent received. event: " + event.event;
        if (!event.reason.equals("") || event.reason != null) {
            message += " reason: " + event.reason;
        }
        message += "\n";
        System.out.print(message);
    }

    @Override
    public void notifyTaskEndEvent(TaskEndEvent event) throws TException {
        App app = tracer.applications.get(event.appId);
        //System.out.print("TaskEndEvent received. taskId: " + event.taskId +
        //" status: " + event.reason + "\n");
    }

    @Override
    public void notifyContainerEvent(ContainerEvent event) throws TException {
        String containerId = event.containerId;
        if (event.action.equals("ADD")) {
            if(!dockerMonitorMap.containsKey(containerId)) {
                System.out.print("adding docker: " + containerId + "\n");
                DockerMonitor dockerMonitor = new DockerMonitor(containerId);
                dockerMonitor.start();
                dockerMonitorMap.put(containerId, dockerMonitor);
                tracer.containerIdToMetrics.put(containerId, new ArrayList<ContainerMetrics>());
            }
        }

        // TODO
        if (event.action.equals("REMOVE")) {
            if (dockerMonitorMap.containsKey(containerId)) {
                DockerMonitor dockerMonitor = dockerMonitorMap.remove(containerId);
                tracer.containerToReport.add(containerId);
                dockerMonitor.stop();
            }
            if(dockerMonitorMap.isEmpty()) {
                tracer.fetchLastApp();
            }
        }
    }
}
