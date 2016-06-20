package com.jtransc.lang;

public class DoubleInfo {
	static public final int EXPONENT_BIAS = 1023;

	static public final int EXPONENT_BITS = 12;
	static public final int MANTISSA_BITS = 52;
	static public final int NON_MANTISSA_BITS = 12;

	static public final long SIGN_MASK     = 0x8000000000000000L;
	static public final long EXPONENT_MASK = 0x7ff0000000000000L;
	static public final long MANTISSA_MASK = 0x000fffffffffffffL;
}
