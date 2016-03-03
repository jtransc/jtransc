package jtransc.internal;

public class JTranscCType {
	//static final private String digits = "0123456789abcdef";

	static public char encodeDigit(int v) {
		if (v >= 0 && v <= 9) return (char) ('0' + (v - 0));
		if (v >= 10 && v <= 35) return (char) ('a' + (v - 10));
		return '0';
	}

	static public int decodeHexDigit(char c) {
		if ((c >= '0') && (c <= '9')) return c - '0';
		if ((c >= 'a') && (c <= 'f')) return c - 'a' + 10;
		if ((c >= 'A') && (c <= 'F')) return c - 'A' + 10;
		assert false;
		return -1;
	}
}
