package com.jtransc.util;

import com.jtransc.internal.JTranscCType;

import java.util.Arrays;

@SuppressWarnings({"PointlessArithmeticExpression", "PointlessBitwiseExpression"})
public class JTranscHex {
	static public String chars = "0123456789abcdef";

	static public int decodeInt(String hex, int offset, int len) {
		//return java.lang.Integer.parseInt(hex.substring(offset, offset + len), 16);

		int out = 0;
		for (int n = 0; n < len; n++) {
			out *= 16;
			out += JTranscCType.decodeDigit(hex.charAt(offset + n));
		}
		return out;
	}

	static public byte[] decode(String hex) {
		int len = hex.length();
		byte[] out = new byte[len / 2 + 1];
		int m = 0;
		for (int n = 0; n < len; ) {
			char c = hex.charAt(n);
			if (!JTranscCType.isDigit(c)) {
				n++;
				continue;
			}
			int high = JTranscCType.decodeDigit(hex.charAt(n++));
			int low = JTranscCType.decodeDigit(hex.charAt(n++));
			out[m++] = (byte) ((high << 4) | low);
		}
		return Arrays.copyOf(out, m);
	}

	static public String encode(byte[] data) {
		int len = data.length;
		char[] out = new char[len * 2];
		for (int n = 0, m = 0; n < len; n++, m += 2) {
			int c = data[n] & 0xFF;
			out[m + 0] = chars.charAt((c >>> 4) & 0xF);
			out[m + 1] = chars.charAt((c >>> 0) & 0xF);
		}
		return new String(out);
	}
}
