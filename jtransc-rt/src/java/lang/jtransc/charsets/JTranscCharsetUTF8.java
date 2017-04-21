package java.lang.jtransc.charsets;

import java.io.ByteArrayOutputStream;
import java.lang.jtransc.JTranscCharset;

public class JTranscCharsetUTF8 extends JTranscCharset {
	public JTranscCharsetUTF8() {
		super(new String[] { "UTF-8", "UTF8" });
	}

	public final float minBytesPerCharacter() {
		return 1;
	}

	public final float avgBytesPerCharacter() {
		return 1.2f;
	}

	public final float maxBytesPerCharacter() {
		return 4;
	}

	@Override
	public void encode(char[] in, int offset, int len, ByteArrayOutputStream out) {
		for (int n = 0; n < len; n++) {
			int codePoint = in[offset + n];

			if ((codePoint & ~0x7F) == 0) { // 1-byte sequence
				out.write(codePoint);
			} else {
				if ((codePoint & ~0x7FF) == 0) { // 2-byte sequence
					out.write(((codePoint >> 6) & 0x1F) | 0xC0);
				} else if ((codePoint & ~0xFFFF) == 0) { // 3-byte sequence
					out.write(((codePoint >> 12) & 0x0F) | 0xE0);
					out.write(createByte(codePoint, 6));
				} else if ((codePoint & 0xFFE00000) == 0) { // 4-byte sequence
					out.write(((codePoint >> 18) & 0x07) | 0xF0);
					out.write(createByte(codePoint, 12));
					out.write(createByte(codePoint, 6));
				}
				out.write((codePoint & 0x3F) | 0x80);
			}
		}
	}

	static private int createByte(int codePoint, int shift) {
		return ((codePoint >> shift) & 0x3F) | 0x80;
	}

	@Override
	public void decode(byte[] in, int offset, int len, StringBuilder out) {
		int i = offset;
		int end = offset + len;
		while (i < end) {
			int c = in[i++] & 0xFF;
			switch (c >> 4) {
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7: {
					// 0xxxxxxx
					out.append((char) (c));
					break;
				}
				case 12:
				case 13: {
					// 110x xxxx   10xx xxxx
					out.append((char) (((c & 0x1F) << 6) | (in[i++] & 0x3F)));
					break;
				}
				case 14: {
					// 1110 xxxx  10xx xxxx  10xx xxxx
					out.append((char) (((c & 0x0F) << 12) | ((in[i++] & 0x3F) << 6) | ((in[i++] & 0x3F) << 0)));
					break;
				}
			}
		}
	}
}
