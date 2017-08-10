package javatest.utils;

import com.jtransc.util.JTranscHex;

import java.util.Base64;

public class Base64Test {
	static public void main(String[] args) {
		System.out.println("Base64Test.main:");
		String base64 = "SGVsbG8gV29ybGQh";
		System.out.println(base64);
		byte[] data = Base64.getMimeDecoder().decode(base64);
		System.out.println(JTranscHex.encode(data));
		System.out.println(JTranscHex.encode(JTranscHex.decode(JTranscHex.encode(data))));
	}
}
