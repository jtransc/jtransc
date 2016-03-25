package jtransc;

import jtransc.annotation.haxe.HaxeMethodBody;

public class JTranscVersion {
	static public String getVersion() {
		return "0.1.5";
	}

	@HaxeMethodBody("return HaxeNatives.str('haxe');")
	static public String getRuntime() {
		return "java";
	}
}
