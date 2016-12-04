package application.applications;

import java.io.File;
import java.net.URL;
import java.util.*;

import application.ADBHelper;
import application.AdbUtils;
import application.DialogUtil;
import application.FolderUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import application.log.Logger;
import application.model.Application;
import application.model.Device;
import application.model.Model;
import application.model.ModelListener;
import application.model.PackageProcess;
import application.services.ApplicationsMonitorService;
import javafx.scene.control.TextInputDialog;

public class ApplicationsTabController implements Initializable {

	private Device device;

	ObservableList<String> applicationsListItems = FXCollections.observableArrayList();

	@FXML
	private ListView<String> listApplications;

	@FXML
	private TextField textFieldFilter;

	@FXML
	private CheckBox checkBoxOnlyRunnig;

	ArrayList<Application> applications = new ArrayList<Application>();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		listApplications.setItems(applicationsListItems);

		textFieldFilter.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				filter();
			}
		});

		checkBoxOnlyRunnig.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				filter();
			}
		});
	}

	private void loadApps() {
		Logger.d("load apps");
		Application previousSelectedApp = getSelectedApplication();
		applicationsListItems.clear();

		applications.clear();

		if (device != null){
			applications.addAll(device.getApplications());

			Collections.sort(applications, new Comparator<Application>() {

				@Override
				public int compare(Application o1, Application o2) {
					return o1.getApplicationName().compareTo(o2.getApplicationName());
				}
			});
		}

		filter();

		selectApplication(previousSelectedApp);
	}

	private void selectApplication(Application previousSelectedApp) {
		if (previousSelectedApp != null){
			int i = 0;
			for (String applicationString : applicationsListItems){
				if (applicationString.contains(previousSelectedApp.getApplicationName())){
					listApplications.getSelectionModel().select(i);
					break;
				}
				i++;
			}
		}
	}

	private void filter() {
		applicationsListItems.clear();

		for (Application application: applications){
			if (isInFilter(application)){
				applicationsListItems.add(application.getApplicationName() +
						(application.isRunning() ? " RUNNING " + application.getPackageProcesses().size() : ""));
			}
		}
	}

	private boolean isInFilter(Application application) {
		return application.getApplicationName().contains(textFieldFilter.getText()) &&
				(!checkBoxOnlyRunnig.selectedProperty().get() || application.isRunning());
	}

	@FXML
	private void handleOpenClicked(ActionEvent event) {
		if (isAppSelected()){
			AdbUtils.executor.execute(new Runnable() {
				@Override
				public void run() {
					Logger.ds("Open: " + getSelectedApplication().getApplicationName());
					String result = ADBHelper.openApp(getGetSelectedAppPackage());
					if (result == null){
						Logger.fs("Opened: " + getSelectedApplication().getApplicationName());
					} else {
						Logger.es("Error opening: " + getSelectedApplication().getApplicationName() + " " + result);
					}
				}
			});
		}
	}

	@FXML
	private void uninstallClicked(ActionEvent event) {
		if (isAppSelected()){
			AdbUtils.executor.execute(new Runnable() {
				@Override
				public void run() {
					String selectedPackage = getGetSelectedAppPackage();
					Logger.ds("Uninstaling: " + selectedPackage);
					String result = AdbUtils.run("uninstall " + selectedPackage);

					if (result.trim().equals("Success")){
						Logger.fs("Uninstalled: " + selectedPackage);
					} else {
						Logger.es("Error Uninstaling: " + selectedPackage + " " + result);
					}
				}
			});
		}
	}

	@FXML
	private void handleClearDataClicked(ActionEvent event) {

		if (isAppSelected()){

			AdbUtils.executor.execute(new Runnable() {
				@Override
				public void run() {
					String result = ADBHelper.clearData(getGetSelectedAppPackage());
					if (result == null){
						Logger.fs("Cleared data: " + getGetSelectedAppPackage());
					} else {
						Logger.es("Cleared data failed: " + result);
					}
				}
			});
		}
	}

	public String getGetSelectedAppPackage(){
		return getSelectedApplication().getPackageName();
	}

	@FXML
	private void handleKillClicked(ActionEvent event) {

		if (isAppSelected()){
			Application application = getSelectedApplication();

			if (application.getPackageProcesses() == null){
				DialogUtil.showErrorDialog("Package should have running processes");
				return;
			}

			for (PackageProcess packageProcess: application.getPackageProcesses()){

				AdbUtils.executor.execute(new Runnable() {
					@Override
					public void run() {
						String result = ADBHelper.kill(application.getPackageName(), packageProcess.PID);
						if (result == null){
							Logger.fs("Killed: " + application.getPackageName());
						} else {
							Logger.es(result);
						}
					}
				});

			}
		}
	}

	@FXML
	private void handleGetAPKClicked(ActionEvent event) {

		if (isAppSelected()){
			final Application application = getSelectedApplication();
			AdbUtils.executor.execute(new Runnable() {

				@Override
				public void run() {
					Logger.ds("Getting apk path for: " + application.getApplicationName());
					String path = AdbUtils.run("shell pm path " + application.getPackageName());

					if (!path.startsWith("package:")){
						Logger.es("Error getting path for apk: " + application.getApplicationName() + ": " + path);
						return;
					}

					File apkFolder = FolderUtil.getApkFolder();

					String tempApkFile = "/sdcard/base.apk";
					String from = path.replace("package:", "");

					Logger.ds("Copy: " + from + " to: " + tempApkFile);

					String result = AdbUtils.run("shell cp " + from + " " + tempApkFile);
					if (!result.trim().equals("")){
						Logger.es("Error copying: " + from + " to: " + tempApkFile);
						return;
					}

					File apkToCreate = new File(apkFolder.getAbsolutePath(), application.getPackageName() + ".apk");
					Logger.ds("pulling: " + from + " to: " + apkToCreate.getAbsolutePath());

					if (ADBHelper.pull(tempApkFile, apkToCreate.getAbsolutePath())){
						Logger.fs("File pulled to: " + apkToCreate.getAbsolutePath());
					} else {
						Logger.es("Error pulling: " + result);
					}

					result = ADBHelper.rm(tempApkFile);
				}
			});
		}
	}

	private Application getSelectedApplication() {

		if (listApplications != null) {
			for (Application application : applications) {
				if (application != null && application.getPackageName() != null &&
						listApplications.getSelectionModel().getSelectedItem() != null &&
						application.getPackageName().equals(listApplications.getSelectionModel().getSelectedItem().split(" ")[0])) {
					return application;
				}
			}
		}
		return null;
	}

	private boolean isAppSelected() {

		if (listApplications.getSelectionModel().getSelectedIndex() > -1){
			return true;
		} else {
			DialogUtil.showErrorDialog("Please select package first");
			return false;
		}
	}

	public void stopWorking() {
		Logger.d("stop analyzing apps");
		ApplicationsMonitorService.instance.stop();
		Model.instance.removeModelListener(deviceChangeListener);

		if (device != null){
			device.removeModelListener(deviceApplicationsListener);
		}
	}

	public void startWorking() {
		Logger.d("start analyzing apps");
		ApplicationsMonitorService.instance.start();

		Model.instance.addSelectedDeviceListener(deviceChangeListener);
		setDeviceData();
	}

	private ModelListener deviceChangeListener = new ModelListener() {

		@Override
		public void onChangeModelListener() {
			Logger.d("Application Controler: Detected device change");

			setDeviceData();
		}
	};

	private ModelListener deviceApplicationsListener = new ModelListener() {

		@Override
		public void onChangeModelListener() {
			Logger.d("Application Controler: Detected applications change");
			loadApps();

		}
	};

	protected void setDeviceData() {

		if (device != null){
			device.removeModelListener(deviceApplicationsListener);
		}

		device = Model.instance.getSelectedDevice();
		if (device != null){
			device.addModelListener(deviceApplicationsListener);
			loadApps();
		}
	}

	public void runMonkeyClicked(ActionEvent actionEvent) {

		if (isAppSelected()) {
			final Application application = getSelectedApplication();

			TextInputDialog dialog = new TextInputDialog("500");
			dialog.setTitle("Monkey runner");
			dialog.setHeaderText("Enter amount of steps to run:");
			//dialog.setContentText("Enter amount of steps to run:");

			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				try {
					final int numberOfSteps = Integer.parseInt(result.get());

					AdbUtils.executor.execute(new Runnable() {
						@Override
						public void run() {

							Logger.ds("Running monkey of: " + application.getApplicationName() + " steps: " + numberOfSteps);

							String result = ADBHelper.runMonkey(application.getApplicationName(), numberOfSteps, 100);

							if (result == null){
								Logger.fs("Monkey Finished: " + application.getApplicationName());
							} else {
								Logger.es("Monkey failed: " + application.getApplicationName() + " " + result);
							}
						}
					});
				} catch (Exception e) {
					DialogUtil.showErrorDialog("Please select a number");
					runMonkeyClicked(null);
				}
			}
		}
	}
}
