package application.batchcommands;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by evgeni.shafran on 11/13/16.
 */
public class CommandWizardController implements Initializable {
    public RadioButton radioButtonInputText;
    public RadioButton radioButtonInputTab;
    public RadioButton radioButtonInputEnter;
    public TextField textFieldInputText;
    public RadioButton radioButtonInputPower;
    public RadioButton radioButtonInputVolumeUp;
    public RadioButton radioButtonInputVolumeDown;
    public RadioButton radioButtonInputRecent;
    public RadioButton radioButtonInputHome;
    public RadioButton radioButtonInputBack;
    public RadioButton radioButtonFilesPush;
    public RadioButton radioButtonFilesPull;
    public RadioButton radioButtonFilesInstallApk;
    public TextField textFieldFilesPushFrom;
    public TextField textFieldFilesPushTo;
    public TextField textFieldFilesPullFrom;
    public TextField textFieldFilesPullTo;
    public TextField textFieldFilesApkPath;
    public RadioButton radioButtonFilesUnInstallApk;
    public TextField textFieldFilesUninstallApp;
    public RadioButton radioButtonFilesClearData;
    public TextField textFieldFilesClearData;
    public RadioButton radioButtonFilesOpenApp;
    public TextField textFieldFilesOpenApp;
    private CommandWizardControllerListener commandWizardControllerListener;

    public interface CommandWizardControllerListener {
        public void onCommandSelected(String command, String description);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public static void showScreen(Class class1, CommandWizardControllerListener commandWizardControllerListener) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(class1.getResource("/application/batchcommands/CommandWizardLayout.fxml"));

        Parent root1 = (Parent) fxmlLoader.load();

        CommandWizardController controller = fxmlLoader.<CommandWizardController>getController();
        controller.setCommandWizardControllerListener(commandWizardControllerListener);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Commands Wizard");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    private void setCommandWizardControllerListener(CommandWizardControllerListener commandWizardControllerListener) {
        this.commandWizardControllerListener = commandWizardControllerListener;
    }

    public void addCommandClicked(ActionEvent actionEvent) {
        String command = "";
        String description = "";

        if (radioButtonInputText.isSelected()){
            command = "shell input text '" + textFieldInputText.getText() + "'";
            description = "Enter text";
        } else if (radioButtonInputTab.isSelected()){
            command = "shell input keyevent KEYCODE_TAB";
            description = "Next field";
        } else if (radioButtonInputEnter.isSelected()){
            command = "shell input keyevent KEYCODE_ENTER";
            description = "Submit";
        } else if (radioButtonInputPower.isSelected()){
            command = "shell input keyevent KEYCODE_POWER";
            description = "Power";
        } else if (radioButtonInputVolumeDown.isSelected()){
            command = "shell input keyevent KEYCODE_VOLUME_DOWN";
            description = "Volume down";
        } else if (radioButtonInputVolumeUp.isSelected()){
            command = "shell input keyevent KEYCODE_VOLUME_UP";
            description = "Volume up";
        } else if (radioButtonInputBack.isSelected()){
            command = "shell input keyevent KEYCODE_BACK";
            description = "Back";
        } else if (radioButtonInputHome.isSelected()){
            command = "shell input keyevent KEYCODE_HOME";
            description = "Home";
        } else if (radioButtonInputRecent.isSelected()){
            command = "shell input keyevent KEYCODE_APP_SWITCH";
            description = "Recent";
        }

        else if (radioButtonFilesPush.isSelected()){
            command = "push " + textFieldFilesPushFrom.getText() + "" + " " + textFieldFilesPushTo.getText() + "";
            description = "Push file to device";
        } else if (radioButtonFilesPull.isSelected()){
            command = "pull " + textFieldFilesPullFrom.getText() + "" + " " + textFieldFilesPullTo.getText() + "";
            description = "Pull file from device";
        } else if (radioButtonFilesInstallApk.isSelected()){
            command = "install -r " + textFieldFilesApkPath.getText() + "";
            description = "Install Apk";
        } else if (radioButtonFilesUnInstallApk.isSelected()){
            command = "uninstall " + textFieldFilesUninstallApp.getText();
            description = "Uninstall app: " + textFieldFilesUninstallApp.getText();
        } else if (radioButtonFilesClearData.isSelected()){
            command = "shell pm clear " + textFieldFilesClearData.getText();
            description = "Clear data: " + textFieldFilesClearData.getText();
        } else if (radioButtonFilesOpenApp.isSelected()){
            command = "shell monkey -p " + textFieldFilesOpenApp.getText() + " 1";
            description = "Open app: " + textFieldFilesOpenApp.getText();
        }

        commandWizardControllerListener.onCommandSelected(command, description);

        ((Stage) radioButtonInputText.getScene().getWindow()).close();
    }
}
