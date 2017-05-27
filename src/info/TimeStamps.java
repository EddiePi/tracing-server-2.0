package info;


/**
 * Created by Eddie on 2017/1/25.
 */
public class TimeStamps implements Cloneable {
    Long startTimeMillis;
    Long finishTimeMillis;

    public TimeStamps clone() {
        TimeStamps timeStampsClone = new TimeStamps();
        timeStampsClone.startTimeMillis = this.startTimeMillis;
        timeStampsClone.finishTimeMillis = this.finishTimeMillis;
        return timeStampsClone;
    }
}
