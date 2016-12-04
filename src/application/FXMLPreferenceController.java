package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

import application.Preferences.PreferenceObj;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class FXMLPreferenceController implements Initializable{

	@FXML
    private Label label;

    @FXML
    private TextField textFieldAdbPath;

    @FXML
    private TextField textFieldAPKsFolders;

    @FXML
    private TextField textFieldObfuscatioinToolPath;

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
    	Preferences.getInstance().setAdbPath(textFieldAdbPath.getText());
    	Preferences.getInstance().setAPKsFoldersPlain(textFieldAPKsFolders.getText());
    	Preferences.getInstance().setObfuscationToolPath(textFieldObfuscatioinToolPath.getText());
    	try {
			Preferences.getInstance().save();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		textFieldAdbPath.setText(Preferences.getInstance().getAdbPath());
		textFieldAPKsFolders.setText(Preferences.getInstance().getAPKsFoldersPlain());
		textFieldObfuscatioinToolPath.setText(Preferences.getInstance().getObfuscationToolPath());

	}

	public void onDownloadJadxClicked(ActionEvent actionEvent) {
		Main.hostService.showDocument("https://github.com/skylot/jadx");
	}
}
