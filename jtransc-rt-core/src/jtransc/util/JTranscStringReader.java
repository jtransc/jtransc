package jtransc.util;

import jtransc.annotation.JTranscInvisible;

import java.util.Objects;

@JTranscInvisible
public class JTranscStringReader {
	private String str;
	private int offset;

	public JTranscStringReader(String str) {
		this.str = str;
		this.offset = 0;
	}

	public char peekch() {
		return this.str.charAt(offset);
	}

	public String peek(int count) {
		return JTranscStrings.substr(this.str, offset, count);
	}

	public String read(int count) {
		String out = JTranscStrings.substr(this.str, offset, count);
		offset += count;
		return out;
	}

	public String tryRead(String ...options) {
		for (String option : options) {
			if (Objects.equals(peek(option.length()), option)) {
				offset += option.length();
				return option;
			}
		}
		return null;
	}

	public char readch() {
		return this.str.charAt(offset++);
	}

	public int getOffset() {
		return offset;
	}

	public int length() {
		return str.length();
	}

	public boolean eof() {
		return offset >= length();
	}
}
