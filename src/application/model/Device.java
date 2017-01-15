package application.model;

import application.log.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Device {
	String id;
	private String model;
	private boolean isEmulator;
	private String name;
	private String androidVersion;
	private List<Application> applications = new ArrayList<Application>();

	Set<ModelListener> modelListeners = new HashSet<>();

	public synchronized void addModelListener(ModelListener modelListener){
		modelListeners.add(modelListener);
	}

	public synchronized void removeModelListener(ModelListener modelListener){
		modelListeners.remove(modelListener);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof Device) {
			return id.equals(((Device) obj).id);
		}

		return super.equals(obj);
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getModel() {
		return model;
	}

	public boolean isEmulator() {
		return isEmulator;
	}

	public void setEmulator(boolean isEmulator) {
		this.isEmulator = isEmulator;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getAndroidVersion() {
		return androidVersion;
	}

	public void setAndroidVersion(String androidVersion) {
		this.androidVersion = androidVersion;
	}

	public List<Application> getApplications() {
		return applications;
	}

	public synchronized void checkApplicationFound(List<Application> foundApplications) {
		List<Application> availbleApplications = getApplications();
		boolean changed = false;

		for (Application application: foundApplications){

			if (!availbleApplications.contains(application)){
				availbleApplications.add(application);
				changed = true;
				//Logger.d("Device Add " + application.getApplicationName());
			} else {
				int index = availbleApplications.indexOf(application);
				Application availbleApplication = availbleApplications.get(index);

				if (!availbleApplication.isHasSameValues(application)){
					availbleApplications.remove(index);
					availbleApplications.add(index, application);
					//Logger.d("Device Change " + application.getApplicationName());

					changed = true;
				}
			}
		}

		Iterator<Application> i = availbleApplications.iterator();
		while (i.hasNext()) {
			Application applicationExisting = i.next(); // must be called before you can
												// call i.remove()

			if (!foundApplications.contains(applicationExisting)) {
				//Logger.d("Device Remove " + applicationExisting.getApplicationName());

				// not exists
				i.remove();
				changed = true;

			}
		}

		if (changed) {
			//Logger.d("Application change detected");
			notifyListeners();
		}
	}

	private synchronized void notifyListeners() {
		ModelListener.notify(modelListeners);
	}

	public boolean copy(Device copyDevice) {
		boolean changed = false;

		if (isChanged(name, copyDevice.name)){
			Logger.d("Name changed: " + name + " " + copyDevice.name);
			name = copyDevice.name;
			changed = true;
		}

		if (isChanged(model, copyDevice.model)){
			Logger.d("Model changed: " + model + " " + copyDevice.model);

			model = copyDevice.model;
			changed = true;
		}

		if (isChanged(androidVersion, copyDevice.androidVersion)){
			Logger.d("androidVersion changed: " + androidVersion + " " + copyDevice.androidVersion);

			androidVersion = copyDevice.androidVersion;
			changed = true;
		}

		return changed;
	}

	private boolean isChanged(String param1, String param2) {
		return ((param1 == null && param2 != null) ||
				(param1 != null && ! param1.equals(param2)));
	}
}
