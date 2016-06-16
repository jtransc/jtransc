package java.lang;

public class ThreadLocal<T> {
	private T value;

	protected T initialValue() {
		return null;
	}

	public ThreadLocal() {
		value = initialValue();
	}

	public T get() {
		return value;
	}

	private T setInitialValue() {
		this.value = initialValue();
		return value;
	}

	public void set(T value) {
		this.value = value;
	}

	public void remove() {
		this.value = null;
	}
}
