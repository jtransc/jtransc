package java.lang.jtransc.charsets;

import com.jtransc.JTranscBits;

import java.io.ByteArrayOutputStream;
import java.lang.jtransc.JTranscCharset;

abstract class JTranscCharsetUTF16Base extends JTranscCharset {
	public final float minBytesPerCharacter() {
		return 2;
	}

	public final float avgBytesPerCharacter() {
		return 2;
	}

	public final float maxBytesPerCharacter() {
		return 2;
	}

	private String[] aliases;
	private boolean littleEndian;

	public JTranscCharsetUTF16Base(String[] names, boolean littleEndian) {
		super(names);
		this.littleEndian = littleEndian;
	}

	@Override
	public void encode(char[] in, int offset, int len, ByteArrayOutputStream out) {
		for (int n = 0; n < len; n++) {
			char c = in[offset + n];
			if (littleEndian) {
				out.write((c >>> 0) & 0xFF);
				out.write((c >>> 8) & 0xFF);
			} else {
				out.write((c >>> 8) & 0xFF);
				out.write((c >>> 0) & 0xFF);
			}
		}
	}

	@Override
	public void decode(byte[] in, int offset, int len, StringBuilder out) {
		for (int n = 0; n < len; n += 2) {
			out.append((char)JTranscBits.readInt16(in, offset + n, littleEndian));
		}
	}
}
