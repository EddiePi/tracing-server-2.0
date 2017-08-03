package tsdb;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eddie on 2017/8/2.
 */
public class ContainerState {
    public static final HashMap<String, Integer> stateMap = new HashMap<String, Integer>(){
        {
            put("NEW", 1);
            put("LOCALIZING", 2);
            put("LOCALIZED", 3);
            put("RUNNING", 4);
            put("KILLING", 5);
            put("EXITED_WITH_SUCCESS", 6);
            put("DONE", 7);
            put("LOCALIZATION_FAILED", 8);
            put("CONTAINER_CLEANEDUP_AFTER_KILL", 9);
            put("EXITED_WITH_FAILURE", 10);
        }
    };

}
