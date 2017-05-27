package Server;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Eddie on 2017/4/18.
 */
public class AnalyzerTest {
    Analyzer analyzer;
    @Before
    public void setUp() throws Exception {
        analyzer = new Analyzer(true);
        analyzer.addFileAppToClassify("./data/pagerank-huge");
        analyzer.addFileAppToClassify("./data/kmeans-huge");
    }

//    @Test
//    public void addFileAppToTraining() throws Exception {
//
//    }
//
//    @Test
//    public void addAllMemoryAppToTraining() throws Exception {
//
//    }

    @Test
    public void classifyTest() throws Exception {
        System.out.print("classify test\n");
        analyzer.classify();
    }

}