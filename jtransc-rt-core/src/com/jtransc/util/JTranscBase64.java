package com.jtransc.util;

import java.util.Arrays;

public class JTranscBase64 {
	static private String TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
	static private int[] DECODE;

	static public byte[] decode(String str) {
		byte[] src = str.getBytes();
		byte[] dst = new byte[src.length];
		return Arrays.copyOf(dst, decode(src, dst));
	}

	static public int decode(byte[] src, byte[] dst) {
		if (DECODE == null) {
			DECODE = new int[0x100];
			for (int n = 0; n < 0x100; n++) DECODE[n] = -1;
			for (int n = 0; n < TABLE.length(); n++) {
				DECODE[TABLE.charAt(n)] = n;
			}
		}

		int m = 0;

		for (int n = 0; n < src.length; ) {
			int d = DECODE[src[n] & 0xFF];
			if (d < 0) {
				n++;
				continue; // skip character
			}

			int b0 = DECODE[src[n++] & 0xFF];
			int b1 = DECODE[src[n++] & 0xFF];
			int b2 = DECODE[src[n++] & 0xFF];
			int b3 = DECODE[src[n++] & 0xFF];
			dst[m++] = (byte) ((b0 << 2) | (b1 >> 4));
			if (b2 < 64) {
				dst[m++] = (byte) ((b1 << 4) | (b2 >> 2));
				if (b3 < 64) {
					dst[m++] = (byte) ((b2 << 6) | b3);
				}
			}
		}
		return m;
	}
}
