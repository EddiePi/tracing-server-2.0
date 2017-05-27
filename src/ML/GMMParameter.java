package ML;

import java.io.Serializable;

/**
 * Created by Eddie on 2017/4/13.
 */
public class GMMParameter implements Serializable {
    private double[][] pMiu;
    private double[] pPi;
    private double[][][] pSigma;

    public double[][] getpMiu() {
        return pMiu;
    }

    public void setpMiu(double[][] pMiu) {
        this.pMiu = pMiu;
    }

    public double[] getpPi() {
        return pPi;
    }

    public void setpPi(double[] pPi) {
        this.pPi = pPi;
    }

    public double[][][] getpSigma() {
        return pSigma;
    }

    public void setpSigma(double[][][] pSigma) {
        this.pSigma = pSigma;
    }
}
