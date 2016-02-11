package jtransc.game.util;

import java.util.ArrayList;

public class StackPool<T> {
	public interface Generator<T> {
		T generate(int index);
	}

	final int capacity;
	final Generator<T> generator;
	private ArrayList<T> values = new ArrayList<T>();
	private int index = 0;

	public StackPool(int capacity, Generator<T> generator) {
		this.capacity = capacity;
		this.generator = generator;
		ensure(capacity);
	}

	private void ensure(int count) {
		while (values.size() < count) values.add(generator.generate(values.size()));
	}

	public int getLength() {
		return index;
	}

	public T push() {
		ensure(index);
		return values.get(index++);
	}

	public T pop() {
		return values.get(--index);
	}

	public void clear() {
		index = 0;
	}
}
