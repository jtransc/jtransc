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
		//if (!hasMore()) return '\0';
		return this.str.charAt(offset);
	}

	public void skip() {
		skip(1);
	}

	public void skip(int count) {
		offset += count;
	}

	public void expect(char c) {
		if (!hasMore()) throw new Error("Expected " + c + " but found end");
		if (read() != c) throw new Error("Expected " + c);
	}

	public char read() {
		if (!hasMore()) throw new Error("Can't read more");
		char out = peek();
		skip();
		return out;
	}

	public int available() {
		return length - offset;
	}

	public String read(int count) {
		int c = Math.min(count, available());
		String out = this.str.substring(offset, offset + c);
		skip(c);
		return out;
	}

	public String readUntil(char c, boolean include) {
		return readUntil(c, c, c, include);
	}

	public String readUntil(char c, char c2, boolean include) {
		return readUntil(c, c2, c2, include);
	}

	public String readUntil(char c, char c2, char c3, boolean include) {
		int start = offset;
		while (hasMore()) {
			char r = read();
			if (r == c || r == c2 || r == c3) {
				if (!include) skip(-1);
				break;
			}
		}
		return this.str.substring(start, offset);
	}
}
