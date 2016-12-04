package application.logexceptions;

/**
 * Created by evgeni.shafran on 10/30/16.
 */
public class Log {
    private static final String LOG_TYPE_ERROR = "E";
    String time;
    String date;
    String logType;
    String applicationID;
    String processID;

    String tag;
    String log;

    String fullLog;

    public Log() {
    }

    public static Log logFromStringLog(String stringLog){
        Log log = new Log();

        String [] split = stringLog.split("\\s+");

        log.date = split[0];
        log.time = split[1];
        log.applicationID = split[2];
        log.processID = split[3];
        log.logType = split[4];

        int i = 5;
        for (; i < split.length; i++){
            log.tag += split[i] + " ";

            if (split[i].endsWith(":")){
                break;
            }
        }

        for (; i < split.length; i++){
            log.log += split[i] + " ";
        }

        log.tag = log.tag.trim();
        log.log = log.log.trim();

        log.fullLog = stringLog;

        return log;
    }

    @Override
    public String toString() {
        return "Log{" +
                "time='" + time + '\'' +
                ", date='" + date + '\'' +
                ", logType='" + logType + '\'' +
                ", applicationID='" + applicationID + '\'' +
                ", processID='" + processID + '\'' +
                ", tag='" + tag + '\'' +
                ", log='" + log + '\'' +
                '}';
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }


    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isError() {
        return logType.equals(LOG_TYPE_ERROR);
    }

    public boolean isExceptionStart() {
        return log.contains("Exception");
    }


    public String getFullLog() {
        return fullLog;
    }

    public void setFullLog(String fullLog) {
        this.fullLog = fullLog;
    }
}
