package com.jtransc.lang;

import com.jtransc.annotation.JTranscAsync;

public class VMStack {
	@JTranscAsync
	public static StackTraceElement[] getCallingClassLoader() {
		try {
			throw new RuntimeException();
		} catch (RuntimeException e) {
			return e.getStackTrace();
		}
	}
}
