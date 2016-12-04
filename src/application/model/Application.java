package application.model;

import java.util.ArrayList;

public class Application {
	private String packageName;
	private String applicationName;
	private boolean isRunning = false;
	private ArrayList<PackageProcess> packageProcesses;
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public boolean isRunning() {
		return isRunning;
	}
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof Application && packageName != null) {
			return packageName.equals(((Application) obj).packageName);
		} else if (obj instanceof String && packageName != null) {
			return packageName.equals(obj);
		}

		return super.equals(obj);
	}
	public boolean isHasSameValues(Application application) {
		return packageName.equals(application.packageName) && isRunning == application.isRunning && sameProcessSize(application.packageProcesses);
	}

	private boolean sameProcessSize(ArrayList<PackageProcess> packageProcesses2) {
		if (packageProcesses == null && packageProcesses2 == null){
			return true;
		}

		if (packageProcesses != null && packageProcesses2 != null && packageProcesses.size() == packageProcesses2.size()){
			return true;
		}

		return false;
	}
	public ArrayList<PackageProcess> getPackageProcesses() {
		return packageProcesses;
	}
	public void setPackageProcesses(ArrayList<PackageProcess> packageProcesses) {
		this.packageProcesses = packageProcesses;
	}
}
