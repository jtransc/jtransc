package com.jtransc.charset;

import com.jtransc.JTranscBits;

@SuppressWarnings("PointlessBitwiseExpression")
public class ModifiedUtf8 {
	public static int countBytes(String str, boolean slen) {
		return countBytes(str);
	}

	public static int countBytes(String str) {
		int result = 0;
		final int length = str.length();
		for (int n = 0; n < length; ++n) {
			char c = str.charAt(n);
			if (c == 0) {
				result += 2;
			} else if (c > 0 && c < 0x80) {
				result += 1;
			} else if (c < 0x800) {
				result += 2;
			} else {
				result += 3;
			}
		}
		return result;
	}

	public static void encode(final byte[] out, final int offset, final String str) {
		final int len = str.length();
		int pos = offset;
		for (int n = 0; n < len; n++) {
			char c = str.charAt(n);
			if (c == 0) {
				out[pos++] = (byte) (0xc0);
				out[pos++] = (byte) (0x80);
			} else if (c > 0 && c < 128) {
				out[pos++] = (byte) c;
			} else if (c < 2048) {
				out[pos++] = (byte) (0xc0 | (0x1f & (c >> 6)));
				out[pos++] = (byte) (0x80 | (0x3f & (c >> 0)));
			} else {
				out[pos++] = (byte) (0xe0 | (0x0f & (c >> 12)));
				out[pos++] = (byte) (0x80 | (0x3f & (c >> 6)));
				out[pos++] = (byte) (0x80 | (0x3f & (c >> 0)));
			}
		}
	}

	public static byte[] encode(String str) {
		int bytesSize = ModifiedUtf8.countBytes(str);
		byte[] out = new byte[2 + bytesSize];
		JTranscBits.writeShortBE(out, 0, (short) bytesSize);
		encode(out, 2, str);
		return out;
	}

	public static String decode(byte[] in, int offset, int len) {
		final StringBuilder out = new StringBuilder();
		int pos = offset;
		int end = offset + len;
		while (pos < end) {
			char c0 = (char) in[pos++];

			if (c0 < '\u0080') {
				out.append(c0);
				continue;
			}

			if ((c0 & 0xe0) == 0xc0) {
				if (pos >= end) break; // invalid
				int c1 = in[pos++];
				if ((c1 & 0xC0) != 0x80) break; // invalid
				out.append((char) (((c0 & 0x1F) << 6) | (c1 & 0x3F)));
				continue;
			}

			if ((c0 & 0xf0) == 0xe0) {
				if (pos + 1 >= end) break; // invalid
				int c1 = in[pos++];
				int c2 = in[pos++];
				if (((c1 & 0xC0) != 0x80) || ((c2 & 0xC0) != 0x80)) break; // invalid
				out.append((char) (((c0 & 0x0F) << 12) | ((c1 & 0x3F) << 6) | (c2 & 0x3F)));
				continue;
			}

			break; // invalid
		}
		return out.toString();
	}

	public static String decode(byte[] in, char[] temp, int offset, int len) {
		return decode(in, offset, len);
	}

	public static String decode(byte[] in) {
		return decode(in, 0, in.length);
	}

	public static String decodeLen(byte[] in, int offset) {
		int len = JTranscBits.readInt16BE(in, offset) & 0xFFFF;
		return decode(in, offset + 2, len);
	}

	public static String decodeLen(byte[] in) {
		return decodeLen(in, 0);
	}
}
