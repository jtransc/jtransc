package com.jtransc.crypto;

import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBodyAllPost;
import com.jtransc.annotation.haxe.HaxeMethodBodyAllPre;
import com.jtransc.annotation.haxe.HaxeMethodBodyJs;

public class JTranscCrypto {
	@HaxeMethodBodyAllPre("" +
		"var bytes = p0;\n" +
		"var length = bytes.length;\n" +
		"\n"
	)
	@HaxeMethodBodyJs("" +
		"try {\n" +
		"  var _bytes = bytes.data;\n" +
		"  untyped __js__(\"crypto.getRandomValues(_bytes);\");\n" +
		"  return;\n" +
		"} catch (e:Dynamic) {\n" +
		"\n" +
		"}\n"
	)
	@HaxeMethodBody("")
	@HaxeMethodBodyAllPost("" +
		"for (n in 0 ... length) {\n" +
		"  bytes.set(n, Std.int(Math.random() * 255));\n" +
		"}"
	)
	native static public void fillSecureRandomBytes(byte[] data);
}
