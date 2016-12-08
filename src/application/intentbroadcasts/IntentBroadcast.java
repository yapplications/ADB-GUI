package application.intentbroadcasts;

/**
 * Created by evgeni.shafran on 11/15/16.
 */
public class IntentBroadcast {

    public static final String ACTIVITY_MANAGER_COMMAND_START = "start";
    public static final String ACTIVITY_MANAGER_COMMAND_BROADCAST = "broadcast";
    public static final String ACTIVITY_MANAGER_COMMAND_START_SERVICE = "startservice";

    public String activityManagerCommand = "broadcast";
    public String action;
    public String promptData = "";
    public String data = "";
    public String mimeType = "";
    public String category = "";
    public String component = "";

    @Override
    public String toString() {
        return "IntentBroadcast{" +
                "activityManagerCommand='" + activityManagerCommand + '\'' +
                ", action='" + action + '\'' +
                ", promptData='" + promptData + '\'' +
                ", data='" + data + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", category='" + category + '\'' +
                ", component='" + component + '\'' +
                '}';
    }
}
