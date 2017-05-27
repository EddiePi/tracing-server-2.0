package Server;

import ML.GMMAlgorithm;
import ML.GMMUtils;
import RPCService.TracingService;
import RPCService.TracingServiceImpl;
import org.apache.log4j.BasicConfigurator;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
