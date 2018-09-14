package application.screencapture;

import application.*;
import application.log.Logger;
import application.model.Model;
import application.preferences.Preferences;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScreenCaptureController implements Initializable {

	public static ExecutorService executor = Executors.newSingleThreadExecutor();
	public AnchorPane paneImageContainer;
	public Label labelLog;
	File snapshotsFolder = FolderUtil.getSnapshotFolder();

	@FXML
	public ImageView imageViewCapture;

	@FXML
	public Pane pane;
	private volatile boolean work;
	private int saving = 1;
	private int saved = 2;
	private Stage stage;
	private Scene scene;

	@Override
	public void initialize(URL location, ResourceBundle resources) {


		imageViewCapture.fitWidthProperty().bind(paneImageContainer.widthProperty());
		imageViewCapture.fitHeightProperty().bind(paneImageContainer.heightProperty());

		//paneImageContainer.widthProperty().bind(pane.widthProperty());
		//paneImageContainer.fitHeightProperty().bind(pane.heightProperty());


		startScreenMonitoring();
	}

	private void updatePicture() {
		File file = getTempSnapshotSaved();
		if (file.exists()) {
			Image image = new Image(file.toURI().toString());

			updateScreenRatio(image);

			imageViewCapture.setImage(image);
		}
	}

	private void updateScreenRatio(Image image) {
		//stage.minWidthProperty().bind(scene.heightProperty().divide(1.83333333333333d));
		//stage.minHeightProperty().bind(scene.widthProperty().multiply(1.83333333333333d));
	}

	private void setStage(Stage stage, Scene scene) {
		stage.setOnHiding(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				stopScreenMonitoring();
			}
		});

		this.stage = stage;
		this.scene = scene;
	}

	private void stopScreenMonitoring() {
		Logger.d("Stop screen monitoring " + this);
		work = false;
	}

	@FXML
	public void onSaveClicked(ActionEvent actionEvent) {
		String fileName = Model.instance.getSelectedDevice().getName() + " " +
				Model.instance.getSelectedDevice().getAndroidVersion() + " " +
				DateUtil.getCurrentTimeStamp() + ".png";

		fileName = fileName.replace(" ", "");

		File snapshotFile = new File(snapshotsFolder,
				fileName);

		Path source = Paths.get(getTempSnapshotSaved().getAbsolutePath());
		Path destination = Paths.get(snapshotFile.getAbsolutePath());

		try {
			Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
			labelLog.setText("Snapshot saved");
			labelLog.setTextFill(Color.GREEN);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void startScreenMonitoring() {
		Logger.d("Start screen monitoring " + this);
		work = true;
		executor.execute(new Runnable() {
			@Override
			public void run() {
				while (work) {
					String tempPicture = "/sdcard/temp.png";
					Logger.d("Taking snapshot " + tempPicture);

					String result = AdbUtils.run("shell screencap -p " + tempPicture);
					if (!result.equals("")) {
						Logger.e("Error taking snapshot: " + result);
						return;
					}

					File snapshotFile = getTempSnapshotToSave();

					if (ADBHelper.pull(tempPicture, snapshotFile.getAbsolutePath())) {
						Logger.d("Created snapshot: " + snapshotFile.getAbsolutePath());
					}

					ADBHelper.rm(tempPicture);

					int temp = saving;
					saving = saved;
					saved = temp;

					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							updatePicture();
						}
					});

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}
		});
	}

	public void onOpenFolderClicked(ActionEvent actionEvent) {
		if (Desktop.isDesktopSupported()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						Desktop.getDesktop().open(new File(Preferences.getInstance().getSnapshotFolder()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	private File getTempSnapshotToSave() {
		return new File(snapshotsFolder,
				"temp" + saving + ".png");
	}

	private File getTempSnapshotSaved() {
		return new File(snapshotsFolder,
				"temp" + saved + ".png");
	}

	public static void showScreen(Class class1) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(class1.getResource("/application/screencapture/ScreenCaptureLayout.fxml"));

		Parent root1 = (Parent) fxmlLoader.load();

		ScreenCaptureController controller = fxmlLoader.<ScreenCaptureController>getController();

		Stage stage = new Stage();
		stage.setMinHeight(560);
		stage.setMinWidth(300);

		stage.setHeight(560);
		stage.setWidth(300);
		stage.setTitle("Screen Capture");
		Scene scene = new Scene(root1);
		stage.setScene(scene);
		stage.show();

		controller.setStage(stage, scene);
	}
}
