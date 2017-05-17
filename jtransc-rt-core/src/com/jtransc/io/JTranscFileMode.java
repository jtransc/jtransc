package com.jtransc.io;

public enum JTranscFileMode {
	//public static final int O_RDONLY = 1;
	//public static final int O_RDWR = 2;
	//public static final int O_SYNC = 4;
	//public static final int O_DSYNC = 8;

	//public static final int O_APPEND = 16; // jtransc specific

	READ(1, "r"),
	WRITE(2, "rw"),
	APPEND(2 + 16, "a+");

	public final int mode;
	public final String modestr;

	JTranscFileMode(int mode, String modestr) {
		this.mode = mode;
		this.modestr = modestr;
	}
}
