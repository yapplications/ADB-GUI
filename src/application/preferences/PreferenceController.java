package application.preferences;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

import application.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PreferenceController implements Initializable{

	public Label labelVersion;
	@FXML
    private Label label;

/*
    @FXML
    private TextField textFieldAdbPath;
*/

    @FXML
    private TextField textFieldAPKsFolders;

    @FXML
    private TextField textFieldObfuscatioinToolPath;

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
    	//Preferences.getInstance().setAdbPath(textFieldAdbPath.getText());
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
		//textFieldAdbPath.setText(Preferences.getInstance().getAdbPath());
		textFieldAPKsFolders.setText(Preferences.getInstance().getAPKsFoldersPlain());
		textFieldObfuscatioinToolPath.setText(Preferences.getInstance().getObfuscationToolPath());

		labelVersion.setText("Version: " + getVersion());
	}

	public void onDownloadJadxClicked(ActionEvent actionEvent) {
		Main.hostService.showDocument("https://github.com/skylot/jadx");
	}

	public void handleGitHubLink(ActionEvent actionEvent) {
		Main.hostService.showDocument("https://github.com/yapplications/ADB-GUI");
	}

	public void handleContactUsLink(ActionEvent actionEvent) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.MAIL)) {
				try {
					String version = getVersion();
					desktop.mail(new URI("mailto:support@yapplications.com?subject=ADB-GUI%20" + version + "%20support")); // alternately, pass a mailto: URI in here
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String getVersion() {
		Package p = getClass().getPackage();
		return "0.1.9v";// p.getImplementationVersion();
	}

	public void handleYapplicationsLink(ActionEvent actionEvent) {
		Main.hostService.showDocument("https://play.google.com/store/apps/dev?id=4971752875596401073&hl=en");
	}
}
