package com.jtransc;

import com.jtransc.annotation.haxe.HaxeMethodBody;

public class JTranscArrays {
	@HaxeMethodBody("return HaxeByteArray.fromBytes(p0.getBytes());")
	static public byte[] copyReinterpret(int[] data) {
		byte[] out = new byte[data.length * 4];
		int m = 0;
		for (int value : data) {
			out[m++] = JTranscBits.int0(value);
			out[m++] = JTranscBits.int1(value);
			out[m++] = JTranscBits.int2(value);
			out[m++] = JTranscBits.int3(value);
		}
		return out;
	}
}
