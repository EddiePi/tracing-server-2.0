package ML;

import Jama.Matrix;
import Server.TracerConf;

import java.util.ArrayList;
import java.util.List;


// TODO this is the basic GMM algorithm. we need to modify it to analyze the tracing data.
public class GMMAlgorithm {
    int dataNum;
    int k;
    int dataDimen;
    GMMParameter parameter;
    ArrayList<ArrayList<Double>> dataSet;
    TracerConf conf = TracerConf.getInstance();
    private double anomalyThreshold = conf.getDoubleOrDefault("tracer.ML.anomaly.possibility-threshold", 0.5);

    public GMMAlgorithm(ArrayList<ArrayList<Double>> dataSet, double[][] initMiu) {
        this(dataSet, false);
        this.k = initMiu.length;
        this.parameter = initParameter(initMiu);
    }

    public GMMAlgorithm(ArrayList<ArrayList<Double>> dataSet, GMMParameter parameter) {
        this(dataSet, false);
        this.parameter = parameter;
        this.k = parameter.getpMiu().length;
    }

    public GMMAlgorithm(ArrayList<ArrayList<Double>> dataSet, boolean initPara) {
        this.dataSet = dataSet;
        this.dataNum = dataSet.size();
        this.dataDimen = dataSet.get(0).size();
        normalize_MaxMin();
        if (initPara) {
            double[][] initMiu = initializeMiu();
            this.k = initMiu.length;
            parameter = initParameter(initMiu);
        }
    }

    public void setAnomalyThreshold(double threshold) {
        this.anomalyThreshold = threshold;
    }

    public List<Boolean> getAnomalies() {
        double[][] px = computeProbability();
        List<Boolean> result = new ArrayList<>();
        for(int i = 0; i < px.length; i++) {
            Double maxProp = -1D;
            Boolean isAnomaly = false;
            for(int j = 0; j < px[i].length; j++) {
                if(px[i][j] > maxProp) {
                    maxProp = px[i][j];
                }
            }
            if (maxProp < anomalyThreshold) {
                isAnomaly = true;
            }
            result.add(isAnomaly);
        }
        return result;
    }

    public List<Boolean> cluster() {
        double Lpre = -Double.MAX_VALUE;
        double threshold = 0.0001;
        while(true) {
            double[][] px = computeProbability();

            // E-step
            double[][] pGama = new double[dataNum][k];
            for(int i = 0; i < dataNum; i++) {
                for(int j = 0; j < k; j++) {
                    pGama[i][j] = px[i][j] * parameter.getpPi()[j];
                }
            }
            double[] sumpGama = GMMUtils.matrixSum(pGama, 2);
            for(int i = 0; i < dataNum; i++) {
                for(int j = 0; j < k; j++) {
                    pGama[i][j] = pGama[i][j] / sumpGama[i];
                    if(Double.isNaN(pGama[i][j])) {
                        pGama[i][j] = 1E-20;
                    }
                }
            }

            // M-step
            // calculate miu
            double[] NK = GMMUtils.matrixSum(pGama, 1);
            double[] NKReciprocal = new double[NK.length];
            for(int i = 0; i < NK.length; i++) {
                NKReciprocal[i] = 1 / NK[i];
            }
            double[][] pMiu = new Matrix(GMMUtils.diag(NKReciprocal)).
                    times(new Matrix(pGama).transpose()).
                    times(new Matrix(GMMUtils.toArray(dataSet))).getArray();
            parameter.setpMiu(pMiu);

            // calculate pPi (vector)
            double[] pPi = new double[k];
            for(int i = 0; i < NK.length; i++) {
                if(NK[i] == 0) {
                    NK[i] += 1E-20;
                }
                pPi[i] = NK[i] / dataNum;
            }
            parameter.setpPi(pPi);

            // calculate pSigma
            double[][][] pSigma = new double[k][dataDimen][dataDimen];
            Matrix[] pSigmaList = new Matrix[k];
            for(int i = 0; i < k; i++) {
                double[][] offset = new double[dataNum][dataDimen];
                pSigmaList[i] = new Matrix(new double[dataDimen][dataDimen]);
                for(int j = 0; j < dataNum; j++) {
                    for(int l = 0; l < dataDimen; l++) {
                        offset[j][l] = dataSet.get(j).get(l) - pMiu[i][l];
                    }
                    Matrix offsetJ = new Matrix(GMMUtils.toVMatrix(offset[j]));
                    Matrix tmpRhs1 = offsetJ.times(offsetJ.transpose());
                    tmpRhs1.timesEquals(pGama[j][i]);
                    pSigmaList[i].plusEquals(tmpRhs1);
                }
                pSigmaList[i].timesEquals(NKReciprocal[i]);
                pSigma[i] = pSigmaList[i].getArray();
            }
            parameter.setpSigma(pSigma);

            // check if convergence
            double[][] a = new Matrix(px).times(new Matrix(GMMUtils.vectorToMatrix(pPi))).getArray();
            for(int i = 0; i < dataNum; i++) {

                a[i][0] = Math.log(a[i][0]);

            }
            double L = GMMUtils.matrixSum(a, 1)[0];

            if(L - Lpre < threshold) {

                break;

            }
            Lpre = L;
        }
        double[][] px = computeProbability();
        List<Boolean> result = new ArrayList<>();
        for(int i = 0; i < px.length; i++) {
            Double maxProp = -1D;
            Boolean isAnomaly = false;
            for(int j = 0; j < px[i].length; j++) {
                if(px[i][j] > maxProp) {
                    maxProp = px[i][j];
                }
            }
            if (maxProp < anomalyThreshold) {
                isAnomaly = true;
            }
            result.add(isAnomaly);
        }

        return result;
    }

