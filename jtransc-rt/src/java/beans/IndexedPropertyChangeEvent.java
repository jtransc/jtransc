package java.beans;

public class IndexedPropertyChangeEvent extends PropertyChangeEvent {
	private final int index;

	public IndexedPropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue, int index) {
		super(source, propertyName, oldValue, newValue);
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}