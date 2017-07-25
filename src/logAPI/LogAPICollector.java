package logAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eddie on 2017/7/3.
 */
public class LogAPICollector {
    public List<MessageMark> allRuleMarkList = new ArrayList<>();

    private LogAPICollector(){}

    private static final LogAPICollector instance = new LogAPICollector();

    public static LogAPICollector getInstance() {
        return instance;
    }

    public void register(AbstractLogAPI api) {
        allRuleMarkList.addAll(api.messageMarkList);
    }

    public void clearAllAPI() {
        allRuleMarkList.clear();
    }

}
