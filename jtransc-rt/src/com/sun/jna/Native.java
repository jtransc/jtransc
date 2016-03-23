package com.sun.jna;

import jtransc.ffi.JTranscFFI;

public class Native {
	public static Object loadLibrary(String name, Class interfaceClass) {
		return JTranscFFI.loadLibrary(name, interfaceClass);
	}
}