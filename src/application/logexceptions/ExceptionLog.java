package application.logexceptions;

import java.util.ArrayList;

/**
 * Created by evgeni.shafran on 10/30/16.
 */
public class ExceptionLog {

    ArrayList<Log> logs = new ArrayList<>();
    String exceptionTime;
    String exceptionDate;
    String exceptionProcessID;

    public ExceptionLog(Log log) {
        logs.add(log);

        exceptionDate = log.date;
        exceptionTime = log.time;
        exceptionProcessID = log.processID;
    }

    public void addLog(Log log){
        logs.add(log);
    }

    public boolean isPartOfRxception(Log log) {
        return log.isError() && log.getProcessID().equals(exceptionProcessID) && log.getTime().equals(exceptionTime);
    }
}
