package application.model;

import java.util.Set;

import javafx.application.Platform;

public interface ModelListener {
	void onChangeModelListener();

	static void notify(Set<ModelListener> modelListeners) {
		if (modelListeners != null) {

			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					for (ModelListener listener : modelListeners) {
						listener.onChangeModelListener();
					}
				}
			});
		}
	}
}
