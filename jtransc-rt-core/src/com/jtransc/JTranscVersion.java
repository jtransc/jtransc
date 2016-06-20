package com.jtransc;

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;

public class JTranscVersion {
	static private final String version = "0.3.0";

	static public String getVersion() {
		return version;
	}

	@HaxeMethodBody("return HaxeNatives.str('haxe');")
	@JTranscMethodBody(target = "js", value = "return N.str('js');")
	static public String getRuntime() {
		return "java";
	}
}
