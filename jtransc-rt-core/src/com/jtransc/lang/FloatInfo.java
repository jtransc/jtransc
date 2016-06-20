package com.jtransc.lang;

public class FloatInfo {
	static public final int EXPONENT_BIAS = 127;

	static public final int EXPONENT_BITS = 9;
	static public final int MANTISSA_BITS = 23;
	static public final int NON_MANTISSA_BITS = 9;

	static public final int SIGN_MASK     = 0x80000000;
	static public final int EXPONENT_MASK = 0x7f800000;
	static public final int MANTISSA_MASK = 0x007fffff;
}
