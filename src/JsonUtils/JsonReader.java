package JsonUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import Utils.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Eddie on 2017/4/14.
 */
public class JsonReader {
    String path;
    JSONArray topJsonArray;
    JSONObject topJsonObject;

    public void setNewPath(String newPath) {
        this.path = newPath;
        try {
            topJsonArray = new JSONArray(readFile());
            if (topJsonArray.length() > 0) {
                topJsonObject = topJsonArray.getJSONObject(0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JsonReader() {}

    public JsonReader(String path) {
        this.path = path;
        try {
            topJsonArray = new JSONArray(readFile());
            if (topJsonArray.length() > 0) {
                topJsonObject = topJsonArray.getJSONObject(0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

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

    public JSONArray getData() {
        JSONArray dataArray;
        dataArray = dataTrim();
        return dataArray;
    }

    public JSONArray dataTrim() {
        JSONArray res = null;
        try {
            res = topJsonObject.getJSONArray("datapoints");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        if (res == null || res.length() < 1) {
            return null;
        }
        try {
            JSONArray tmp;
            String value;
            Boolean needWrite = false;
            while(true) {
                tmp = res.getJSONArray(0);
                value = tmp.getString(0);
                if (value.equals("null")) {
                    if (!needWrite) {
                        needWrite = true;
                    }
                    res.remove(0);
                    continue;
                } else {
                    break;
                }
            }
            for(int index = res.length() - 1; index >= 0; index--) {
                tmp = res.getJSONArray(index);
                value = tmp.getString(0);
                if (value.equals("null")) {
                    if (!needWrite) {
                        needWrite = true;
                    }
                    res.remove(index);
                    continue;
                } else {
                    break;
                }
            }
            if(needWrite) {
                writeFile();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return res;
        }
    }

    private String readFile() {
        String lastStr = "";
        try {
            List<String> strings = Utils.FileReader.read(path);
            for(String str: strings) {
                lastStr = lastStr + str;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lastStr;
    }

    //把json格式的字符串写到文件
    private void writeFile() throws IOException {
        FileWriter fw = new FileWriter(path);
        PrintWriter out = new PrintWriter(fw);
        out.write(topJsonArray.toString());
        out.println();
        fw.close();
        out.close();
    }
}
