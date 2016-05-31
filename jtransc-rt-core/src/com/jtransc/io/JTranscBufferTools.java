package com.jtransc.io;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class JTranscBufferTools {
	static public byte[] toByteArray(ByteBuffer buffer) {
		return toByteArray(buffer, 0);
	}

	static public byte[] toByteArray(ByteBuffer buffer, int position) {
		if (position == 0 && buffer.hasArray()) {
			return buffer.array();
		} else {
			byte[] out = new byte[buffer.limit() - position];
			for (int n = 0; n < out.length; n++) out[n] = buffer.get(position + n);
			return out;
		}
	}

	static public int[] toIntArray(ByteBuffer buffer) {
		IntBuffer intBuffer = buffer.asIntBuffer();
		int[] out = new int[intBuffer.limit()];
		for (int n = 0; n < out.length; n++) {
			out[n] = intBuffer.get(n);
		}
		return out;
	}
}
