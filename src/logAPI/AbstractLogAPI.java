package logAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Eddie on 2017/7/1.
 */
public abstract class AbstractLogAPI {
    List<MessageMark> messageMarkList;

    public AbstractLogAPI() {
        messageMarkList = new ArrayList<>();
    }

    public class MessageMark {
        public String objName;
        public String messageRegex;
        // positive number means counting from left to right.
        // negative number means counting from right to left.
        public String[] offset;
    }
}
