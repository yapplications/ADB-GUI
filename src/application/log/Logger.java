package application.log;

import javafx.application.Platform;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public interface LoggerListener {
		void onNewLogToShow(String message);
		void onNewErrorLogToShow(String message);
		void onFinishLogToShow(String message);
	}

	private static LoggerListener loggerListener;

	public static void ds (String message){
		d(message);

		if (loggerListener != null){
			Platform.runLater(new Runnable() {
	            @Override public void run() {
	    			loggerListener.onNewLogToShow(message);
	            }
	        });
		}
	}

	public static void d(String message){
		System.out.println(getLogText(message));
	}

	public static void setShowLogListener(LoggerListener loggerListener) {
		Logger.loggerListener = loggerListener;
	}

	public static void es(String message) {
		e(message);

		if (loggerListener != null){
			Platform.runLater(new Runnable() {
	            @Override public void run() {
	    			loggerListener.onNewErrorLogToShow(message);
	            }
	        });
		}
	}

	public static void e(String message) {
		System.err.println(getLogText(message));
	}

	public static void fs(String message) {
		d(message);

		if (loggerListener != null){
			Platform.runLater(new Runnable() {
	            @Override public void run() {
	    			loggerListener.onFinishLogToShow(message);
	            }
	        });
		}
	}

	public static void fes(String error, String success) {
		if (error == null){
			fs(success);
		} else {
			es(error);
		}
	}

	static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");

	public static String getLogText(String text){
		String time = simpleDateFormat.format(new Date());
		return time + " " + text;
	}
}

