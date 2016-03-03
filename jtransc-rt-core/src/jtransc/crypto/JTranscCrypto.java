package jtransc.crypto;

import jtransc.annotation.haxe.HaxeMethodBody;

public class JTranscCrypto {
	@HaxeMethodBody("HaxeNatives.fillSecureRandomBytes(p0);")
	native static public void fillSecureRandomBytes(byte[] data);
}
