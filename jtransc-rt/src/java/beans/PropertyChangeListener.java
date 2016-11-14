package java.beans;

import java.util.EventListener;

public interface PropertyChangeListener extends EventListener {
	public void propertyChange(PropertyChangeEvent event);
}