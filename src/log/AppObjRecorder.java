package log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Eddie on 2017/7/3.
 */
public class AppObjRecorder {
    private static final AppObjRecorder instance = new AppObjRecorder();

    private AppObjRecorder(){}

    public static AppObjRecorder getInstance() {
        return instance;
    }

    private ConcurrentMap<Long, Map<String, List<ObjInfo>>> timestampInfoMap = new ConcurrentHashMap<>();

    public void putInfo(Long timestamp, String name, String value) {

    }

    public ObjInfo getInfo(String containerId, Long timestamp) {
        ObjInfo res = null;


        return res;
    }


    private class ObjInfo {
        String name;
        String info;

        public ObjInfo(String name, String info) {
            this.name = name;
            this.info = info;
        }
    }
}
