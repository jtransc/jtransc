package com.jtransc.types;

public class AstTestExample {
	/*
	public static long reverseBytes(long v) {
		public infix fun Long.until(to: Int): LongRange {
			return this .. (to.toLong() - 1).toLong()
		}
	}
	*/

	public static LongRange reverseBytes(long a, int b) {
		return new LongRange(a, (long) ((long) (b) - 1));
	}
}

class LongRange {
	public LongRange(long a, long b) {
	}
}