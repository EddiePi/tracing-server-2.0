package ML;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created by Eddie on 2017/4/14.
 */
public class GMMAlgorithmTest {
    GMMAlgorithm gmmAlgorithm;
    @Before
    public void setUp() throws Exception {
        List<List<Double>> dataSet = new ArrayList<List<Double>>();
        Random random = new Random();
        for(int i = 0; i < 20; i++) {
            dataSet.add(new ArrayList<>(Arrays.asList(
                    random.nextDouble()*3,random.nextDouble()*3,random.nextDouble()*3+7,random.nextDouble()*3+9)));
        }
        for(int i = 0; i < 10; i++) {
            dataSet.add(new ArrayList<>(Arrays.asList(
                    random.nextDouble()*2+3,random.nextDouble()*2+3,random.nextDouble()*2+3,random.nextDouble()*2+3)));
        }
        for(int i = 0; i < 20; i++) {
            dataSet.add(new ArrayList<>(Arrays.asList(
                    random.nextDouble()*3+7,random.nextDouble()*3+8,random.nextDouble()*3,random.nextDouble()*3)));
        }
        ArrayList<ArrayList<Double>> initMiu = new ArrayList<>();
        ArrayList<Double> miu1 = new ArrayList<>(Arrays.asList(-1.5, -1.5, 10.5, 12.5));
        ArrayList<Double> miu2 = new ArrayList<>(Arrays.asList(10.5, 12.5, -1.5, -1.5));
        initMiu.add(miu1);
        initMiu.add(miu2);
        gmmAlgorithm = new GMMAlgorithm((ArrayList)dataSet, GMMUtils.toArray(initMiu));
    }

    @Test
    public void TestGMMAlgorithm() {
        List<Boolean> result;
        result = gmmAlgorithm.cluster();
        int index = 0;
        for(Boolean cate: result) {
            System.out.print(String.format("line: %d is anomaly: %b\n", index, cate));
            index++;
        }
    }
}