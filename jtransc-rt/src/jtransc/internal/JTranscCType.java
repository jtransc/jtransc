package jtransc.internal;

public class JTranscCType {
	static public char encodeDigit(int v) {
		return Character.forDigit(v, Character.MAX_RADIX);
	}

	static public int decodeDigit(char c) {
		return Character.digit(c, Character.MAX_RADIX);
	}

	static public boolean isDigit(char c) {
		if (c >= '0' && c <= '9') return true;
		if (c >= 'a' && c <= 'z') return true;
		if (c >= 'A' && c <= 'Z') return true;
		return false;
	}
}
