package application;

import application.preferences.Preferences;

import java.io.File;

public class FolderUtil {

	public static File getApkFolder() {

		File apkFolder = new File(Preferences.getInstance().getPrimaryAPKFolder());
		if (!apkFolder.exists()){
			apkFolder.mkdir();
		}

		return apkFolder;
	}

	public static File getSnapshotFolder() {

		File apkFolder = new File(Preferences.getInstance().getSnapshotFolder());
		if (!apkFolder.exists()){
			apkFolder.mkdir();
		}

		return apkFolder;
	}

}
