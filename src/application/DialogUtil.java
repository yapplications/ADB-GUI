package application;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class DialogUtil {
	public static void showErrorDialog(String headerText){
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(headerText);
		//alert.setContentText("Ooops, there was an error!");

		alert.showAndWait();
	}

	public static void showInfoDialog(String headerText) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Info");
		alert.setHeaderText(headerText);
		//alert.setContentText("Ooops, there was an error!");

		alert.showAndWait();
	}
}
