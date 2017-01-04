package java.lang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

// @TODO: This is slow, this should be implemented different in each target.
public class ThreadLocal<T> {
	private Map<Long, T> valuesPerThread = new HashMap<Long, T>();
	private Set<Long> initialized = new HashSet<Long>();

	protected T initialValue() {
		return null;
	}

	public ThreadLocal() {
	}

	public static <S> ThreadLocal<S> withInitial(Supplier<? extends S> supplier) {
		return new ThreadLocal<S>() {
			@Override
			protected S initialValue() {
				return supplier.get();
			}
		};
	}

	private long initializeOnce() {
		long id = Thread.currentThread().getId();
		if (!initialized.contains(id)) {
			initialized.add(id);
			valuesPerThread.put(id, initialValue());
		}
		return id;
	}

	public T get() {
		long id = initializeOnce();
		return valuesPerThread.get(id);
	}

	public void set(T value) {
		long id = initializeOnce();
		valuesPerThread.put(id, value);
	}

	public void remove() {
		long id = initializeOnce();
		valuesPerThread.put(id, null);
	}
}
