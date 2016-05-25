package com.jtransc;

import com.jtransc.annotation.haxe.HaxeMethodBody;

public class JTranscVersion {
	static private final String version = "0.2.7";

	static public String getVersion() {
		return version;
	}

	@HaxeMethodBody("return HaxeNatives.str('haxe');")
	static public String getRuntime() {
		return "java";
	}
}
