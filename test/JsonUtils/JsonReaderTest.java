package JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Eddie on 2017/4/17.
 */
public class JsonReaderTest {
    JsonReader jsonReader;
    @Before
    public void setUp() throws Exception {
        jsonReader = new JsonReader("./test/test.json");
    }

    @Test
    public void getTopJsonObject() throws Exception {
        JSONObject top = jsonReader.getTopJsonObject();
        System.out.print(top.toString() + "\n");
    }

    @Test
    public void getMetricName() throws Exception {
        String name = jsonReader.getMetricName();
        System.out.print(name + "\n");
    }

    @Test
    public void getData() throws Exception {
        JSONArray data = jsonReader.getData();
        JSONArray piece;
        String value;
        Long time;
        for(int i = 0; i < data.length(); i++) {
            piece = data.getJSONArray(i);
            value = piece.getString(0);
            time = piece.getLong(1);
            System.out.print("value: " + value + " time: " + time + "\n");
        }
    }

}