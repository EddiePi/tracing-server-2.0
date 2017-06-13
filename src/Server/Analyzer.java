package Server;

import JsonUtils.AppConstructor;
import ML.GMMAlgorithm;
import ML.GMMParameter;
import Utils.ObjPersistent;
import info.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eddie on 2017/4/14.
 */
//TODO this class now is only in test package. This algorithm works like shit
public class Analyzer {
    TracerConf conf;
    String GMMParameterPath;
    String simpleParameterPath;
    List<ContainerMetrics> metricsToAnalyzeBuffer = new ArrayList<>();
    List<ContainerMetrics> metricsInAnalysis = new ArrayList<>();
    boolean readParameter = false;
    GMMAlgorithm classifier;
    Long classifyInterval;
    Thread analysisThread;
    String analysisMode;
    boolean isRunning;

    // parameter related to GMM
    GMMParameter GMMparameter;
    // parameter related to simple analysis
    Double upperThreshold;
    Double lowerThreshold;
    SimpleParameter simpleParameter;

    private class classifierRunnable implements Runnable {

        @Override
        public void run() {
            while(isRunning) {
                classify();
                try {
                    Thread.sleep(classifyInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class simpleAnalysisRunnable implements Runnable {

        @Override
        public void run() {
            while(isRunning) {
                simpleAnalysis();
            }
            try {
                Thread.sleep(classifyInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public Analyzer(boolean readParameter) {
        this.conf = TracerConf.getInstance();
        GMMParameterPath = conf.getStringOrDefault("tracer.ML.parameter.path", "./GMMParameter");
        simpleParameterPath = conf.getStringOrDefault("tracer.simple.parameter.path", "./simpleParameter");
        analysisMode = conf.getStringOrDefault("tracer.analysis-mode", "simple");
        this.readParameter = readParameter;
        classifyInterval = (long)conf.getIntegerOrDefault("tracer.ML.classify-interval", 5000);
        if (analysisMode.equals("simple")) {
            analysisThread = new Thread(new simpleAnalysisRunnable());
            upperThreshold = conf.getDoubleOrDefault("tracer.simple.parameter.upperThreshold", 0.8);
            lowerThreshold = conf.getDoubleOrDefault("tracer.simple.parameter.upperThreshold", 0.1);
            simpleParameter = (SimpleParameter) ObjPersistent.readObject(simpleParameterPath);
            if (simpleParameter == null) {
                simpleParameter = new SimpleParameter();
                ObjPersistent.saveObject(simpleParameter, simpleParameterPath);
            }

        } else {
            analysisThread = new Thread(new classifierRunnable());
            if(readParameter) {
                GMMparameter = (GMMParameter) ObjPersistent.readObject(GMMParameterPath);
            }
        }
    }

    public void start() {
        isRunning = true;
        analysisThread.run();
    }

    public void stop() {
        isRunning = false;
    }

    public void simpleAnalysis() {
        ArrayList<ArrayList<Double>> currentDataSet = buildDataInAnalysis();
        if(currentDataSet == null) {
            return;
        }
        if(readParameter) {
            int i = 0;
            List<Integer> overIndex = new ArrayList<>();
            List<Integer> underIndex = new ArrayList<>();
            for(ArrayList<Double> data: currentDataSet) {
                Double lowerSum = 0D;
                for(int j = 0; j < data.size(); j++) {
                    if(data.get(j) > simpleParameter.maxUsage[j] * upperThreshold) {
                        overIndex.add(i);
                    }
                    lowerSum += data.get(j) / simpleParameter.maxUsage[j];
                }
                if (lowerSum > 0.001D && lowerSum < lowerThreshold) {
                    underIndex.add(i);
                }
                i++;
            }
            if(overIndex.size() > 0) {
                System.out.print("over usage anomalies: \n");
                printAnomalyInfo(overIndex);
            }
            if(underIndex.size() > 0) {
                System.out.print("under usage anomalies: \n");
                printAnomalyInfo(underIndex);
            }
        } else {
            for(ArrayList<Double> data: currentDataSet) {
                for(int i = 1; i < 6; i++) {
                    if(simpleParameter.maxUsage[i] < data.get(i)) {
                        simpleParameter.maxUsage[i] = data.get(i);
                    }
                }
            }
            ObjPersistent.saveObject(simpleParameter, simpleParameterPath);
        }
        metricsInAnalysis.clear();
    }

    // public for test
    public void classify() {
        ArrayList<ArrayList<Double>> currentDataSet = buildDataInAnalysis();
        if (currentDataSet == null) {
            return;
        }
        if (readParameter) {
            classifier = new GMMAlgorithm(currentDataSet, GMMparameter);
        } else {
            classifier = new GMMAlgorithm(currentDataSet, true);
        }
        List<Boolean> anomalyList;
        anomalyList = classifier.getAnomalies();
        List<Integer> anomalyIndex = new ArrayList<>();
        for(int i = 0; i < anomalyList.size(); i++) {
            if (anomalyList.get(i)) {
                anomalyIndex.add(i);
            }
        }
        printAnomalyInfo(anomalyIndex);

        metricsInAnalysis.clear();
    }

    // this method is called by Tracer periodically
    public void addDataToAnalyze(ContainerMetrics containerMetrics) {
        synchronized (metricsToAnalyzeBuffer) {
            metricsToAnalyzeBuffer.add(containerMetrics);
        }
    }

    private ArrayList<ArrayList<Double>> buildDataInAnalysis() {
        synchronized (metricsToAnalyzeBuffer) {
            metricsInAnalysis.addAll(metricsToAnalyzeBuffer);
            metricsToAnalyzeBuffer.clear();
        }
        if (metricsInAnalysis.size() == 0) {
            return null;
        }
        ArrayList<ArrayList<Double>> dataSet = new ArrayList<>();
        for(ContainerMetrics metrics: metricsInAnalysis) {
            dataSet.add(formatMetrics(metrics));
        }
        return dataSet;
    }

    private ArrayList<ArrayList<Double>> formatApp(App app) {
        ArrayList<ArrayList<Double>> dataSet = new ArrayList<>();
        for(Job job: app.getAllJobs()) {
            for(Stage stage: job.getAllStage()) {
                for(List<ContainerMetrics> metricList: stage.containerMetricsMap.values()) {
                    for(ContainerMetrics metrics: metricList) {
                        ArrayList<Double> data = formatMetrics(metrics);
                        dataSet.add(data);
                    }
                }
            }
        }
        return dataSet;
    }

    private ArrayList<Double> formatMetrics(Metrics metrics) {
        ArrayList<Double> data = new ArrayList<>();
        data.add(metrics.cpuUsage);
        data.add((double)(metrics.storeMemoryUsage + metrics.execMemoryUsage));
        data.add(metrics.diskReadRate);
        data.add(metrics.diskWriteRate);
        data.add(metrics.netRecRate);
        data.add(metrics.netTransRate);
        return data;
    }

    public static void printParameter(GMMParameter parameter) {
        int category = parameter.getpPi().length;
        int dime = parameter.getpMiu()[0].length;
        for(int i = 0; i < category; i++) {
            double[] miu = parameter.getpMiu()[i];
            System.out.print(String.format("pi: %f\n", parameter.getpPi()[i]));
            System.out.print(String.format("miu: %.4f, %.4f, %.4f, %.4f\n", miu[0], miu[1], miu[2], miu[3]));
            System.out.print("sigma: \n");
            double[][] sigma = parameter.getpSigma()[i];
            for(int j = 0; j < dime; j++) {
                System.out.print(String.format("%.4f\t%.4f\t%.4f\t%.4f\n", sigma[j][0], sigma[j][1], sigma[j][2], sigma[j][3]));
            }
            System.out.print("\n");
        }
    }

    private void printAnomalyInfo(List<Integer> index) {
        for(int i = 0; i < index.size(); i++) {
            ContainerMetrics anomaly = metricsInAnalysis.get(index.get(i));
            System.out.print("anomalyId: " + anomaly.getFullId() + "\n");
        }
    }

    // this method is only used for TEST
    public void addFileAppToClassify(String path) {
        App app = AppConstructor.getApp(path);
        for(Job job: app.getAllJobs()) {
            for(Stage stage: job.getAllStage()) {
                for(List<ContainerMetrics> metricList: stage.containerMetricsMap.values()) {
                    metricsInAnalysis.addAll(metricList);
                }
            }
        }

    }
}
