package application;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import application.apks.APKsTabController;
import application.applications.ApplicationsTabController;
import application.model.ModelListener;
import application.preferences.Preferences;
import application.screencapture.ScreenCaptureController;
import application.startupcheck.StartupCheckController;
import com.sun.prism.PresentableState;
import javafx.application.Platform;
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

import javax.swing.*;

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
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (Preferences.getInstance().isFirstRun()) {

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
                if (oldValue.equals(TAB_INDEX_APPLICATIONS)) {
                    applicationTabPageController.stopWorking();
                } else if (newValue.equals(TAB_INDEX_APPLICATIONS)) {
                    applicationTabPageController.startWorking();
                }

                if (newValue.equals(TAB_INDEX_APKS)) {
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

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                checkBoxAlwaysOnTop.setSelected(Preferences.getInstance().isWindowIsAlwaysOn());
                handleAlwaysOnTop(null);

                if (!Preferences.getInstance().
                        isEditWindowIsOpen()) {
                    handleToggleMainView(null);
                }
            }
        });

        openADBValidator();
    }

    private void openADBValidator() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {


                try {
                    StartupCheckController.showScreen(getClass());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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

        if (stage != null && tabPane != null) {

            Logger.d("tabPane  " + tabPane);

            if (tabPane.isVisible()) {
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

            if (actionEvent != null) {
                Preferences.getInstance().setEditWindowIsOpen(tabPane.isVisible());
            }
        } else {
            Logger.e("handleToggleMainView WTF: " + stage + " " + tabPane);
        }
    }

    public void handleAlwaysOnTop(ActionEvent actionEvent) {
        if (stage != null && checkBoxAlwaysOnTop != null) {
            stage.setAlwaysOnTop(checkBoxAlwaysOnTop.isSelected());

            if (actionEvent != null) {
                Preferences.getInstance().setWindowIsAlwaysOn(checkBoxAlwaysOnTop.isSelected());
            }
        } else {
            Logger.e("handleAlwaysOnTop WTF: " + stage + " " + checkBoxAlwaysOnTop);
        }

    }

    public void onOpenAppDirectory(ActionEvent actionEvent) {
        if (Desktop.isDesktopSupported()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        Desktop.getDesktop().open(Preferences.getInstance().getAppFolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void setStageAndSetupListeners(Stage stage) {
        this.stage = stage;
    }
}
