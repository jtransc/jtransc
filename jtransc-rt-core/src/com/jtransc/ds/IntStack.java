package com.jtransc.ds;

import java.util.Arrays;

public class IntStack {
	private int[] data;
	private int offset;

	public IntStack() {
		this(64);
	}

	public IntStack(int capacity) {
		this.data = new int[Math.max(1, capacity)];
	}

	private void ensure(int count) {
		int requiredSize = offset + count;
		if (requiredSize > data.length) {
			this.data = Arrays.copyOf(this.data, Math.max(requiredSize, data.length * 2));
		}
	}

	public void push(int value) {
		ensure(1);
		this.data[offset++] = value;
	}

	public int pop() {
		if (offset <= 0) throw new StackOverflowError();
		return this.data[--offset];
	}

	public int getLength() {
		return this.offset;
	}
}