    //TODO: this method is under construction. now it only generate 2 classes
    private double[][] initializeMiu() {
        double[][] initMiu = new double[2][dataDimen];
        for(ArrayList<Double> data: dataSet) {
            for(int i = 0; i < dataDimen; i++) {
                initMiu[0][i] = Math.max(initMiu[0][i], data.get(i));
            }
        }
        for(int i = 0; i < dataDimen; i++) {
            initMiu[1][i] = 0;
        }
        return initMiu;
    }

    private GMMParameter initParameter(double[][] initMiu) {
        GMMParameter res = new GMMParameter();
        res.setpMiu(initMiu);

        double[] pPi = new double[k];
        int[] type = getTypes(initMiu);
        int[] typeNum = new int[k];
        for(int i = 0; i < dataNum; i++) {
            typeNum[type[i]]++;
        }

        // initialize pPi
        for(int i = 0; i < k; i++) {
            pPi[i] = (typeNum[i] / (double)dataNum);
            //pPi[i] = 1 / k;
        }
        res.setpPi(pPi);

        //initialize pSigma
        double[][][] pSigma = new double[k][dataDimen][dataDimen];
        for(int i = 0; i < k; i++) {
            ArrayList<ArrayList<Double>> tmp = new ArrayList<ArrayList<Double>>();
            for(int j = 0; j < dataNum; j++) {
                if(type[j] == i) {
                    tmp.add(dataSet.get(j));
                }
            }
            pSigma[i] = GMMUtils.computeCov(tmp, dataDimen);
        }
        res.setpSigma(pSigma);

        return res;
    }

    private double[][] computeProbability() {
        double[][] px = new double[dataNum][k];
        double[][] pMiu = parameter.getpMiu();
        double[][][] covList = parameter.getpSigma();

        // calculate the probability for each cluster
        for(int i = 0; i < dataNum; i++) {
            for(int j = 0; j < k; j++) {
                double[] offset;
                offset = GMMUtils.vectorMinus(GMMUtils.toVector(dataSet.get(i)), pMiu[j]);
                Matrix cov = new Matrix(covList[j]);
                Matrix alphaI = new Matrix(GMMUtils.diag(0.000001, dataDimen));
                cov.plusEquals(alphaI);
                Matrix offsetM = new Matrix(GMMUtils.vectorToMatrix(offset));
                Matrix invSigma = cov.inverse();
                Matrix tmp1 = offsetM.transpose().times(invSigma);
                Matrix tmp2 = tmp1.times(offsetM);
                double det = cov.det();
                if (det < 0) {
                    det = -det;
                }
                double pi = Math.pow((2 * Math.PI), -(double)dataDimen / 2d);
                double coef = pi / Math.sqrt(det);
                px[i][j] = coef * Math.pow(Math.E, -0.5 * tmp2.get(0, 0));
                if(Double.isNaN(px[i][j])) {
                    System.out.print("px is not a number");
                }
            }
        }
        return px;
    }

    private int[] getTypes(double[][] miu) {
        int[] type = new int[dataNum];
        for(int j = 0; j < dataNum; j++) {
            double minDistance = GMMUtils.computeDistance(dataSet.get(j), miu[0]);
            for(int i = 1; i < k; i++) {
                double tmpDistance = GMMUtils.computeDistance(dataSet.get(j), miu[i]);
                if (tmpDistance < minDistance) {
                    minDistance = tmpDistance;
                    type[j] = i;
                }
            }
        }
        return type;
    }

    private void normalize_MaxMin() {
        double[] maxValue = new double[dataDimen];
        for(ArrayList<Double> data: dataSet) {
            for(int i = 1; i < dataDimen; i++) {
                if(maxValue[i] < data.get(i)) {
                    maxValue[i] = data.get(i);
                }
            }
        }
        for(int i = 1; i < dataDimen; i++) {
            if(maxValue[i] == 0) {
                continue;
            }
            for(ArrayList<Double> data: dataSet) {
                data.set(i, data.get(i) / maxValue[i]);
            }
        }
    }

    //TODO
    private void normalize_ZScore() {

    }

    public GMMParameter getParameter() {
        return parameter;
    }
}
