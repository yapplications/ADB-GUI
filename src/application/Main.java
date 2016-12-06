package application;

import application.preferences.Preferences;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import application.services.DeviceMonitorService;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.PrintStream;

public class Main extends Application {

	public static HostServices hostService;

	@Override
	public void start(Stage primaryStage) throws Exception {

		DeviceMonitorService.instance.startMonitoringDevices();

		if (Preferences.getInstance().isDebug()) {
			System.setOut(new PrintStream(Preferences.getInstance().getLogFile()));
			System.setErr(new PrintStream(Preferences.getInstance().getLogFileErr()));
		}

		hostService = getHostServices();

		Parent root = FXMLLoader.load(getClass().getResource("FXMLMain.fxml"));
		Scene scene = new Scene(root, 1200, 600);

		primaryStage.setTitle("ADB GUI");
		primaryStage.setScene(scene);
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

	public static void main(String[] args) {
		launch(args);
	}
}
