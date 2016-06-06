package com.jtransc.util;

public class JTranscMath {
	static public int clamp(int v, int min, int max) {
		return Math.min(Math.max(v, min), max);
	}

	public static int roundUpToPowerOfTwo(int i) {
		i--; // If input is a power of two, shift its high-order bit right.

		// "Smear" the high-order bit all the way to the right.
		i |= i >>>  1;
		i |= i >>>  2;
		i |= i >>>  4;
		i |= i >>>  8;
		i |= i >>> 16;

		return i + 1;
	}
}