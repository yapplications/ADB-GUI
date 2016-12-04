package application.intentbroadcasts;

/**
 * Created by evgeni.shafran on 11/15/16.
 */
public class IntentBroadcast {
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
