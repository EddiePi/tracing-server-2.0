package JsonUtils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Eddie on 2017/4/17.
 */
public class AppConstructorTest {
    AppConstructor appConstructor;

    @Before
    public void setUp() throws Exception {
        appConstructor = new AppConstructor("./data/pagerank-huge");
    }

    @Test
    public void construct() throws Exception {
        appConstructor.construct();
    }

}