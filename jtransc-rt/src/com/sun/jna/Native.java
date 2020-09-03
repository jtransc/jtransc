package com.sun.jna;

import com.jtransc.ffi.JTranscFFI;

public class Native {
    public static Object loadLibrary(String name, Class interfaceClass) {
        return JTranscFFI.loadLibrary(name, interfaceClass);
    }
}