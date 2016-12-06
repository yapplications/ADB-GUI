package application.logexceptions;

import application.AdbUtils;
import application.DateUtil;
import application.DialogUtil;
import application.preferences.Preferences;
import application.log.Logger;
import application.model.Model;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by evgeni.shafran on 10/17/16.
 */
public class LogExceptionsController implements Initializable {

    public static ExecutorService executor = Executors.newSingleThreadExecutor();

    public ListView listViewLog;
    public Button buttonToggle;
    public Button buttonClearLogCat;
    public Button buttonFindExceptions;
    public Button buttonPrevException;
    public Label labelExceptions;
    public Button buttonNextException;

    ObservableList<String> logListItems = FXCollections.observableArrayList();
    private boolean working;
    private ArrayList<ExceptionLog> exceptions;
    private volatile boolean exceptionState;
    private int exceptionIndex;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listViewLog.setItems(logListItems);
        toggleButtonsStates(false);
    }

    public void onToggleClicked(ActionEvent actionEvent) {

        if (working) {
            stop();
        } else {
            start();
        }
    }

    private void start() {
        toggleButtonsStates(false);
        logListItems.clear();
        working = true;
        buttonToggle.setText("Stop");

        executor.execute(new Runnable() {
            @Override
            public void run() {
                String logcatCommand = AdbUtils.getAdbCommand("logcat");

                Process process;
                try {

                    String[] envp = {};
                    process = Runtime.getRuntime().exec(logcatCommand, envp);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        if (!working) {
                            break;
                        }

                        if (!exceptionState) {
                            addLine(line);
                        }
                    }

                    process.destroy();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void stop() {
        working = false;
        buttonToggle.setText("Start");

    }

    private void addLine(String line) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                logListItems.add(line);
            }
        });
    }

    public void onClearLocalyClicked(ActionEvent actionEvent) {
        logListItems.clear();
    }

    public void onClearADBClicked(ActionEvent actionEvent) {
        //stop();
        logListItems.clear();
        /*AdbUtils.runAsync("logcat -c", new AdbUtils.ADBRunListener() {
            @Override
            public void onFinish(String resporse) {

            }
        });*/
    }

    public void onSaveToFileClicked(ActionEvent actionEvent) {
        final List<String> listToSave = new ArrayList<>(logListItems.size());
        for (String line : logListItems) {
            listToSave.add(line);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                File logcatFolder = Preferences.getInstance().getLogcatFolder();

                PrintWriter writer = null;
                Logger.ds("Saving log...");
                try {

                    File logFile = new File(logcatFolder,
                            Model.instance.getSelectedDevice().getName() + " " +
                                    Model.instance.getSelectedDevice().getAndroidVersion() + " " +
                                    DateUtil.getCurrentTimeStamp() + ".txt");


                    writer = new PrintWriter(logFile, "UTF-8");

                    for (String line : listToSave) {
                        writer.println(line);
                    }

                    writer.close();
                    Logger.fs("Log saved: " + logFile.getAbsolutePath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Logger.es("Error creating log: " + e.getMessage());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Logger.es("Error creating log: " + e.getMessage());
                }
            }
        }).start();
    }

    public void onFindExceptionsClicked(ActionEvent actionEvent) {

        if (logListItems.size() == 0){
            DialogUtil.showErrorDialog("Press 'Start' first, we need some logs to analyze");
            return;
        }

        toggleButtonsStates(true);
        stop();

        exceptions = new ArrayList<>();

        ExceptionLog exception = null;

        for (String logString : logListItems) {
            Log log = null;
            try {
                log = Log.logFromStringLog(logString);
            } catch (Exception e) {
                Logger.d("Unable to parse log: " + logString);
                continue;
            }

            if (exception != null) {
                if (exception.isPartOfRxception(log)) {
                    exception.addLog(log);
                } else {
                    if (exception.logs.size() <= 1){
                        exceptions.remove(exception);
                    }
                    exception = null;
                }
            }

            if (exception == null) {
                if (log.isError()) {
                    if (log.isExceptionStart()) {
                        exception = new ExceptionLog(log);
                        exceptions.add(exception);
                    }
                }
            }
        }

        updateExceptions();
    }

    private void updateExceptions() {
        if (exceptions.size() > 0) {
            showLog(exceptions.size() - 1);
        } else {
            DialogUtil.showInfoDialog("No exceptions found");
            toggleButtonsStates(false);
        }
    }

    private void showLog(int i) {
        logListItems.clear();

        exceptionIndex = i;
        ArrayList<Log> logs = exceptions.get(i).logs;
        for (Log log : logs) {
            logListItems.add(log.getFullLog());
        }

        labelExceptions.setText( (i + 1) + "/" + exceptions.size());
    }

    public void onPreviousExceptionsClicked(ActionEvent actionEvent) {
        int i = exceptionIndex - 1;
        if (i < 0){
            i = exceptions.size() - 1;
        }

        showLog(i);
    }

    public void onNextExceptionsClicked(ActionEvent actionEvent) {
        int i = exceptionIndex + 1;
        if (i >= exceptions.size()){
            i = 0;
        }

        showLog(i);
    }

    public void toggleButtonsStates(boolean exceptionState) {

        this.exceptionState = exceptionState;

        buttonClearLogCat.setDisable(exceptionState);
        buttonFindExceptions.setDisable(exceptionState);
        buttonPrevException.setVisible(exceptionState);
        labelExceptions.setVisible(exceptionState);
        buttonNextException.setVisible(exceptionState);
    }
}
