package com.jtransc.text;

import com.jtransc.annotation.JTranscInvisible;

@JTranscInvisible
public class MStringReader {
	public final String str;
	public final int length;
	public int offset;

	public MStringReader(String str) {
		this(str, 0);
	}

	public MStringReader(String str, int offset) {
		this.str = str;
		this.length = str.length();
		this.offset = offset;
	}

	public boolean hasMore() {
		return offset < length;
	}

	public char peek() {
		if (!hasMore()) throw new Error("Can't read more");
		return this.str.charAt(offset);
	}

	public void skip() {
		skip(1);
	}

	public void skip(int count) {
		offset += count;
	}

	public void expect(char c) {
		if (read() != c) throw new Error("Expected " + c);
	}

	public char read() {
		if (!hasMore()) throw new Error("Can't read more");
		char out = peek();
		skip();
		return out;
	}
}
