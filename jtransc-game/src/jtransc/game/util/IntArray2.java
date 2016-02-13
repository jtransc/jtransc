package jtransc.game.util;

public class IntArray2<T> {
	public final int width;
	public final int height;
	public final int[] values;

	public interface Generator<T> {
		T generate();
	}

	public IntArray2(int width, int height) {
		this.width = width;
		this.height = height;
		this.values = new int[width * height];
	}

	private int index(int x, int y) {
		return y * width + x;
	}

	public int get(int x, int y) {
		return values[index(x, y)];
	}

	public void set(int x, int y, int value) {
		values[index(x, y)] = value;
	}
}
