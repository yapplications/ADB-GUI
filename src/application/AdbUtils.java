package application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.*;

import application.preferences.Preferences;
import javafx.application.Platform;
import application.log.Logger;
import application.model.Command;
import application.model.CommandBatch;
import application.model.Device;
import application.model.Model;

public class AdbUtils {
	//public static ExecutorService executor = Executors.newSingleThreadExecutor();
	public static ExecutorService executor = Executors.newScheduledThreadPool(2);
	public static ExecutorService executorTimeout = Executors.newScheduledThreadPool(2);

	public interface TimeCallListener{
		void timeout();
		void error(Exception e);
	}

	public static <T> T timedCall(Callable<T> c, long timeout, TimeUnit timeUnit, final TimeCallListener timeCallListener) {
		FutureTask<T> task = new FutureTask<T>(c);
		executor.execute(task);
		executorTimeout.execute(new Runnable() {
			@Override
			public void run() {
				try {
					task.get(timeout, timeUnit);
				} catch (InterruptedException e) {
					timeCallListener.error(e);
				} catch (ExecutionException e) {
					timeCallListener.error(e);
				} catch (TimeoutException e) {
					timeCallListener.timeout();
				}
			}
		});
		return null;
	}


	public static String run(CommandBatch batch) {
		String result = "";

		for (Command command : batch.commands) {
			result += executeADBCommand(command.command) + "\n";
		}

		return result;
	}

	private static String executeADBCommand(String command) {
		return executeCommand(getAdbCommand(command));
	}

	public static String getAdbCommand(String command) {
		if (command.startsWith("adb")){
			command = command.replaceFirst("adb", "");
		}

		Device selectedDevice = Model.instance.getSelectedDevice();
		return Preferences.getInstance().getAdbInstallLocatoin() + "adb "
				+ (selectedDevice != null ? "-s " + selectedDevice.getId() + " " : "") + command;
		// return "adb " + command;
	}

	public interface ADBRunListener {
		void onFinish(String resporse);
	}

	public static void runAsync(String string, ADBRunListener listener) {
		executor.execute(new Runnable() {

			@Override
			public void run() {
				String resporse = executeADBCommand(string);

				if (listener != null) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							listener.onFinish(resporse);
						}
					});
				}
			}
		});
	}

	public static String run(Command command) {

		return executeADBCommand(command.command);
	}

	public static String run(String string) {

		return executeADBCommand(string);
	}

	public static String executeCommand(String [] command) {
		return executeCommand(null, command);
	}

	public static String executeCommand(String command) {
		return executeCommand(command, null);
	}
	public static String executeCommand(String command , String [] commands) {
		// System.out.println("Run: " + command);

		StringBuffer output = new StringBuffer();

		Process p;
		try {

			String[] envp = {};
			if (commands == null) {
				p = Runtime.getRuntime().exec(command, envp);
			} else {
				p = Runtime.getRuntime().exec(commands, envp);
			}

			p.waitFor(10, TimeUnit.SECONDS);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			boolean firstLine = true;
			String line = "";
			while ((line = reader.readLine()) != null) {
				if (!firstLine) {
					output.append("\n");
				}
				firstLine = false;
				output.append(line);
				// System.out.println(line);
			}

			reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			while ((line = reader.readLine()) != null) {
				if (!firstLine) {
					output.append("\n");
				}
				firstLine = false;
				output.append(line + "\n");
				// System.out.println(line);
			}

		} catch (Exception e) {
			e.printStackTrace();
			output.append(e.getMessage());
		}

		String result = output.toString();

		if (false){
			Logger.d("Run: " + command + "\n" + result);
		}

		return result;
	}
}
