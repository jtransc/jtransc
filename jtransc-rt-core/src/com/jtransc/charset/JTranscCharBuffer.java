package com.jtransc.charset;

import com.jtransc.annotation.JTranscSync;

public class JTranscCharBuffer {
	private static final int DEFAULT_SIZE = 64;
	private char[] buffer;
	private int size;
	private int position;

	@JTranscSync
	public JTranscCharBuffer() {
		size = DEFAULT_SIZE;
		buffer = new char[size];
	}

	@JTranscSync
	public JTranscCharBuffer(int size) {
		this.size = size < DEFAULT_SIZE ? DEFAULT_SIZE : size;
		buffer = new char[this.size];
	}

	@JTranscSync
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

	@JTranscSync
	public void reset() {
		position = 0;
	}

	@Override
	@JTranscSync
	public String toString() {
		return new String(buffer, 0, position);
	}
}