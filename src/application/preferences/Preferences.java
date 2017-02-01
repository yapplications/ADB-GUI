package application.preferences;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import application.DateUtil;
import application.FileUtils;
import application.log.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Preferences {

	public static final String OS = System.getProperty("os.name").toLowerCase();
	private static Preferences instance;
	private PreferenceObj preferenceObj;
	private File appFolder = new File("appData" + File.separator);
	private File preferenceFile = new File(appFolder, "preferences");
	private File apksFolder = new File(appFolder, "apks" + File.separator);
	private File screenshotsFolder = new File(appFolder, "screenshots" + File.separator);
	private File appLogsFolder = new File(appFolder, "app-logs" + File.separator);
	private File logcatFolder = new File(appFolder, "logcat-logs" + File.separator);
	private File commandsFolder = new File(appFolder, "commands" + File.separator);
	private File intentsFolder = new File(appFolder, "intents" + File.separator);

	private Preferences() {

		if (!appFolder.exists()){
			appFolder.mkdirs();
		}

		if (!preferenceFile.exists()){
			createDefaultPreferences();
		} else {
			Gson gson = new Gson();
			try {
				preferenceObj = gson.fromJson(FileUtils.readFile(preferenceFile.getAbsolutePath()), PreferenceObj.class);
			} catch (JsonSyntaxException | IOException e) {
				e.printStackTrace();
				preferenceObj = new PreferenceObj();
				Logger.e("Unable to load prefs will work with defaults");
			}
		}

	}

	public File getCommandFolder() {
		if (!commandsFolder.exists()) {
			boolean folderCreated = commandsFolder.mkdir();

			Logger.d("No commands folder, folder created: " + folderCreated);
		}

		return commandsFolder;
	}

	public File getIntentsFolder() {
		if (!intentsFolder.exists()) {
			boolean folderCreated = intentsFolder.mkdir();

			Logger.d("No intents folder, folder created: " + folderCreated);
		}

		return intentsFolder;
	}

	private void createDefaultPreferences() {

		try {
			preferenceObj = new PreferenceObj();
			save();
		} catch (IOException e) {
			Logger.e("Unable to create / locate pref file will work from defaults");
			e.printStackTrace();
		}
	}

	public static Preferences getInstance(){
		if (instance == null){
			instance = new Preferences();
		}

		return instance;
	}

	public String getAdbInstallLocatoin() {
		String installedLocation = preferenceObj.adbPath;

		if (installedLocation != null) {

			// we will adjust the adb command ourselves
			if (installedLocation.endsWith(File.separator + "adb")) {
				installedLocation = installedLocation.substring(0, installedLocation.lastIndexOf(File.separator));
			}

			if (installedLocation.trim().length() > 0 && !installedLocation.endsWith(File.separator)){
				installedLocation += File.separator;
			}
		}

		return installedLocation;
	}

	public File getLogFile() {
		File logFolder = appLogsFolder;
		if (!logFolder.exists()){
			logFolder.mkdir();
		}

		return new File (logFolder, "log_" + DateUtil.getCurrentTimeStamp() + ".txt");
	}

	public File getLogFileErr() {
		File logFolder = appLogsFolder;
		if (!logFolder.exists()){
			logFolder.mkdir();
		}

		return new File (logFolder, "log_" + DateUtil.getCurrentTimeStamp() + "_e.txt");
	}

	public boolean isDebug() {
		return preferenceObj.debug;
	}

	public File getLogcatFolder() {
		File logFolder = logcatFolder;
		if (!logFolder.exists()){
			logFolder.mkdir();
		}
		return logFolder;
	}

	public void setEditWindowIsOpen(boolean isEditWindowOpen) {
		preferenceObj.isEditWindowOpen = isEditWindowOpen;
		try {
			save();
		} catch (IOException e) {}
	}

	public boolean isEditWindowIsOpen(){
		return preferenceObj.isEditWindowOpen;
	}

	public void setWindowIsAlwaysOn(boolean windowIsAlwaysOn) {
		preferenceObj.windowIsAlwaysOn = windowIsAlwaysOn;

		try {
			save();
		} catch (IOException e) {}
	}

	public boolean isWindowIsAlwaysOn(){
		return preferenceObj.windowIsAlwaysOn;
	}

	static class PreferenceObj{
		String adbPath = "";
	    boolean firstRun = true;
		public String apksFolders = "";
		public String obfuscationToolPath;
		public boolean debug = false;
		public boolean isEditWindowOpen = true;
		public boolean windowIsAlwaysOn;
	}

	public void setAdbPath(String adbPath){
		preferenceObj.adbPath = adbPath;
	}

	public void save() throws IOException{
		Gson gson = new Gson();
		preferenceFile.delete();
		FileUtils.writeToFile(preferenceFile.getPath(), gson.toJson(preferenceObj));
	}

	public String getAdbPath() {
		return preferenceObj.adbPath;
	}

	public boolean isFirstRun() {
		return preferenceObj.firstRun ;
	}

	public void setFirstRun(boolean firstRun) {
		preferenceObj.firstRun = firstRun;
	}

	public String getPrimaryAPKFolder() {
		return apksFolder.getAbsolutePath();
	}

	public String[] getAPKsFolders() {
		String [] additionslPathes = preferenceObj.apksFolders.split(";");

		String[] apksFolders = new String [additionslPathes.length + 1];

		apksFolders[0] = getPrimaryAPKFolder();

		for (int i = 0; i < additionslPathes.length; i++){
			apksFolders[i + 1] = additionslPathes[i];
		}

		return apksFolders ;
	}

	public void setAPKsFoldersPlain(String text) {
		preferenceObj.apksFolders = text;
	}

	public String getAPKsFoldersPlain() {
		return preferenceObj.apksFolders ;
	}

	public String getObfuscationToolPath() {
		return preferenceObj.obfuscationToolPath;
	}

	public void setObfuscationToolPath(String text) {
		preferenceObj.obfuscationToolPath = text;
	}

	public String getSnapshotFolder() {
		return screenshotsFolder.getAbsolutePath();
	}

	public File getAppFolder() {
		return appFolder;
	}
}
