package jtransc.game.util;

import java.util.ArrayList;

public class Array2<T> {
	public final int width;
	public final int height;
	public final ArrayList<T> values;

	public interface Generator<T> {
		T generate();
	}

	public Array2(int width, int height, Generator<T> generate) {
		final int size = width * height;
		this.width = width;
		this.height = height;
		this.values = new ArrayList<T>();
		for (int n = 0; n < size; n++) this.values.add(generate.generate());
	}

	private int index(int x, int y) {
		return y * width + x;
	}

	public T get(int x, int y) {
		return values.get(index(x, y));
	}

	public void set(int x, int y, T value) {
		values.set(index(x, y), value);
	}
}
