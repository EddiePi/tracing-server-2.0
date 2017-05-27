package JsonUtils;

import info.App;
import info.ContainerMetrics;
import info.Job;
import info.Stage;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Eddie on 2017/4/17.
 */
public class AppConstructor {
    String topPath; // topPath is also application name
    String appId;
    Map<Integer, List<Integer>> jobToStageMap;

    private final static HashMap<String, String> fileNameToMetricNameMap = new HashMap<String, String>();
    static {
        fileNameToMetricNameMap.put("CPU", "cpuUsage");
        fileNameToMetricNameMap.put("execution-memory", "execMemoryUsage");
        fileNameToMetricNameMap.put("storage-memory", "storeMemoryUsage");
        fileNameToMetricNameMap.put("disk-read-rate", "diskReadRate");
        fileNameToMetricNameMap.put("disk-write-rate", "diskWriteRate");
        fileNameToMetricNameMap.put("net-receive-rate", "netRecRate");
        fileNameToMetricNameMap.put("net-transfer-rate", "netTransRate");
    }

    public static App getApp(String path) {
        AppConstructor appConstructor = new AppConstructor(path);
        return appConstructor.construct();
    }

    public AppConstructor(){}


    public AppConstructor(String topPath) {
        setApp(topPath);
    }

    public void setApp(String path) {
        this.topPath = path;
        appId = getLastPiece(topPath, "/");
    }

