namespace java RPCService

struct TaskInfo {
	1: i64 taskId;
	2: i32 stageId;
	3: i32 stageAttemptId;
	4: i32 jobId;
	5: string appId;
	6: i64 startTime;
	7: i64 finishTime;
	8: double cpuUsage;
	9: i64 execMemory;
	10: i64 storeMemory;
	11: string status;	//RUNNING, SUCCEEDED, FAILED...
	12: string containerId;
}

struct StageInfo {
	1: i32 stageId;
	2: string type;	//ShuffleMap or Final stage
	3: i32 jobId;
	4: string appId;
	5: string status;	//ACTIVE, COMPLETE, PENDING or FAILED
	6: i32 taskNum;
	7: i64 startTime;
	8: i64 finishTime;
}

struct JobInfo {
	1: i32 jobId;
	2: string appId;
	3: string status;	//RUNNING, SUCCEEDED, FAILED...
	4: i64 startTime;
	5: i64 finishTime;
}

service TracingService {
	void updateTaskInfo (1: TaskInfo task)

	void updateStageInfo (1: StageInfo stage)

	void updateJobInfo(1: JobInfo job)

	void notifyCommonEvent(1: SchedulerEvent event)
	
	void notifyTaskEndEvent(1: TaskEndEvent event)

	void notifyContainerEvent(1: ContainerEvent evnet)
}

struct SchedulerEvent {
	1: string event;
	2: i64 timeStamp;
	3: string reason;
}

struct TaskEndEvent {
	1: i64 taskId;
	2: i32 stageId;
	3: i32 jobId;
	4: string appId;
	5: i64 timeStamp;
	6: string reason;
}

struct ContainerEvent {
	1: string containerId;
	2: string action;	//ADD or REMOVE
}
