package com.jtransc.charset;

import com.jtransc.annotation.JTranscAsync;
import com.jtransc.annotation.JTranscSync;
import com.jtransc.ds.FastIntIntMap;
import com.jtransc.ds.FastIntMap;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class JTranscCharsetSingleByte extends JTranscCharset {
	final String decode;
	final FastIntIntMap encode;
	final byte invalidChar = (byte) '?';

	@JTranscSync
	public JTranscCharsetSingleByte(String[] aliases, String chars) {
		super(aliases, 1, 1, 1);
		this.decode = chars;
		this.encode = new FastIntIntMap();
		for (int n = 0; n < chars.length(); n++) {
			this.encode.set(chars.charAt(n), n);
		}
	}

	@Override
	@JTranscSync
	final public void encode(char[] in, int offset, int len, ByteArrayOutputStream out) {
		for (int n = 0; n < len; n++) {
			char c = in[offset + n];
			if (encode.has(c)) {
				out.write(encode.get(c));
			} else {
				out.write(invalidChar);
			}
		}
	}

	@Override
	@JTranscSync
	final public void decode(byte[] in, int offset, int len, JTranscCharBuffer out) {
		for (int n = 0; n < len; n++) {
			int b = in[offset + n] & 0xFF;
			out.append(decode.charAt(b));
		}
	}

	@Override
	@JTranscAsync
	final public void decode(ByteBuffer in, CharBuffer out) {
		while (in.hasRemaining() && out.hasRemaining()) {
			int b = in.get() & 0xFF;
			out.append(decode.charAt(b));
		}
	}
}
