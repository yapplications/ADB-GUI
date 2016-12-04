package application.model;

public class Command {
	public static final String TYPE_FREE_TEXT = "FREE_TEXT";
	public static final String TYPE_ADB = "ADB";

	public String command = "";
	public String description = "";
	public String type = TYPE_ADB;
}
