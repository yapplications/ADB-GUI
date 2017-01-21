package application;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;

/**
 * Created by evgeni.shafran on 1/21/17.
 */
public class CssUtill {
    public static void apply(Scene scene) {
        scene.getStylesheets().add("/res/application.css");
    }

    public static void setBackgroud(Node node) {
        node.setStyle("-fx-background-color: #3c3f41;");
    }
}
