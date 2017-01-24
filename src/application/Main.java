package application;

import application.log.Logger;
import application.preferences.Preferences;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import application.services.DeviceMonitorService;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javax.swing.*;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;

public class Main extends Application {

	public static HostServices hostService;

	@Override
	public void start(Stage primaryStage) throws Exception {

		DeviceMonitorService.instance.startMonitoringDevices();

		if (Preferences.getInstance().isDebug()) {
			System.setOut(new PrintStream(Preferences.getInstance().getLogFile()));
			System.setErr(new PrintStream(Preferences.getInstance().getLogFileErr()));
		}

		if (Preferences.getInstance().isFirstRun()) {

			findADBPath();
		}

		hostService = getHostServices();

		FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLMain.fxml"));

		Parent root = loader.load();
		Scene scene = new Scene(root, 1200, 620);

		Image iconImage = new Image("/res/icon.png");
		primaryStage.getIcons().add(iconImage);

		try {
		/*	URL iconURL = Main.class.getResource("/res/icon.png");
			java.awt.Image image = new ImageIcon(iconURL).getImage();
			com.apple.eawt.Application.getApplication().setDockIconImage(image);*/
		} catch (Exception e) {
			// Won't work on Windows or Linux.
		}


		primaryStage.setTitle("ADB GUI Tool");
		primaryStage.setScene(scene);

		FXMLMainController controller = (FXMLMainController)loader.getController();
		controller.setStageAndSetupListeners(primaryStage); // or what you want to do

		//primaryStage.setResizable(false);
		primaryStage.show();

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent e) {
				Platform.exit();
				System.exit(0);
			}
		});

		if (FolderUtil.getSnapshotFolder().getAbsolutePath().contains(" ")){
			DialogUtil.showErrorDialog("This app do not support operating from a path with spaces,\n" +
					"please move the app and start again");
			System.exit(0);
		}
	}

	private void findADBPath() {
		Logger.d("Find adb on: " + Preferences.OS);

		if (Preferences.OS.startsWith("windows")){

		} else {
			File baseDirectory = new File("/Users/");
			for (File file : baseDirectory.listFiles()) {
				File pathCheck = new File(file, "Library/Android/sdk/platform-tools/");
				if (pathCheck.exists()){
					Logger.d("Found adb location: " + pathCheck.getAbsolutePath());
					Preferences.getInstance().setAdbPath(pathCheck.getAbsolutePath() + "/");
					break;
				}
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
