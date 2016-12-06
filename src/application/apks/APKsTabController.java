package application.apks;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Set;

import application.*;
import application.log.Logger;
import application.preferences.Preferences;
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

public class APKsTabController implements Initializable {

    @FXML
    public TextField textFieldFilter;
    public CheckBox checkBoxOpenAfterInstall;

    ObservableList<String> apksListItems = FXCollections.observableArrayList();
    ArrayList<String> apksPathes = new ArrayList<>();

    @FXML
    private ListView<String> listAPKs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listAPKs.setItems(apksListItems);

        textFieldFilter.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                filter();
            }
        });

    }

    @FXML
    private void handleInstallAction(ActionEvent event) {
        String selectedApk = listAPKs.getSelectionModel().getSelectedItem();
        if (selectedApk != null) {
            AdbUtils.executor.execute(new Runnable() {
                @Override
                public void run() {
                    Set<String> packages = null;
                    if (checkBoxOpenAfterInstall.isSelected()) {
                        packages = ADBHelper.getPackages();
                    }

                    Logger.ds("Trying to install: " + selectedApk);
                    String result = ADBHelper.install(selectedApk);

                    if (result != null) {
                        Logger.es(result);
                    } else {
                        Logger.fs("Installed: " + selectedApk);


                        if (packages != null) {
                            boolean found = false;
                            Set<String> newPackages = ADBHelper.getPackages();

                            for (String packageNew : newPackages) {
                                if (!packages.contains(packageNew)) {
                                    found = true;
                                    Logger.ds("Found new pakcage: " + packageNew);
                                    ADBHelper.openApp(packageNew);
                                    Logger.ds("Opened package: " + packageNew);
                                    break;
                                }
                            }

                            if (!found) {
                                Logger.es("Didnt found new package to open");
                            }
                        }
                    }
                }
            });
        }
    }

    @FXML
    private void handleObfuscationToolAction(ActionEvent event) {

        if (Preferences.getInstance().getObfuscationToolPath() == null ||
                Preferences.getInstance().getObfuscationToolPath().trim().equals("")) {
            DialogUtil.showErrorDialog("Go to preference tab and Download / Configure Obfuscation tool");
            return;
        }

        String selectedApk = listAPKs.getSelectionModel().getSelectedItem();
        if (selectedApk != null) {
            if (Preferences.getInstance().getObfuscationToolPath() != null) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        try {

                            Process process = Runtime.getRuntime().exec(
                                    Preferences.getInstance().getObfuscationToolPath() + " " + selectedApk, ProcessUtils.getEnviormentParams());
                            /*BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

							String line;
							while ((line = reader.readLine()) != null) {
								System.out.println(line);
							}*/
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } else {
            DialogUtil.showErrorDialog("Please select APK to analyze");
        }
    }

    public void refreshList() {
        apksPathes.clear();

        String[] folders = Preferences.getInstance().getAPKsFolders();

        for (String folderPath : folders) {

            File folder = new File(folderPath);

            if (folder != null && folder.isDirectory()) {
                ArrayList<File> files = new ArrayList<File>();
                FileUtils.getFilesInFolderRecursivly(folder.getAbsolutePath(), files);
                for (File file : files) {
                    if (file.getName().endsWith(".apk")) {
                        apksPathes.add(file.getAbsolutePath());
                    }
                }
            }
        }

        filter();
    }

    private void filter() {
        apksListItems.clear();

        for (String path : apksPathes) {
            if (isInFilter(path)) {
                apksListItems.add(path);
            }
        }
    }

    private boolean isInFilter(String path) {
        return path.contains(textFieldFilter.getText());
    }
}
