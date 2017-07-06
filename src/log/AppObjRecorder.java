package log;

import logAPI.LogAPICollector;
import logAPI.MessageMark;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by Eddie on 2017/7/3.
 */
public class AppObjRecorder {
    List<MessageMark> allMarks;

    private static final AppObjRecorder instance = new AppObjRecorder();

    private AppObjRecorder(){
        LogAPICollector collector = LogAPICollector.getInstance();
        allMarks = collector.getAllRuleMarks();
    }

    public static AppObjRecorder getInstance() {
        return instance;
    }

    private ConcurrentMap<String, Map<String ,ObjInfoWithTimestamp>> timestampInfoMap = new ConcurrentHashMap<>();

    public void maybeUpdateInfo(String containerId, String message) {
        for(MessageMark mark: allMarks) {
            Pattern pattern = Pattern.compile(mark.regex);
            Matcher matcher = pattern.matcher(message);
            if(matcher.matches()) {
                System.out.printf("app log message is matched: %s.\n", message);
                if(matcher.groupCount() >= 1) {

                    String value = matcher.group(1);
                    String[] words = message.split("\\s+");
                    Long timestamp = Timestamp.valueOf(words[0] + " " + words[1].replace(',', '.')).getTime();
                    System.out.printf("going to update app info. name: %s, value: %s, isFinish: %b\n",
                            mark.name, value, mark.isFinishMark);
                    updateInfo(containerId, mark.name, value, timestamp, mark.isFinishMark);
                }
            }
        }
    }

    public void updateInfo(String containerId, String name, String value, Long timestamp, boolean isFinish) {
        Map<String, ObjInfoWithTimestamp> objInfoWithTimestampMap =
                timestampInfoMap.getOrDefault(containerId, new HashMap<>());
        if(isFinish) {
            objInfoWithTimestampMap.remove(name);
        } else {
            ObjInfoWithTimestamp objInfoWithTimestamp = new ObjInfoWithTimestamp(name, value, timestamp);
            objInfoWithTimestampMap.put(name, objInfoWithTimestamp);
        }
    }

    public List<String> getInfo(String containerId, Long timestamp) {
        List<String> res;
        Map<String, ObjInfoWithTimestamp> objInfoWithTimestampMap =
                timestampInfoMap.getOrDefault(containerId, null);
        if (objInfoWithTimestampMap == null) {
            return null;
        }
        res = new ArrayList<>();
        for(Map.Entry<String, ObjInfoWithTimestamp> entry: objInfoWithTimestampMap.entrySet()) {
            ObjInfoWithTimestamp value = entry.getValue();
            if(value.timestamp <= timestamp) {
                res.add(value.name + ":" + value.info);
            }
        }
        return res;
    }


    private class ObjInfoWithTimestamp {
        Long timestamp;
        String name;
        String info;

        public ObjInfoWithTimestamp(String name, String info, Long timestamp) {
            this.timestamp = timestamp;
            this.name = name;
            this.info = info;
        }
    }
}
