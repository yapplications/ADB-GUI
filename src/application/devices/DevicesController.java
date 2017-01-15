package application.devices;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import application.ADBHelper;
import application.AdbUtils;
import application.DialogUtil;
import application.intentbroadcasts.IntentBroadcast;
import application.model.*;
import application.screencapture.ScreenCaptureController;
import application.view.DateTimePickerController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import application.log.Logger;
import application.services.DeviceMonitorService;
import javafx.scene.control.TextField;

public class DevicesController implements Initializable {

    public TextField textFieldDeviceInput;
    public ChoiceBox choiceBoxBatchCommands;
    boolean killed = false;

    ObservableList<String> commandsListItems = FXCollections.observableArrayList();


    @FXML
    private ListView<String> listDevices;

    @FXML
    private Button buttonADBToggle;

    ObservableList<String> devicesListItems = FXCollections.observableArrayList();

    private List<Device> availableDevices;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listDevices.setItems(devicesListItems);
        // listDevices.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        listDevices.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> ov, String old_val, String new_val) {
                if (listDevices.getSelectionModel().getSelectedIndex() >= 0) {
                    Model.instance.setSelectedDevice(
                            availableDevices.get(listDevices.getSelectionModel().getSelectedIndex()));
                }
            }
        });

        choiceBoxBatchCommands.setItems(commandsListItems);

        choiceBoxBatchCommands.getSelectionModel().selectedItemProperty().addListener(batchCommandSelect);

        Model.instance.addModelListener(new ModelListener() {

            @Override
            public void onChangeModelListener() {
                refreshDevices();
            }
        });

        refreshDevices();

        loadBatchCommands();
    }

    ChangeListener batchCommandSelect = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            int selectedIndex = choiceBoxBatchCommands.getSelectionModel().getSelectedIndex();
            if (selectedIndex != 0) {
                choiceBoxBatchCommands.setDisable(true);

                final CommandBatch commandBatch = Model.instance.getCommandBatches().get(selectedIndex - 1);

                AdbUtils.executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        for (Command command : commandBatch.commands) {
                            Logger.ds("Run: " + command.description);
                            AdbUtils.run(command);
                        }

                        Platform.runLater(new Runnable() {
                            @Override public void run() {
                                choiceBoxBatchCommands.setDisable(false);

                                choiceBoxBatchCommands.getSelectionModel().select(0);
                            }
                        });


                        Logger.fs("Finished: " + commandBatch.name);
                    }
                });

            }
        }
    };

    private void loadBatchCommands() {
        commandsListItems.clear();

        commandsListItems.add("Select command to RUN");
        choiceBoxBatchCommands.getSelectionModel().select(0);

        for (CommandBatch commandBatch : Model.instance.getCommandBatches()) {
            commandsListItems.add(commandBatch.name);
        }
    }

    @FXML
    private void handleToggleADBClicked(ActionEvent event) {

        devicesListItems.clear();

        if (killed) {
            DeviceMonitorService.instance.startMonitoringDevices();
            buttonADBToggle.setText("Kill");
            Logger.fs("ADB server started");
        } else {
            buttonADBToggle.setText("Start monitoring");
            DeviceMonitorService.instance.stopMonitoringDevices();
            AdbUtils.executor.execute(new Runnable() {

                @Override
                public void run() {
                    Logger.d(ADBHelper.killServer());
                    Logger.fs("ADB server killed");
                }
            });

            DialogUtil.showInfoDialog("Restarting ADB service from this tool can cause device to be 'unauthorized'\n" +
                    "In that case please open you favourite command line (terminal) and enter:\n" +
                    "adb devices\n" +
                    "Then press start monitoring");
        }

        killed = !killed;
    }

    @FXML
    private void handleTakeSnapshotClicked(ActionEvent event) {
        try {
            ScreenCaptureController.showScreen(getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void refreshDevices() {

        Device selectedDevice = Model.instance.getSelectedDevice();

        int i = 0;

        devicesListItems.clear();
        availableDevices = Model.instance.getAvailableDevices();
        boolean setSelected = false;
        for (Device device : availableDevices) {
            devicesListItems.add(getDeviceDescription(device));

            if (selectedDevice != null && device.getId().equals(selectedDevice.getId())){
                listDevices.getSelectionModel().select(i);
                setSelected = true;
            }

            i++;
        }

        if (!setSelected && devicesListItems.size() > 0) {
            listDevices.getSelectionModel().select(0);
        }
    }

    private String getDeviceDescription(Device device) {
        return device.getName() + " " + device.getAndroidVersion() + " " + device.getId();
    }

    public void onDeviceInputEnter(ActionEvent actionEvent) {
        AdbUtils.executor.execute(new Runnable() {
            @Override
            public void run() {
                Logger.ds("Sending input to device: " + textFieldDeviceInput.getText());

                ADBHelper.sendInputText(textFieldDeviceInput.getText());
                Logger.fs("Text sent: " + textFieldDeviceInput.getText());
                textFieldDeviceInput.setText("");
            }
        });
    }

    public void handleConnectToWifiClicked(ActionEvent actionEvent) {
        AdbUtils.executor.execute(new Runnable() {
            @Override
            public void run() {

                Device selectedDevice = Model.instance.getSelectedDevice();

                if (selectedDevice != null) {
                    Logger.ds("Connecting device to wifi: " + getDeviceDescription(selectedDevice));
                    Logger.fes(ADBHelper.connectDeviceToWifi(), "Device connected, you can disconnect it from the usb port");

                } else {
                    DialogUtil.showErrorDialog("Please select device first");
                }
            }
        });
    }

    public void onChangeEmulatorDate(ActionEvent actionEvent) {
        Device device = Model.instance.getSelectedDevice();
        if (device == null) {
            DialogUtil.showErrorDialog("Select emulator first");
        } else if (!device.isEmulator()) {
            DialogUtil.showErrorDialog("Changing date/time works only on emulators");
        } else {
            try {
                DateTimePickerController.showScreen(getClass(), new DateTimePickerController.DateTimePickerListener() {
                    @Override
                    public void onDateSet(Calendar calendar) {
                        Logger.ds("Set emulator time" + (calendar.getTime()));
                        AdbUtils.executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                String result = ADBHelper.setDateTime(calendar);
                                Logger.fes(result, "Date/Time set: " + calendar.getTime());
                            }
                        });
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onOpenDevSettings(ActionEvent actionEvent) {
        AdbUtils.executor.execute(new Runnable() {
            @Override
            public void run() {
                IntentBroadcast intent = new IntentBroadcast();
                intent.activityManagerCommand = IntentBroadcast.ACTIVITY_MANAGER_COMMAND_START;
                intent.action = "android.settings.APPLICATION_DEVELOPMENT_SETTINGS";
                ADBHelper.sendIntent(intent);
            }
        });
    }
}
