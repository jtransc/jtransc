package com.jtransc.ds;

public class IntPool {
	private int lastId = 0;
	private IntStack available = new IntStack();

	public int alloc() {
		if (available.getLength() == 0) available.push(lastId++);
		return available.pop();
	}

	public void free(int value) {
		available.push(value);
	}
}
