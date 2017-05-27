package CsvUtils;

import info.ContainerMetrics;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.List;

/**
 * Created by Eddie on 2017/5/19.
 */
public class CsvWriter {
    String filePath;
    String fileName;

    public void setNewMetric(String newPath, String fileName) {
        this.filePath = newPath;
        this.fileName = fileName;
    }

    public CsvWriter() {}

    public void writeDataPoints(List<ContainerMetrics> metrics) {
        File dest = new File(filePath, fileName);
        if(!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        String titleLine = "time,CPU,exec mem,store mem,disk read,disk write,net rec, net trans";
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(dest, true));
            bw.write(titleLine);
            bw.newLine();
            for(int i = 0; i < metrics.size(); i++) {
                bw.write(buildLine(metrics.get(i)));
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildLine(ContainerMetrics metric) {
        String res;
        res = metric.timestamp.toString() + "," +
                metric.cpuUsage.toString() + "," +
                metric.execMemoryUsage.toString() + "," +
                metric.storeMemoryUsage.toString() + "," +
                metric.diskReadRate.toString() + "," +
                metric.diskWriteRate.toString() + "," +
                metric.netRecRate.toString() + "," +
                metric.netTransRate.toString();
        return res;
    }
}
