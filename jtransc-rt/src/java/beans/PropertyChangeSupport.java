package java.beans;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class PropertyChangeSupport implements Serializable {
	private static final ObjectStreamField[] serialPersistentFields = {
		new ObjectStreamField("source", Object.class),
		new ObjectStreamField("children", Object.class),
		new ObjectStreamField("propertyChangeSupportSerializedDataVersion", int.class),
	};

	private transient Object sourceBean;

	private transient List<PropertyChangeListener> listeners = new CopyOnWriteArrayList<PropertyChangeListener>();

	public PropertyChangeSupport(Object sourceBean) {
		if (sourceBean == null) throw new NullPointerException("sourceBean == null");
		this.sourceBean = sourceBean;
	}

	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		firePropertyChange(new PropertyChangeEvent(sourceBean, propertyName, oldValue, newValue));
	}

	public void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
		firePropertyChange(new IndexedPropertyChangeEvent(sourceBean,
			propertyName, oldValue, newValue, index));
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		for (PropertyChangeListener p : listeners) {
			if (equals(propertyName, listener, p)) {
				listeners.remove(p);
				return;
			}
		}
	}

	private boolean equals(String aName, EventListener a, EventListener b) {
		while (b instanceof PropertyChangeListenerProxy) {
			PropertyChangeListenerProxy bProxy = (PropertyChangeListenerProxy) b; // unwrap b
			String bName = bProxy.getPropertyName();
			b = bProxy.getListener();
			if (aName == null) {
				if (!(a instanceof PropertyChangeListenerProxy)) return false;
				PropertyChangeListenerProxy aProxy = (PropertyChangeListenerProxy) a; // unwrap a
				aName = aProxy.getPropertyName();
				a = aProxy.getListener();
			}
			if (!Objects.equals(aName, bName)) return false; // not equal; a and b subscribe to different properties
			aName = null;
		}
		return aName == null && Objects.equals(a, b);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		if (listener != null && propertyName != null) {
			listeners.add(new PropertyChangeListenerProxy(propertyName, listener));
		}
	}

	public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
		List<PropertyChangeListener> result = new ArrayList<PropertyChangeListener>();
		for (PropertyChangeListener p : listeners) {
			if (p instanceof PropertyChangeListenerProxy && Objects.equals(
				propertyName, ((PropertyChangeListenerProxy) p).getPropertyName())) {
				result.add(p);
			}
		}
		return result.toArray(new PropertyChangeListener[result.size()]);
	}

	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
		firePropertyChange(propertyName, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
	}

	public void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
		if (oldValue != newValue) {
			fireIndexedPropertyChange(propertyName, index, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
		}
	}

	public void firePropertyChange(String propertyName, int oldValue, int newValue) {
		firePropertyChange(propertyName, Integer.valueOf(oldValue), Integer.valueOf(newValue));
	}

	public void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
		if (oldValue != newValue) {
			fireIndexedPropertyChange(propertyName, index, Integer.valueOf(oldValue), Integer.valueOf(newValue));
		}
	}

	public boolean hasListeners(String propertyName) {
		for (PropertyChangeListener p : listeners) {
			if (!(p instanceof PropertyChangeListenerProxy) || Objects.equals(
				propertyName, ((PropertyChangeListenerProxy) p).getPropertyName())) {
				return true;
			}
		}
		return false;
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		for (PropertyChangeListener p : listeners) {
			if (equals(null, listener, p)) {
				listeners.remove(p);
				return;
			}
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return listeners.toArray(new PropertyChangeListener[0]); // 0 to avoid synchronization
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		Map<String, PropertyChangeSupport> map = new Hashtable<String, PropertyChangeSupport>();
		for (PropertyChangeListener p : listeners) {
			if (p instanceof PropertyChangeListenerProxy && !(p instanceof Serializable)) {
				PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) p;
				PropertyChangeListener listener = (PropertyChangeListener) proxy.getListener();
				if (listener instanceof Serializable) {
					PropertyChangeSupport list = map.get(proxy.getPropertyName());
					if (list == null) {
						list = new PropertyChangeSupport(sourceBean);
						map.put(proxy.getPropertyName(), list);
					}
					list.listeners.add(listener);
				}
			}
		}

		ObjectOutputStream.PutField putFields = out.putFields();
		putFields.put("source", sourceBean);
		putFields.put("children", map);
		out.writeFields();

		for (PropertyChangeListener p : listeners) {
			if (p instanceof Serializable) {
				out.writeObject(p);
			}
		}
		out.writeObject(null);
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		ObjectInputStream.GetField readFields = in.readFields();
		sourceBean = readFields.get("source", null);
		listeners = new CopyOnWriteArrayList<PropertyChangeListener>();

		Map<String, PropertyChangeSupport> children = (Map<String, PropertyChangeSupport>) readFields.get("children", null);
		if (children != null) {
			for (Map.Entry<String, PropertyChangeSupport> entry : children.entrySet()) {
				for (PropertyChangeListener p : entry.getValue().listeners) {
					listeners.add(new PropertyChangeListenerProxy(entry.getKey(), p));
				}
			}
		}

		PropertyChangeListener listener;
		while ((listener = (PropertyChangeListener) in.readObject()) != null) {
			listeners.add(listener);
		}
	}

	public void firePropertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();
		Object oldValue = event.getOldValue();
		Object newValue = event.getNewValue();
		if (newValue != null && oldValue != null && newValue.equals(oldValue)) return;

		notifyEachListener:
		for (PropertyChangeListener p : listeners) {
			// unwrap listener proxies until we get a mismatched name or the real listener
			while (p instanceof PropertyChangeListenerProxy) {
				PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) p;
				if (!Objects.equals(proxy.getPropertyName(), propertyName)) continue notifyEachListener;
				p = (PropertyChangeListener) proxy.getListener();
			}
			p.propertyChange(event);
		}
	}
}