package application;

import application.log.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class FileUtils {
	public static String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, Charset.defaultCharset());
	}

	public static void writeToFile(String path, String jsonCommandBatch) throws IOException {

		if (new File(path).exists()){
			new File(path).delete();
		}

		Files.write(Paths.get(path), jsonCommandBatch.getBytes(), StandardOpenOption.CREATE);
	}

	public static void getFilesInFolderRecursivly(String directoryName, ArrayList<File> files) {
	    File directory = new File(directoryName);

	    // get all the files from a directory
	    File[] fList = directory.listFiles();
	    for (File file : fList) {
	        if (file.isFile()) {
	            files.add(file);
	        } else if (file.isDirectory()) {
	        	getFilesInFolderRecursivly(file.getAbsolutePath(), files);
	        }
	    }
	}
}
