package application.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.AdbUtils;
import application.log.Logger;
import application.model.Application;
import application.model.Device;
import application.model.Model;
import application.model.PackageProcess;

public class ApplicationsMonitorService {

    protected static final long INTERVAL_DURATION = 5000;
    public static ApplicationsMonitorService instance = new ApplicationsMonitorService();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean working;

    private ApplicationsMonitorService() {

    }

    public synchronized void start() {
        working = true;
        executor.execute(workRunnable);
    }

    public synchronized void stop() {
        working = false;
    }

    public synchronized void shutDown() {
        executor.shutdownNow();
    }

    Runnable workRunnable = new Runnable() {

        @Override
        public void run() {

            while (working) {
                Device selectedDevice = Model.instance.getSelectedDevice();

                if (selectedDevice != null) {
                    List<Application> applications = new ArrayList<>();
                    Application application = null;

                    Map<String, ArrayList<PackageProcess>> packageProcesses = getRunningProcesses();

                    String result = AdbUtils.run("shell pm list packages");

                    String[] split = result.split("\n");


                    for (int i = 1; i < split.length; i++) {
                        String packageName = split[i].replace("package:", "").trim();
                        if (packageName.equals("android")) {
                            continue;
                        }

                        application = new Application();
                        application.setApplicationName(packageName);
                        application.setPackageName(packageName);

                        if (packageProcesses.containsKey(packageName)) {
                            application.setRunning(true);
                            application.setPackageProcesses(packageProcesses.get(packageName));
                        } else {
                            application.setPackageProcesses(null);
                        }

                        applications.add(application);
                    }

					/*String result = AdbUtils.run("shell dumpsys package packages");
                    String[] split = result.split("\n");
					for (int i = 0; i < split.length; i++){
						String line = split[i].trim();
						if (line.startsWith("Package ")){
							if (application != null){
								applications.add(application);
							}
							application = new Application();
							application.setApplicationName(line.substring(line.indexOf("["), line.indexOf("]")));
							application.setPackageName(line.substring(line.indexOf("[") + 1, line.indexOf("]")));

							Logger.d(application.getApplicationName());
						}
					}*/
                    selectedDevice.checkApplicationFound(applications);
                }

                try {
                    Thread.sleep(INTERVAL_DURATION);
                } catch (InterruptedException e) {
                }
            }
        }
    };


    protected Map<String, ArrayList<PackageProcess>> getRunningProcesses() {
        Map<String, ArrayList<PackageProcess>> map = new HashMap<>();
        String result = AdbUtils.run("shell ps");
        String[] split = result.split("\n");
        for (int i = 1; i < split.length; i++) {

            PackageProcess packageProcess = new PackageProcess();
            String[] process = split[i].split("\\s+");
            try {
                packageProcess.process = process[process.length - 1];
                packageProcess.PID = process[1];

                ArrayList<PackageProcess> packageProcesses;
                String packageName = packageProcess.process.split(":")[0];
                //Logger.d(packageName + " " + packageProcess.process + " " + packageProcess.PID);
                if (map.containsKey(packageName)) {
                    packageProcesses = map.get(packageName);
                } else {
                    packageProcesses = new ArrayList<PackageProcess>();
                }

                packageProcesses.add(packageProcess);

                map.put(packageName, packageProcesses);
            } catch (Exception e) {
                Logger.e("Error loading app: " + split[i]);
            }
        }

        return map;
    }
}
