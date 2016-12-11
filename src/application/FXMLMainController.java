package application;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import application.apks.APKsTabController;
import application.applications.ApplicationsTabController;
import application.preferences.Preferences;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import application.log.Logger;
import application.log.Logger.LoggerListener;
import javafx.stage.Stage;

public class FXMLMainController implements Initializable {

	public static final int TAB_INDEX_APPLICATIONS = 1;

	protected static final int TAB_INDEX_APKS = 2;
	public Button buttonToggleEdit;
	public CheckBox checkBoxAlwaysOnTop;

	@FXML
	private TabPane tabPane;

	@FXML
	private Tab preferenceTabPage;

    @FXML
    private ApplicationsTabController applicationTabPageController;

    @FXML
    private APKsTabController apksTabPageController;

    @FXML
    private Label labelRunningLog;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		if (Preferences.getInstance().isFirstRun()) {

			findADBPath();

			tabPane.getSelectionModel().select(preferenceTabPage);
			Preferences.getInstance().setFirstRun(false);
			try {
				Preferences.getInstance().save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		tabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (oldValue.equals(TAB_INDEX_APPLICATIONS)){
					applicationTabPageController.stopWorking();
				} else if (newValue.equals(TAB_INDEX_APPLICATIONS)){
					applicationTabPageController.startWorking();
				}

				if (newValue.equals(TAB_INDEX_APKS)){
					apksTabPageController.refreshList();
				}
			}
		});

		Logger.setShowLogListener(new LoggerListener() {
			@Override
			public void onNewLogToShow(String message) {
				log(Color.BLACK, message);
			}

			@Override
			public void onNewErrorLogToShow(String message) {
				log(Color.RED, message);
			}

			@Override
			public void onFinishLogToShow(String message) {
				log(Color.GREEN, message);
			}
		});
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

	protected void log(Color color, String message) {
		labelRunningLog.setTextFill(color);
		labelRunningLog.setText(message);
	}

	protected ApplicationsTabController getApplicationController() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("applications/ApplicationsTab.fxml"));

		try {
			Parent root1 = (Parent) fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ApplicationsTabController controller = fxmlLoader.<ApplicationsTabController>getController();

		return controller;
	}


	public void handleToggleMainView(ActionEvent actionEvent) {

		Stage stage = (Stage) tabPane.getScene().getWindow();
		if (tabPane.isVisible()){
			tabPane.setVisible(false);
			stage.setWidth(210);
			stage.setResizable(false);
			buttonToggleEdit.setText("Open edit window");

		} else {
			tabPane.setVisible(true);
			stage.setResizable(true);
			stage.setWidth(1200);

			buttonToggleEdit.setText("Close edit window");
		}
	}

	public void handleAlwaysOnTop(ActionEvent actionEvent) {
		Stage stage = (Stage) tabPane.getScene().getWindow();
		stage.setAlwaysOnTop(checkBoxAlwaysOnTop.isSelected());
	}
}
