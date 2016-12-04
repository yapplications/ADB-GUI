package application;

import java.io.File;
import java.io.IOException;

import application.log.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import application.Preferences.PreferenceObj;

public class Preferences {
	private static final String PREF_FILE = "preferences";
	private static Preferences instance;
	private PreferenceObj preferenceObj;

	private Preferences() {
		File preferenceFile = new File(PREF_FILE);

		if (!preferenceFile.exists()){
			createDefaultPreferences();
		} else {
			Gson gson = new Gson();
			try {
				preferenceObj = gson.fromJson(FileUtils.readFile(PREF_FILE), PreferenceObj.class);
			} catch (JsonSyntaxException | IOException e) {
				e.printStackTrace();
				preferenceObj = new PreferenceObj();
				Logger.e("Unable to load prefs will work with defaults");
			}
		}

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
		return preferenceObj.adbPath;
	}

	public File getLogFile() {
		File logFolder = new File("app-logs/");
		if (!logFolder.exists()){
			logFolder.mkdir();
		}

		return new File (logFolder, "log_" + DateUtil.getCurrentTimeStamp() + ".txt");
	}

	public File getLogFileErr() {
		File logFolder = new File("app-logs/");
		if (!logFolder.exists()){
			logFolder.mkdir();
		}

		return new File (logFolder, "log_" + DateUtil.getCurrentTimeStamp() + "_e.txt");
	}

	public boolean isDebug() {
		return preferenceObj.debug;
	}

	public File getLogcatFolder() {
		File logFolder = new File("logcat-logs/");
		if (!logFolder.exists()){
			logFolder.mkdir();
		}
		return logFolder;
	}

	static class PreferenceObj{
		String adbPath = "/Users/evgeni.shafran/Library/Android/sdk/platform-tools/";
	    boolean firstRun = true;
		public String apksFolders = "";
		public String obfuscationToolPath;
		public boolean debug = true;
	}

	public void setAdbPath(String adbPath){
		preferenceObj.adbPath = adbPath;
	}

	public void save() throws IOException{
		Gson gson = new Gson();
		new File(PREF_FILE).delete();
		FileUtils.writeToFile(PREF_FILE, gson.toJson(preferenceObj));
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
		return "apks/";
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
		return "screenshot/";
	}
}
