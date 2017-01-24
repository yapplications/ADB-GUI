package application.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import application.AdbUtils;
import application.FileUtils;
import application.preferences.Preferences;
import application.log.Logger;
import com.google.gson.Gson;

public class Model {
	Device selectedDevice;

	List<Device> availableDevices = new ArrayList<Device>();

	public static volatile Model instance = new Model();

	Set<ModelListener> modelListeners = new HashSet<>();

	Set<ModelListener> selectedDeviceListeners = new HashSet<>();

	ArrayList<CommandBatch> commandBatches = new ArrayList<>();

	private Model() {

	}

	public synchronized void addModelListener(ModelListener modelListener){
		modelListeners.add(modelListener);
	}

	public synchronized void removeModelListener(ModelListener modelListener){
		modelListeners.remove(modelListener);
	}

	public synchronized void addSelectedDeviceListener(ModelListener modelListener){
		selectedDeviceListeners.add(modelListener);
	}

	public synchronized void removeSelectedDeviceListener(ModelListener modelListener){
		selectedDeviceListeners.remove(modelListener);
	}

	public synchronized List<Device> getAvailableDevices(){
		ArrayList<Device> devices = new ArrayList<Device>();
		devices.addAll(availableDevices);

		return devices;
	}

	public synchronized void checkDevicesFound(List<Device> foundDevices) {
		boolean changed = false;
		for (Device deviceFound : foundDevices) {
			int index = availableDevices.indexOf(deviceFound);
			addDeviceInfo(deviceFound);

			if (index < 0) {
				// not exists
				Logger.d("Found new device: " + deviceFound.getId());


				availableDevices.add(deviceFound);
				changed = true;
			} else {
				Device availableDevice = availableDevices.get(index);
				changed = availableDevice.copy(deviceFound) || changed;
			}
		}

		Iterator<Device> i = availableDevices.iterator();
		while (i.hasNext()) {
			Device deviceExisting = i.next(); // must be called before you can
												// call i.remove()

			if (!foundDevices.contains(deviceExisting)) {
				// not exists
				Logger.d("Lost device: " + deviceExisting.getId());
				i.remove();
				changed = true;
			}
		}

		if (changed) {
			Logger.d("Device change detected");
			notifyListeners();
		}
	}

	private void addDeviceInfo(Device deviceFaund) {
		String id = deviceFaund.getId();
		if (id.startsWith("emulator")){
			deviceFaund.setName("Emulator");
			deviceFaund.setEmulator(true);

			// set port
		} else {
			deviceFaund.setName(deviceFaund.getModel());
		}
		String[] split =
				AdbUtils.run("shell getprop ro.build.version.release").split("\n");

		if (split.length > 0){
			deviceFaund.setAndroidVersion(split[0]);
		}
	}

	private synchronized void notifyListeners() {
        ModelListener.notify(modelListeners);
	}

	public void setSelectedDevice(Device device) {
		selectedDevice = device;

        ModelListener.notify(selectedDeviceListeners);
	}

	public Device getSelectedDevice() {
		return selectedDevice;
	}

	public void clearDevices() {
		selectedDevice = null;
		availableDevices.clear();

		notifyListeners();
		ModelListener.notify(null);
	}

	public ArrayList<CommandBatch> getCommandBatches() {
		return commandBatches;
	}

	public void loadCommandBatches() {
		commandBatches.clear();

		for (File commandFile : Preferences.getInstance().getCommandFolder().listFiles()) {
			Logger.d("Read: " + commandFile.getName());
			String commands;
			if (commandFile.getName().startsWith(".")) {
				Logger.e("Will not try to read: " + commandFile);
				continue;
			}

			try {
				commands = FileUtils.readFile(commandFile.getAbsolutePath());
				Gson gson = new Gson();
				CommandBatch commandBatch = gson.fromJson(commands, CommandBatch.class);
				commandBatch.name = commandFile.getName();

				commandBatches.add(commandBatch);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
