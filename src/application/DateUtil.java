package application;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by evgeni.shafran on 10/13/16.
 */
public class DateUtil {

    public static String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }

}
