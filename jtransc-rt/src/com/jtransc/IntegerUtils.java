package com.jtransc;

public class IntegerUtils {
	static public int umod(int left, int right) {
		int remainder = left % right;
		if (remainder < 0) {
			return remainder + right;
		} else {
			return remainder;
		}
	}
}
