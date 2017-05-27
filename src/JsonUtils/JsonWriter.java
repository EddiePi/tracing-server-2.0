package JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by Eddie on 2017/5/18.
 */
public class JsonWriter {
    String filePath;
    String fileName;
    String metricName;
    JSONArray topJsonArray;
    JSONObject topJsonObject;
    JSONArray dataArray;

    public void setNewMetric(String newPath, String fileName, String metricName) {
        this.filePath = newPath;
        this.fileName = fileName;
        this.metricName = metricName;
        topJsonObject = new JSONObject();
        topJsonArray = new JSONArray();
        try {
            topJsonObject.put("target", metricName);
            dataArray = new JSONArray();
            topJsonObject.put("datapoints", dataArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        topJsonArray.put(topJsonObject);

    }

    public JsonWriter() {}

    public JSONObject getTopJsonObject() {
        return topJsonObject;
    }

    public String getMetricName() {
        String name = null;
        try {
            name =  topJsonObject.getString("target");
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return name;
        }
    }


    public void writeDataPoints(List<Double> metricValue, List<Long> timestamp) {
        for(int i = 0; i < metricValue.size(); i++) {
            JSONArray oneData = new JSONArray();
            oneData.put(metricValue.get(i));
            oneData.put(timestamp.get(i));
            dataArray.put(oneData);
        }
        try {
            writeFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //把json格式的字符串写到文件
    private void writeFile() throws IOException {
        File dest = new File(filePath, fileName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        PrintWriter out = new PrintWriter(dest);
        out.write(topJsonArray.toString());
        out.println();
        out.close();
    }
}
