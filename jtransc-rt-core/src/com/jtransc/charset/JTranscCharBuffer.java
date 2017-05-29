package com.jtransc.charset;

public class JTranscCharBuffer {
	private static final int DEFAULT_SIZE = 64;
	private char[] buffer;
	private int size;
	private int position;

	public JTranscCharBuffer() {
		size = DEFAULT_SIZE;
		buffer = new char[size];
	}

	public JTranscCharBuffer(int size) {
		this.size = size < DEFAULT_SIZE ? DEFAULT_SIZE : size;
		buffer = new char[this.size];
	}

	public void append(char c) {
		if (position == size) {
			char[] extention = new char[size * 2];
			for (int i = 0; i < size; ++i) {
				extention[i] = buffer[i];
			}
			buffer = extention;
			size += size;
		}
		buffer[position] = c;
		position++;
	}

	public void reset() {
		position = 0;
	}

	@Override
	public String toString() {
		return new String(buffer, 0, position);
	}
}