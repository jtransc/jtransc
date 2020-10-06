package com.jtransc.crypto;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscAddHeader;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscMethodBodyList;





import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class JTranscCrypto {
	static public void fillSecureRandomBytes(byte[] data) {
		if (secureRandomProvider == null || data == null) throw new RuntimeException("fillSecureRandomBytes");
		//System.out.println("JTranscCrypto.fillSecureRandomBytes!");
		secureRandomProvider.fillSecureRandomBytes(data);
	}

	static public byte[] md5(byte[] data) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			return md5.digest(data);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("");
		}
	}

	static public byte[] sha1(byte[] data) {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			return sha1.digest(data);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("");
		}
	}

	// Allows to override the SecureRandom provider
	static public SecureRandomProvider secureRandomProvider = new SecureRandomProvider() {

	};

	static public class SecureRandomProvider {
		@JTranscMethodBody(target = "js", value = "N.fillSecureRandomBytes(p0);")
		//@JTranscMethodBody(target = "cs", value = "var len = p0.length; var temp = new byte[len]; var provider = (new System.Security.Cryptography.RNGCryptoServiceProvider()); provider.GetBytes(temp); provider.Dispose(); for (int n = 0; n < len; n++) p0[n] = (sbyte)temp[n];"),
		@JTranscMethodBody(target = "cs", value = "var len = p0.length; var temp = new byte[len]; var provider = (new System.Security.Cryptography.RNGCryptoServiceProvider()); provider.GetBytes(temp); for (int n = 0; n < len; n++) p0[n] = (sbyte)temp[n];")
		@JTranscMethodBody(target = "dart", value = "N.fillSecureRandomBytes(p0.data);")
		@SuppressWarnings("all")
		public void fillSecureRandomBytes(byte[] data) {
			if (!JTranscSystem.isJTransc()) {
				SecureRandom secureRandom = new SecureRandom();
				secureRandom.nextBytes(data);
			} else {
				System.err.println("[IMPORTANT WARNING] Using SecureRandom without properly setting JTranscCrypto.secureRandomProvider");
				//throw new Run
				//for (int n = 0; n < data.length; n++) data[n] = (byte)(Math.random() * 255);
			}
		}
	}
}
