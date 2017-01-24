package application;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by evgeni.shafran on 10/13/16.
 */
public class DateUtil {

    public static String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(new Date());
    }

}
