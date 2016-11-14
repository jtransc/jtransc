package com.jtransc.lang;

public class VMStack {
	public static StackTraceElement[] getCallingClassLoader() {
		try {
			throw new RuntimeException();
		} catch (RuntimeException e) {
			return e.getStackTrace();
		}
	}
}
