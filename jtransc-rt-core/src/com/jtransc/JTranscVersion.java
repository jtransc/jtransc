package com.jtransc;

import com.jtransc.annotation.JTranscSync;

public class JTranscVersion {
	static private final String version = "0.6.9-SNAPSHOT";

	@JTranscSync
	static public String getVersion() {
		return version;
	}

	@JTranscSync
	static public String getRuntime() {
		return JTranscSystem.getRuntimeKind();
	}
}