    // public for test
    public App construct() {
        App app = new App(appId);
        File topDirectory = new File(topPath);
        File[] jobDirArray = topDirectory.listFiles();
        // job level iteration
        for (File jobDir : jobDirArray) {
            if (!jobDir.isDirectory()) {
                continue;
            }
            String jobNameStr = jobDir.getName();
            Integer jobId = Integer.parseInt(getLastPiece(jobNameStr, "_"));
            Job job = app.getJobById(jobId);
            if (job == null) {
                job = new Job(jobId, appId);
            }
            File[] stageDirArray = jobDir.listFiles();

            // stage level iteration
            for (File stageDir : stageDirArray) {
                if (!stageDir.isDirectory()) {
                    continue;
                }
                // get stage id and Stage. update the Stage at end of the iteration
                String stageNameStr = stageDir.getName();
                Integer stageId = Integer.parseInt(getLastPiece(stageNameStr, "_"));
                Stage stage = job.getStageById(stageId);
                if (stage == null) {
                    stage = new Stage(stageId, jobId, appId);
                }
                File[] containerDirArray = stageDir.listFiles();
                List<ContainerMetrics> containerMetrics;
                // container level iteration
                for (File containerDir : containerDirArray) {
                    if (!containerDir.isDirectory()) {
                        continue;
                    }
                    File[] metricFiles = containerDir.listFiles();
                    String containerId = containerDir.getName();



                    // construct container metrics
                    try {
                        containerMetrics = constructContainer(containerDir, containerId, jobId, stageId);
                        stage.containerMetricsMap.put(containerId, containerMetrics);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
                job.stageIdToStage.put(stageId, stage);
            }

            app.jobIdToJob.put(jobId, job);
        }
        return app;
    }

    private List<ContainerMetrics> constructContainer(
            File dir, String containerId, Integer jobId, Integer stageId)
            throws JSONException {
        Long startTime = Long.MAX_VALUE;
        Long endTime = 0L;
        JsonReader jsonReader = new JsonReader();
        JSONArray cpuUsage = null;
        JSONArray execMemory = null;
        JSONArray storeMemory = null;
        JSONArray diskRead = null;
        JSONArray diskWrite = null;
        JSONArray netRec = null;
        JSONArray netTrans = null;
        for (File metricFile : dir.listFiles()) {
            String path = metricFile.getAbsolutePath();
            String metricKey = getMetricName(metricFile.getName());
            jsonReader.setNewPath(path);
            JSONArray tmpData = jsonReader.getData();
            if(tmpData == null) {
                continue;
            }
            Long startTimeTmp = tmpData.getJSONArray(0).getLong(1);
            Long endTimeTmp = tmpData.getJSONArray(tmpData.length() - 1).getLong(1);
            if (startTime > startTimeTmp) {
                startTime = startTimeTmp;
            }
            if (endTime < endTimeTmp) {
                endTime = endTimeTmp;
            }
            switch (metricKey) {
                case "CPU":
                    cpuUsage = tmpData;
                    break;
                case "execution-memory":
                    execMemory = tmpData;
                    break;
                case "storage-memory":
                    storeMemory = tmpData;
                    break;
                case "disk-read-rate":
                    diskRead = tmpData;
                    break;
                case "disk-write-rate":
                    diskWrite = tmpData;
                    break;
                case "net-receive-rate":
                    netRec = tmpData;
                    break;
                case "net-transfer-rate":
                    netTrans = tmpData;
                    break;
                default:
                    System.out.print("metric name error!\n");
            }
        }
        List<ContainerMetrics> containerMetricsList;
        Integer length = (int) (endTime - startTime + 1);
        containerMetricsList = new ArrayList<ContainerMetrics>(length);
        for (int i = 0; i < length; i++) {
            ContainerMetrics containerMetrics = new ContainerMetrics(containerId);
            containerMetrics.timestamp = startTime + i;
            containerMetrics.appId = appId;
            containerMetrics.jobId = jobId;
            containerMetrics.stageId = stageId;
            containerMetrics.buildFullId();
            containerMetricsList.add(containerMetrics);
        }
        for (int i = 0; i < length; i++) {
            // set cpu
            if (cpuUsage != null) {
                if (cpuUsage.length() > i) {
                    Double cpuValue = getDoubleOrDefault(cpuUsage.getJSONArray(i).getString(0), 0D);
                    Integer offset = (int) (cpuUsage.getJSONArray(i).getLong(1) - startTime);
                    containerMetricsList.get(offset).cpuUsage = cpuValue;
                }
            }

            // set exec mem
            if (execMemory != null) {
                if (execMemory.length() > i) {
                    Long execMemValue = getLongOrDefault(execMemory.getJSONArray(i).getString(0), 0L);
                    Integer offset = (int) (execMemory.getJSONArray(i).getLong(1) - startTime);
                    containerMetricsList.get(offset).execMemoryUsage = execMemValue;
                }
            }

            // set store mem
            if (storeMemory != null) {
                if (storeMemory.length() > i) {
                    Long storeMemValue = getLongOrDefault(storeMemory.getJSONArray(i).getString(0), 0L);
                    Integer offset = (int) (storeMemory.getJSONArray(i).getLong(1) - startTime);
                    containerMetricsList.get(offset).storeMemoryUsage = storeMemValue;
                }
            }

            if (diskRead != null) {
                if (diskRead.length() > i) {
                    Double diskReadValue = getDoubleOrDefault(diskRead.getJSONArray(i).getString(0), 0D);
                    Integer offset = (int) (diskRead.getJSONArray(i).getLong(1) - startTime);
                    containerMetricsList.get(offset).diskReadRate = diskReadValue;
                }
            }

            if (diskWrite != null) {
                if (diskWrite.length() > i) {
                    Double diskWriteValue = getDoubleOrDefault(diskWrite.getJSONArray(i).getString(0), 0D);
                    Integer offset = (int) (diskWrite.getJSONArray(i).getLong(1) - startTime);
                    containerMetricsList.get(offset).diskWriteRate = diskWriteValue;
                }
            }

            if (netRec != null) {
                if (netRec.length() > i) {
                    Double netRecValue = getDoubleOrDefault(netRec.getJSONArray(i).getString(0), 0D);
                    Integer offset = (int) (netRec.getJSONArray(i).getLong(1) - startTime);
                    containerMetricsList.get(offset).netRecRate = netRecValue;
                }
            }

            if( netTrans != null) {
                if (netTrans.length() > i) {
                    Double netTransValue = getDoubleOrDefault(netTrans.getJSONArray(i).getString(0), 0D);
                    Integer offset = (int) (netTrans.getJSONArray(i).getLong(1) - startTime);
                    containerMetricsList.get(offset).netTransRate = netTransValue;
                }
            }
        }
        return containerMetricsList;
    }

    // get the last piece of string in a string.
    private String getLastPiece(String path, String separator) {
        String[] dirs = path.split(separator);
        return dirs[dirs.length - 1];
    }

    private String getMetricName(String id) {
        String[] names = id.split("\\.");
        return names[names.length -2];
    }

    private Double getDoubleOrDefault(String str, Double defaultValue) {
        if(str.equals("null")) {
            return defaultValue;
        } else {
            return Double.parseDouble(str);
        }
    }

    private Long getLongOrDefault(String str, Long defaultValue) {
        if(str.equals("null")) {
            return defaultValue;
        } else {
            BigDecimal bigDecimal = new BigDecimal(str);

            return bigDecimal.longValueExact();
        }
    }
}
