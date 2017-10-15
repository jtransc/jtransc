package com.jtransc.internal;

import com.jtransc.annotation.JTranscSync;

public class JTranscCType {
	@JTranscSync
	static public char encodeDigit(int v) {
		return Character.forDigit(v, Character.MAX_RADIX);
	}

	@JTranscSync
	static public int decodeDigit(char c) {
		return Character.digit(c, Character.MAX_RADIX);
	}

	@JTranscSync
	static public boolean isDigit(char c) {
		return ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
	}
}
