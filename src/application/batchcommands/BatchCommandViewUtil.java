package application.batchcommands;

import application.model.Command;

public class BatchCommandViewUtil {

	public static String getCommandUIString(Command command) {
		return command.description + " (" + command.type + "): " + command.command;
	}

}
