package application.batchcommands;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import application.preferences.Preferences;
import com.google.gson.Gson;

import application.AdbUtils;
import application.DialogUtil;
import application.FileUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import application.model.Command;
import application.model.CommandBatch;
import javafx.scene.layout.VBox;

public class BatchCommandEditController implements Initializable {

    private static final int TYPE_INDEX_ADB = 0;

    @FXML
    private BatchCommandsListView listViewCommands;

    @FXML
    private TextArea textAreaLog;

    @FXML
    private TextField textFieldDescription;

    @FXML
    private TextField textFieldBatchName;

    @FXML
    private TextArea textAreaCommand;

    @FXML
    private TextField textFieldIndex;

    @FXML
    private ComboBox<String> comboBoxCommandType;

    @FXML
    private Button buttonCreateCommand;

    private CommandBatch commandBatch;

    private String previousBatchName;

    private int editIndex = -1;

    @FXML
    VBox panelCenter;

    @FXML
    Button buttonSave;

    protected int runningIndex = -1;
    private BatchCommandEditControllerListener batchCommandEditControllerListener;

    public interface BatchCommandEditControllerListener {
        void onBatchCommandUpdated();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        comboBoxCommandType.getSelectionModel().select(TYPE_INDEX_ADB);

        textFieldIndex.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkIndexIsValid(oldValue, newValue);
            }
        });
        commandBatch = new CommandBatch();
        updateCommandsList();
        updateIndexToLast();
    }

    private void updateIndexToLast() {
        textFieldIndex.setText(Integer.toString(commandBatch.commands.size()));
    }

    protected void checkIndexIsValid(String oldValue, String newValue) {
        if (newValue.trim().equals("")) {
            textFieldIndex.setText("0");
        } else {

            if (!newValue.matches("\\d*")) {
                if (newValue.matches("\\d*")) {

                } else {
                    textFieldIndex.setText(oldValue);
                }
            } else {
                int value = Integer.parseInt(newValue);
                if (value > commandBatch.commands.size()) {
                    textFieldIndex.setText(Integer.toString(commandBatch.commands.size()));
                }
            }
        }
    }

    private void log(String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textAreaLog.appendText(text + "\n");
            }
        });
    }

    @FXML
    public void onCreateCommandClicked(ActionEvent event) {
        String commandString = textAreaCommand.getText().trim();
        String description = textFieldDescription.getText().trim();
        String type = getCommandType();
        if (commandString.equals("")) {
            DialogUtil.showErrorDialog("Please enter the command line");
        } else {
            Command command;
            if (editIndex == -1) {
                command = new Command();
            } else {
                command = commandBatch.commands.get(editIndex);
            }

            command.command = commandString;
            command.description = description;
            command.type = type;

            int index = Integer.parseInt(textFieldIndex.getText());

            if (editIndex > -1) {
                commandBatch.commands.remove(editIndex);
                if (editIndex < index) {
                    index--;
                }
            }

            commandBatch.commands.add(index, command);

            updateCommandsList();
            updateIndexToLast();
            clearCommandFields();
            exitEditMode();
        }
    }

    private void updateCommandsList() {
        listViewCommands.update(commandBatch.commands, runningIndex);
    }

    private String getCommandType() {
        return Command.TYPE_ADB;
    }

    @FXML
    public void onSaveClicked(ActionEvent event) {

        if (textFieldBatchName.getText().trim().equals("")) {
            DialogUtil.showErrorDialog("Please fill Batch name");
        } else {
            File commandFile = new File(Preferences.getInstance().getCommandFolder(), textFieldBatchName.getText());

            if (commandFile.exists()
                    && (previousBatchName == null || !previousBatchName.equals(textFieldBatchName.getText()))) {
                DialogUtil.showErrorDialog("File already exists choose another name: " + commandFile);

            } else {
                Gson gson = new Gson();
                String jsonCommandBatch = gson.toJson(commandBatch);
                try {
                    if (previousBatchName != null) {
                        new File(Preferences.getInstance().getCommandFolder(), previousBatchName).delete();
                    }

                    FileUtils.writeToFile(commandFile.getAbsolutePath(), jsonCommandBatch);
                    batchCommandEditControllerListener.onBatchCommandUpdated();
                    ((Stage) textFieldBatchName.getScene().getWindow()).close();
                } catch (IOException e) {
                    e.printStackTrace();
                    log(e.getMessage());
                }
            }
        }
    }

    @FXML
    public void onCancelCommandClicked(ActionEvent event) {
        exitEditMode();
        clearCommandFields();
    }

    private void clearCommandFields() {
        textAreaCommand.setText("");
        textFieldDescription.setText("");
        updateIndexToLast();
    }

    @FXML
    public void onDeleteCommandClicked(ActionEvent event) {
        int selectedIndex = listViewCommands.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            commandBatch.commands.remove(selectedIndex);
            updateCommandsList();
            updateIndexToLast();
        } else {
            showSelectCommandFirstErrorDialog();
        }
    }

    private void showSelectCommandFirstErrorDialog() {
        DialogUtil.showErrorDialog("Please select command first");
    }

    @FXML
    public void onEditCommandClicked(ActionEvent event) {
        int selectedIndex = listViewCommands.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            editIndex = selectedIndex;
            enterEditMode();
        } else {
            showSelectCommandFirstErrorDialog();
        }
    }

    @FXML
    public void onRunAllCommandsClicked(ActionEvent event) {
        AdbUtils.executor.execute(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < commandBatch.commands.size(); i++) {
                    updateRunningIndex(i);
                    executeCommand(i);
                }

                updateRunningIndex(-1);
                log("Finished executing commands");
            }
        });
    }

    protected void updateRunningIndex(int i) {
        runningIndex = i;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                updateCommandsList();
            }
        });
    }

    protected void executeCommand(int i) {
        updateRunningIndex(i);
        Command command = commandBatch.commands.get(i);
        log("Run: " + command.command);
        log(AdbUtils.run(command));
        updateRunningIndex(-1);
    }

    @FXML
    public void onRunSelectedCommandClicked(ActionEvent event) {
        int selectedIndex = listViewCommands.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            AdbUtils.executor.execute(new Runnable() {

                @Override
                public void run() {
                    executeCommand(selectedIndex);
                    log("Finished executing command");
                }
            });
        } else {
            showSelectCommandFirstErrorDialog();
        }
    }

    private void enterEditMode() {
        buttonCreateCommand.setText("Save");
        panelCenter.setDisable(true);
        buttonSave.setDisable(true);
        Command command = commandBatch.commands.get(editIndex);
        textAreaCommand.setText(command.command);
        textFieldDescription.setText(command.description);
        textFieldIndex.setText(Integer.toString(editIndex));
    }

    private void exitEditMode() {
        editIndex = -1;
        buttonCreateCommand.setText("Create");
        panelCenter.setDisable(false);
        buttonSave.setDisable(false);
    }

    public static void showScreen(Class class1, CommandBatch commandBatch, String name,
                                  BatchCommandEditControllerListener batchCommandEditControllerListener) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(class1.getResource("/application/batchcommands/BatchCommandEditLayout.fxml"));

        Parent root1 = (Parent) fxmlLoader.load();

        BatchCommandEditController controller = fxmlLoader.<BatchCommandEditController>getController();
        controller.setCommandBatch(commandBatch, name);
        controller.setBatchCommandEditControllerListener(batchCommandEditControllerListener);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Create new batch");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    private void setBatchCommandEditControllerListener(BatchCommandEditControllerListener batchCommandEditControllerListener) {
        this.batchCommandEditControllerListener = batchCommandEditControllerListener;
    }

    private void setCommandBatch(CommandBatch commandBatch, String name) {
        this.previousBatchName = name;

        if (previousBatchName != null) {
            textFieldBatchName.setText(previousBatchName);
        }

        if (commandBatch != null) {
            this.commandBatch = commandBatch;
            updateCommandsList();
            updateIndexToLast();
        }
    }

    public void onCommandWizardClicked(ActionEvent actionEvent) {
        try {
            CommandWizardController.showScreen(getClass(), new CommandWizardController.CommandWizardControllerListener() {
                @Override
                public void onCommandSelected(String command, String description) {
                    textAreaCommand.setText(command);
                    textFieldDescription.setText(description);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
