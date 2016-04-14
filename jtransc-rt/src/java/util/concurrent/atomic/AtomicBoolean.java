package java.util.concurrent.atomic;

public class AtomicBoolean implements java.io.Serializable {
	private boolean value;

	public AtomicBoolean(boolean initialValue) {
		value = initialValue;
	}

	public AtomicBoolean() {
	}

	public final boolean get() {
		return value;
	}

	public final void set(boolean newValue) {
		this.value = newValue;
	}

	public final boolean getAndSet(boolean newValue) {
		boolean prev = get();
		this.value = newValue;
		return prev;
	}

	public final boolean compareAndSet(boolean expect, boolean update) {
		if (this.value != expect) return false; // Should return true too to avoid infinite loops?
		this.value = update;
		return true;
	}

	public boolean weakCompareAndSet(boolean expect, boolean update) {
		return compareAndSet(expect, update);
	}

	public final void lazySet(boolean newValue) {
		set(newValue);
	}

	public String toString() {
		return Boolean.toString(get());
	}

}
