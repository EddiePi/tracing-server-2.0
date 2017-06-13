package Server;

import org.apache.log4j.BasicConfigurator;


/**
 * Created by Eddie on 2017/1/23.
 */
public class TracingMaster {
    public static void main(String argv[]) throws Exception {
        BasicConfigurator.configure();
        Tracer tracer = Tracer.getInstance();
        tracer.init();
    }
}
