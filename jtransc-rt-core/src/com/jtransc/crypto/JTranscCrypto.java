package com.jtransc.crypto;

import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBodyPost;
import com.jtransc.annotation.haxe.HaxeMethodBodyPre;

public class JTranscCrypto {
	@HaxeMethodBodyPre("" +
		"var bytes = p0;\n" +
		"var length = bytes.length;\n" +
		"\n"
	)
	@HaxeMethodBody(target = "js", value = "" +
		"try {\n" +
		"  var _bytes = bytes.data;\n" +
		"  untyped __js__(\"crypto.getRandomValues(_bytes);\");\n" +
		"  return;\n" +
		"} catch (e:Dynamic) {\n" +
		"\n" +
		"}\n"
	)
	@HaxeMethodBody("")
	@HaxeMethodBodyPost("" +
		"for (n in 0 ... length) {\n" +
		"  bytes.set(n, Std.int(Math.random() * 255));\n" +
		"}"
	)
	native static public void fillSecureRandomBytes(byte[] data);
}
