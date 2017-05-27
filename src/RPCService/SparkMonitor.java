package RPCService;

import Server.Tracer;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;

/**
 * Created by Eddie on 2017/2/21.
 */
public class SparkMonitor {
    private class MonitorRunnable implements Runnable {
        @Override
        public void run() {
            try {
                TProcessor tprocessor = new TracingService.Processor<TracingService.Iface>
                        (new TracingServiceImpl());

                TServerSocket serverTransport = new TServerSocket(8089);
                TThreadPoolServer.Args tArgs = new TThreadPoolServer.Args(serverTransport);
                tArgs.processor(tprocessor);
                tArgs.protocolFactory(new TBinaryProtocol.Factory());
                // tArgs.protocolFactory(new TCompactProtocol.Factory());
                // tArgs.protocolFactory(new TJSONProtocol.Factory());
                TServer server = new TThreadPoolServer(tArgs);
                server.serve();

            } catch (Exception e) {
                System.out.println("Spark tracing component start error!");
                e.printStackTrace();
            }
        }
    }

    MonitorRunnable runnable = new MonitorRunnable();
    Thread monitorThread = new Thread(runnable);
    public void startServer() {
        System.out.print("starting spark monitor...\n");
        monitorThread.start();
    }
}
