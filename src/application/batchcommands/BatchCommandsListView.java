package application.batchcommands;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import application.model.Command;

import java.util.List;

/**
 * Created by evgeni.shafran on 10/10/16.
 */
public class BatchCommandsListView extends ListView<String> {

    ObservableList<String> commandsListItems = FXCollections.observableArrayList();


    public void update(List<Command> commands, int runningIndex) {
        setItems(commandsListItems);
        commandsListItems.clear();
        int i = 0;

        for (Command command : commands) {
            String runningString = "";
            if (i == runningIndex) {
                runningString = "RUNNING ";
            }

            commandsListItems.add(runningString + BatchCommandViewUtil.getCommandUIString(command));

            i++;
        }
    }

    public void clear() {
        commandsListItems.clear();
    }
}
