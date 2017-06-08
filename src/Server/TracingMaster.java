package Server;

import log.AppLogReader;
import log.LogReaderManager;
import org.apache.log4j.BasicConfigurator;

import java.util.Map;

/**
 * Created by Eddie on 2017/1/23.
 */
public class TracingMaster {
    public static void main(String argv[]) throws Exception {
        BasicConfigurator.configure();
        //Tracer tracer = Tracer.getInstance();
        //tracer.init();
        System.out.print("new tracing master\n");
        LogReaderManager logReader = new LogReaderManager();
        logReader.start();
    }
}
