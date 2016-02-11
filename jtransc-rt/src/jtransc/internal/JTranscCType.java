package jtransc.internal;

public class JTranscCType {
	static public int decodeHexDigit(char c) {
		if ((c >= '0') && (c <= '9')) return c - '0';
		if ((c >= 'a') && (c <= 'f')) return c - 'a' + 10;
		if ((c >= 'A') && (c <= 'F')) return c - 'A' + 10;
		assert false;
		return -1;
	}
}
