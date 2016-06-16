package com.jtransc.crypto;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBodyPost;
import com.jtransc.annotation.haxe.HaxeMethodBodyPre;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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
	@JTranscMethodBody(target = "js", value = "N.fillSecureRandomBytes(p0);")
	@SuppressWarnings("all")
	static public void fillSecureRandomBytes(byte[] data) {
		if (!JTranscSystem.usingJTransc()) {
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextBytes(data);
		} else {
			//for (int n = 0; n < data.length; n++) data[n] = (byte)(Math.random() * 255);
		}
	}

	static public byte[] md5(byte[] data) {
		//try {
		//	MessageDigest md5 = MessageDigest.getInstance("MD5");
		//	return md5.digest(data);
		//} catch (NoSuchAlgorithmException e) {
		//	throw new RuntimeException("");
		//}
		throw new RuntimeException("Not implemented md5 yet!");
	}

	static public byte[] sha1(byte[] data) {
		//try {
		//	MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		//	return sha1.digest(data);
		//} catch (NoSuchAlgorithmException e) {
		//	throw new RuntimeException("");
		//}
		throw new RuntimeException("Not implemented sha1 yet!");
	}
}
