package java.beans;

import java.util.EventListenerProxy;

public class PropertyChangeListenerProxy extends EventListenerProxy implements PropertyChangeListener {

	String propertyName;

	public PropertyChangeListenerProxy(String propertyName, PropertyChangeListener listener) {
		super(listener);
		this.propertyName = propertyName;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void propertyChange(PropertyChangeEvent event) {
		PropertyChangeListener listener = (PropertyChangeListener) getListener();
		listener.propertyChange(event);
	}
}