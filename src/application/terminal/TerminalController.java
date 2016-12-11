package application.terminal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

import application.AdbUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class TerminalController implements Initializable{

	@FXML
    private Label label;
    @FXML
    private TextArea actiontarget;

    @FXML
    private TextField textField;

    @FXML
    private void handleSubmitButtonAction(ActionEvent event) {

    	String result = AdbUtils.run(textField.getText());


        actiontarget.setText(result);
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

}
