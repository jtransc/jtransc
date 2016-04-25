package com.jtransc.crypto;

import com.jtransc.annotation.haxe.HaxeMethodBody;

public class JTranscCrypto {
	@HaxeMethodBody(
		"var bytes = p0;\n" +
		"var length = bytes.length;\n" +
		"\n" +
		"#if js\n" +
		"try {\n" +
		"  var _bytes = bytes.data;\n" +
		"  untyped __js__(\"crypto.getRandomValues(_bytes);\");\n" +
		"  return;\n" +
		"} catch (e:Dynamic) {\n" +
		"\n" +
		"}\n" +
		"#end\n" +
		"\n" +
		"for (n in 0 ... length) {\n" +
		"  bytes.set(n, Std.int(Math.random() * 255));\n" +
		"}"
	)
	native static public void fillSecureRandomBytes(byte[] data);
}
