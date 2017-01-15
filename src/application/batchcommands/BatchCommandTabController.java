package application.batchcommands;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import application.AdbUtils;
import application.DialogUtil;
import application.FileUtils;
import application.model.Model;
import application.preferences.Preferences;
import com.google.gson.Gson;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import application.log.Logger;
import application.model.Command;
import application.model.CommandBatch;

public class BatchCommandTabController implements Initializable {

	ObservableList<String> batchesListItems = FXCollections.observableArrayList();

	ArrayList<CommandBatch> commandBatches = Model.instance.getCommandBatches();

	@FXML
	private ListView<String> listBatches;

	@FXML
	private BatchCommandsListView listCommands;

	@FXML
	private TextArea textAreaLog;
	private int runningIndex = -1;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		listBatches.setItems(batchesListItems);

		loadBatches();

		listBatches.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> ov, String old_val, String new_val) {
				updateCommandsList();
			}
		});
	}

	protected void updateCommandsList() {

		int index = listBatches.getSelectionModel().getSelectedIndex();

		if (index > -1) {
			CommandBatch commandBatch = commandBatches.get(index);
			listCommands.update(commandBatch.commands, runningIndex);
		}
	}

	@FXML
	private void handleRunBatchClicked(ActionEvent event) {

		int index = listBatches.getSelectionModel().getSelectedIndex();

		if (index > -1) {
			CommandBatch commandBatch = commandBatches.get(index);
			runCommands(commandBatch, 0, false);
		} else {
			DialogUtil.showErrorDialog("Please select command batch");
		}
	}

	@FXML
	private void handleRunSingleCommandClicked(ActionEvent event) {
		int index = listBatches.getSelectionModel().getSelectedIndex();

		if (index > -1) {
			int commandIndex = listCommands.getSelectionModel().getSelectedIndex();
			if (commandIndex > -1){
				CommandBatch commandBatch = commandBatches.get(index);
				runCommands(commandBatch, commandIndex, true);
			}
		}
	}

	@FXML
	private void handleRunForwardSingleCommandClicked(ActionEvent event) {
		int index = listBatches.getSelectionModel().getSelectedIndex();

		if (index > -1) {
			int commandIndex = listCommands.getSelectionModel().getSelectedIndex();
			if (commandIndex > -1){
				CommandBatch commandBatch = commandBatches.get(index);
				runCommands(commandBatch, commandIndex, false);
			}
		} else {
			DialogUtil.showErrorDialog("Please select command to run from");
		}
	}

	public void runCommands(CommandBatch commandBatch, int runFromIndex, boolean singleCommand) {
		AdbUtils.executor.execute(new Runnable() {

			@Override
			public void run() {
				int toIndex = (singleCommand ? runFromIndex + 1 : commandBatch.commands.size());
				for (int i = runFromIndex; i < toIndex; i++) {
					updateRunningIndex(i);
					executeCommand(commandBatch, i);
					Logger.fs("Finished batch commands: " + commandBatch.name);
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

	protected void executeCommand(CommandBatch commandBatch, int i) {
		Logger.ds("Running command: " + commandBatch.name + " " + commandBatch.commands.get(i).description);

		updateRunningIndex(i);
		Command command = commandBatch.commands.get(i);
		log("Run: " + command.command);
		log(AdbUtils.run(command));
		updateRunningIndex(-1);
	}


	@FXML
	private void handleCreateNewClicked(ActionEvent event) {
		try {
			BatchCommandEditController.showScreen(getClass(), null, null, batchCommandEditControllerListener);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleEditNewClicked(ActionEvent event) {
		int index = listBatches.getSelectionModel().getSelectedIndex();

			if (index > -1) {
			try {
				CommandBatch commandBatch = commandBatches.get(index);

				BatchCommandEditController.showScreen(getClass(), commandBatch, listBatches.getSelectionModel().getSelectedItem(), batchCommandEditControllerListener);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			log("select batch to edit");
		}
	}

	@FXML
	private void handleCopyClicked(ActionEvent event) {
		int index = listBatches.getSelectionModel().getSelectedIndex();

		if (index > -1) {
			try {
				CommandBatch commandBatch = commandBatches.get(index);

				BatchCommandEditController.showScreen(getClass(), commandBatch, null, batchCommandEditControllerListener);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			log("select batch to copy");
		}
	}

	@FXML
	private void handleDeleteClicked(ActionEvent event) {
		int index = listBatches.getSelectionModel().getSelectedIndex();

		if (index > -1) {
			CommandBatch commandBatch = commandBatches.get(index);
			new File(Preferences.getInstance().getCommandFolder(), listBatches.getSelectionModel().getSelectedItem()).delete();
			loadBatches();


		} else {
			log("select batch to delete");
		}
	}

	private void log(String text) {
		textAreaLog.appendText("\n" + text);
	}

	@FXML
	private void handleRefreshClicked(ActionEvent event) {
		loadBatches();
	}

	private BatchCommandEditController.BatchCommandEditControllerListener batchCommandEditControllerListener = new BatchCommandEditController.BatchCommandEditControllerListener() {
		@Override
		public void onBatchCommandUpdated() {
			loadBatches();
		}
	};

	private void loadBatches() {

		Logger.d("Working Directory = " + System.getProperty("user.dir"));
		batchesListItems.clear();
		listCommands.clear();

		Model.instance.loadCommandBatches();

		for (CommandBatch commandBatch : commandBatches) {
			batchesListItems.add(commandBatch.name);
		}
	}
}
