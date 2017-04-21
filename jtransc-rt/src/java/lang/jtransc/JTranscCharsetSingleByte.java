package java.lang.jtransc;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class JTranscCharsetSingleByte extends JTranscCharset {
	final String decode;
	final Map<Character, Byte> encode;
	final byte invalidChar = (byte) '?';

	public final float minBytesPerCharacter() {
		return 1;
	}

	public final float avgBytesPerCharacter() {
		return 1;
	}

	public final float maxBytesPerCharacter() {
		return 1;
	}

	public JTranscCharsetSingleByte(String[] aliases, String chars) {
		super(aliases);
		this.decode = chars;
		this.encode = new HashMap<>(chars.length());
		for (int n = 0; n < chars.length(); n++) {
			this.encode.put(chars.charAt(n), (byte) n);
		}
	}

	@Override
	final public void encode(char[] in, int offset, int len, ByteArrayOutputStream out) {
		for (int n = 0; n < len; n++) {
			char c = in[offset + n];
			Byte b = encode.get(c);
			if (b != null) {
				out.write(b);
			} else {
				out.write(invalidChar);
			}
		}
	}

	@Override
	final public void decode(byte[] in, int offset, int len, StringBuilder out) {
		for (int n = 0; n < len; n++) {
			int b = in[offset + n] & 0xFF;
			out.append(decode.charAt(b));
		}
	}
}
