package com.jtransc.types;

public class AstTestExample {
	public static long reverseBytes(long v) {
		v = (v & 0x00ff00ff00ff00ffL) << 8 | (v >>> 8) & 0x00ff00ff00ff00ffL;
		return (v << 48) | ((v & 0xffff0000L) << 16) | ((v >>> 16) & 0xffff0000L) | (v >>> 48);
	}
}
