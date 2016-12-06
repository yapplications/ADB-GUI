package application.intentbroadcasts;

import application.ADBHelper;
import application.AdbUtils;
import application.DialogUtil;
import application.FileUtils;
import application.log.Logger;
import application.model.CommandBatch;
import application.preferences.Preferences;
import com.google.gson.Gson;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.sun.activation.registries.LogSupport.log;

public class IntentBroadcastsController implements Initializable {

    private static final String ACTIVITY_MANAGER_COMMAND_START = "start";
    private static final String ACTIVITY_MANAGER_COMMAND_BROADCAST = "broadcast";
    private static final String ACTIVITY_MANAGER_COMMAND_START_SERVICE = "startservice";
    public TextField textFieldAction;
    public TextField textFieldData;
    public RadioButton radioButtonActivityManagerCommandStart;
    public RadioButton radioButtonActivityManagerCommandBroadcast;
    public RadioButton radioButtonActivityManagerCommandStartService;
    public Button buttonSend;
    public TextField textFieldMimeType;
    public TextField textFieldCategory;
    public TextField textFieldComponent;
    public ListView<String> listSaved;
    public TextField textFieldName;

    ObservableList<String> devicesListItems = FXCollections.observableArrayList();
    ObservableList<String> savedIntentsListItems = FXCollections.observableArrayList();
    ArrayList<IntentBroadcast> intentBroadcasts = new ArrayList<>();
    public ChoiceBox choiceBoxActions;
    private ArrayList<IntentBroadcast> intents = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fillDefaultActions();
        choiceBoxActions.setItems(devicesListItems);

        choiceBoxActions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (choiceBoxActions.getSelectionModel().getSelectedItem() != null) {
                    updateDefaults(intentBroadcasts.get(choiceBoxActions.getSelectionModel().getSelectedIndex()));
                }
                choiceBoxActions.getSelectionModel().select(null);
            }
        });

        radioButtonActivityManagerCommandBroadcast.setSelected(true);

        listSaved.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                loadSelectedIntoFieleds();
            }
        });

        listSaved.setItems(savedIntentsListItems);
        loadSavedIntents();
    }

    private void loadSelectedIntoFieleds() {
        int index = listSaved.getSelectionModel().getSelectedIndex();
        if (index > -1){
            textFieldName.setText(listSaved.getSelectionModel().getSelectedItem());

            updateDefaults(intents.get(index));
        }
    }

    private void updateDefaults(IntentBroadcast intentBroadcast) {
        textFieldAction.setText(intentBroadcast.action);
        textFieldComponent.setText(intentBroadcast.component);
        textFieldCategory.setText(intentBroadcast.category);
        textFieldData.setText(intentBroadcast.data);
        textFieldMimeType.setText(intentBroadcast.mimeType);
        //textFieldData.setPromptText(intentBroadcast.promptData);

        if (intentBroadcast.activityManagerCommand.equals(ACTIVITY_MANAGER_COMMAND_START)) {
            radioButtonActivityManagerCommandStart.setSelected(true);
        } else if (intentBroadcast.activityManagerCommand.equals(ACTIVITY_MANAGER_COMMAND_BROADCAST)) {
            radioButtonActivityManagerCommandBroadcast.setSelected(true);
        } else if (intentBroadcast.activityManagerCommand.equals(ACTIVITY_MANAGER_COMMAND_START_SERVICE)) {
            radioButtonActivityManagerCommandStartService.setSelected(true);
        }
    }

    //TODO should probably be loaded from disk (json)
    private void fillDefaultActions() {

        IntentBroadcast intentBroadcast = new IntentBroadcast();
        intentBroadcast.action = "android.intent.action.VIEW";
        intentBroadcast.activityManagerCommand = ACTIVITY_MANAGER_COMMAND_START;
        //intentBroadcast.promptData = "Can be web url like: http://somewebsite.com";

        addIntentBroadcast(intentBroadcast);

        intentBroadcast = new IntentBroadcast();
        intentBroadcast.action = "android.intent.action.BOOT_COMPLETED";
        //intentBroadcast.promptData = null;
        addIntentBroadcast(intentBroadcast);
    }

    private void addIntentBroadcast(IntentBroadcast intentBroadcast) {
        intentBroadcasts.add(intentBroadcast);
        devicesListItems.add(intentBroadcast.action);
    }

    public void onButtonSendClicked(ActionEvent actionEvent) {
        AdbUtils.timedCall(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                IntentBroadcast intentBroadcast = getFilledIntent();
                Logger.ds("Sending Intent: " + intentBroadcast);

                String result = ADBHelper.sendIntent(intentBroadcast);
                Logger.fes(result, "Intent Sent");
                return null;
            }
        }, 1, TimeUnit.SECONDS, new AdbUtils.TimeCallListener() {
            @Override
            public void timeout() {
                Logger.fs("Intent Sent");
            }

            @Override
            public void error(Exception e) {
                Logger.es("Error: " + e.toString());
            }
        });
    }

    private IntentBroadcast getFilledIntent() {
        IntentBroadcast intentBroadcast = new IntentBroadcast();
        intentBroadcast.activityManagerCommand = getActivityManagerCommand();
        intentBroadcast.data = textFieldData.getText();
        intentBroadcast.action = textFieldAction.getText();
        intentBroadcast.mimeType = textFieldMimeType.getText();
        intentBroadcast.category = textFieldCategory.getText();
        intentBroadcast.component = textFieldComponent.getText();

        return intentBroadcast;
    }

    private String getActivityManagerCommand() {
        if (radioButtonActivityManagerCommandStartService.isSelected())
            return ACTIVITY_MANAGER_COMMAND_START_SERVICE;

        if (radioButtonActivityManagerCommandStart.isSelected())
            return ACTIVITY_MANAGER_COMMAND_START;

        return ACTIVITY_MANAGER_COMMAND_BROADCAST;
    }

    public void onButtonSave(ActionEvent actionEvent) {
        if (textFieldName.getText().equals("")) {
            DialogUtil.showErrorDialog("Enter a name before saving");
        } else {
            IntentBroadcast intentBroadcast = getFilledIntent();

            File intentFile = new File(Preferences.getInstance().getIntentsFolder(), textFieldName.getText());
            Gson gson = new Gson();
            String jsonIntent = gson.toJson(intentBroadcast);
            try {
                if (intentFile.exists()){
                    intentFile.delete();
                }

                FileUtils.writeToFile(intentFile.getAbsolutePath(), jsonIntent);

                Logger.fs("File saved: " + intentFile.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        loadSavedIntents();
    }

    private void loadSavedIntents() {
        savedIntentsListItems.clear();
        intents.clear();

        for (File saved: Preferences.getInstance().getIntentsFolder().listFiles()){
            Logger.d("Read: " + saved.getName());
            String intent;
            if (saved.getName().startsWith(".")) {
                Logger.e("Will not try to read: " + saved);
                continue;
            }

            try {
                intent = FileUtils.readFile(saved.getAbsolutePath());
                Gson gson = new Gson();
                IntentBroadcast intentBroadcast = gson.fromJson(intent, IntentBroadcast.class);

                savedIntentsListItems.add(saved.getName());
                intents.add(intentBroadcast);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onButtonDelete(ActionEvent actionEvent) {
        if (listSaved.getSelectionModel().getSelectedItem() == null) {
            DialogUtil.showErrorDialog("Select intent to delete");
        } else {
            File file = new File(Preferences.getInstance().getIntentsFolder(), listSaved.getSelectionModel().getSelectedItem());

            file.delete();

            Logger.fs("File deleted: " + file.getAbsolutePath());

            loadSavedIntents();
        }
    }
}
