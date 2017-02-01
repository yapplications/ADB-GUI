package application.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.AdbUtils;
import application.log.Logger;
import application.model.Device;
import application.model.Model;

public class DeviceMonitorService {

	protected static final long INTERVAL_DURATION = 1000;
	public static DeviceMonitorService instance = new DeviceMonitorService();
    ExecutorService executor = Executors.newSingleThreadExecutor();
	private boolean working;
	private boolean justStart;

	private DeviceMonitorService(){

	}

	public synchronized void startMonitoringDevices(){
		working = true;
		executor.execute(monitorRunnable);
	}

	public synchronized void stopMonitoringDevices(){
		working = false;
		Model.instance.clearDevices();

		justStart = true;
	}

	public synchronized void shutDown(){
		executor.shutdownNow();
	}

	Runnable monitorRunnable = new Runnable() {

		@Override
		public void run() {
			while (working)
			{
				//Logger.d("run devices");

				String result = AdbUtils.run("devices -l");
				String[] split = result.split("\n");
				//Logger.d("devices: " + result);

				List<Device> devices = new ArrayList<>();

				for (int i = 1; i < split.length; i++){
					String line = split[i];
					String[] deviceDescriptionSplit = line.split("\\s+");
					String model = "Device";
					for (String descriptionTemp : deviceDescriptionSplit){
						if (descriptionTemp.contains("model:")){
							model = descriptionTemp.split("model:")[1];
						}
					}

					Device device = new Device();
					device.setId(deviceDescriptionSplit[0]);
					device.setModel(model);
					devices.add(device);
				}

				Model.instance.checkDevicesFound(devices);

				try {
					Thread.sleep(INTERVAL_DURATION);
				} catch (InterruptedException e) {}
			}

			Logger.d("Someone stopped me!!!");
		}
	};
}
