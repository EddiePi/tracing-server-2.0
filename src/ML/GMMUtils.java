package ML;

import java.util.ArrayList;
import Jama.Matrix;
/**
 * Created by Eddie on 2017/4/13.
 */
public class GMMUtils {

    public static double computeDistance(ArrayList<Double> d1, double[] d2) {
        double squareSum = 0;
        for(int i = 0; i < d1.size() - 1; i++) {
            squareSum += (d1.get(i) - d2[i]) * (d1.get(i) - d2[i]);
        }
        return Math.sqrt(squareSum);
    }

    public static double[][] computeCov(ArrayList<ArrayList<Double>> dataSet, int dataDimen) {
        int dataNum = dataSet.size();
        double res [][] = new double[dataDimen][dataDimen];

        double[] avg = new double[dataDimen];
        for(ArrayList<Double> data: dataSet) {
            for(int i = 0; i < dataDimen; i++) {
                avg[i] += data.get(i);
            }
        }
        for(int i = 0; i < dataDimen; i++) {
            avg[i] = avg[i] / dataNum;
        }

        // calculate the Covariance
        for(int i = 0; i < dataDimen; i++) {
            double[] tmp = new double[dataDimen];
            for(int j = 0; j < dataDimen; j++) {
                double cov = 0;
                for(ArrayList<Double> data: dataSet) {
                    cov += (data.get(i) - avg[i]) * (data.get(j) - avg[j]);
                }
                tmp[j] = cov;
            }
            res[i] = tmp;
        }
        return res;
    }

    public static double[] vectorMinus(double[] a1, double[] a2) {
        double[] res = new double[a1.length];
        for(int i = 0; i < a1.length; i++) {
            res[i] = a1[i] - a2[i];
        }
        return res;
    }

    public static double[] toVector(ArrayList<Double> a1) {
        double[] res = new double[a1.size()];
        for(int i = 0; i < a1.size(); i++) {
            res[i] = a1.get(i);
        }
        return res;
    }

    public static double[][] vectorToMatrix(double[] a) {
        double[][] res = new double[a.length][1];
        for(int i = 0; i < a.length; i++) {
            res[i][0] = a[i];
        }
        return res;
    }

    /**
     *
     * @Title: matrixSum
     * @Description: 返回矩阵每行之和(mark==2)或每列之和(mark==1)
     * @return ArrayList<Double>
     * @throws
     */
    public static double[] matrixSum(double[][] a, int mark) {
        double res[] = new double[a.length];
        if(mark == 1) {
            // 计算每列之和，返回行向量
            res = new double[a[0].length];
            for(int i = 0; i < a[0].length; i++) {
                for(int j = 0; j < a.length; j++) {
                    res[i] += a[j][i];
                }
            }
        } else if (mark == 2) {
            // 计算每行之和， 返回列向量
            for(int i = 0; i < a.length; i++) {
                for(int j = 0; j < a[0].length; j++) {

                    res[i] += a[i][j];
                }
            }
        }
        return res;
    }

    /**
     *
     * @Title: diag
     * @Description: 向量对角化
     * @return double[][]
     * @throws
     */
    public static double[][] diag(double[] a) {
        double[][] res = new double[a.length][a.length];
        for(int i = 0; i < a.length; i++) {
            for(int j = 0; j < a.length; j++) {
                if(i == j) {
                    res[i][j] = a[i];
                }
            }
        }
        return res;
    }

    public static double[][] diag(double a, int length) {
        double[][] res = new double[length][length];
        for(int i = 0; i < length; i++) {
            for(int j = 0; j < length; j++) {
                if(i == j) {
                    res[i][j] = a;
                }
            }
        }
        return res;
    }

    public static double[][] toArray(ArrayList<ArrayList<Double>> a) {
        int dataNum = a.size();
        if (dataNum == 0) {
            return null;
        }
        int dataDimen = a.get(0).size();
        double[][] res = new double[dataNum][dataDimen];
        for(int i = 0; i < dataNum; i++) {
            for(int j = 0; j < dataDimen; j++) {
                res[i][j] = a.get(i).get(j);
            }
        }
        return res;
    }

    public static double[][] toVMatrix(double[] a) {
        int dataDimen = a.length;
        double[][] res = new double[dataDimen][1];
        for(int i = 0; i < dataDimen; i++) {
            res[i][0] = a[i];
        }
        return res;
    }
}
