package com.jtransc;

import com.jtransc.annotation.haxe.HaxeMethodBody;

public class JTranscVersion {
	static public String getVersion() {
		return "0.2.0";
	}

	@HaxeMethodBody("return HaxeNatives.str('haxe');")
	static public String getRuntime() {
		return "java";
	}
}
