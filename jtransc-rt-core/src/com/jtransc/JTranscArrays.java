package com.jtransc;

import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.util.Arrays;

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

	static public byte[] copyReinterpretReversed(int[] data) {
		int[] temp = Arrays.copyOf(data, data.length);
		swizzle_inplace_reverse(temp);
		return copyReinterpret(temp);
	}

	//@JTranscInline
	static final public void swizzle_inplace(int[] data, int v3, int v2, int v1, int v0) {
		int size = data.length;
		for (int n = 0; n < size; n++) {
			int v = data[n];
			data[n] = JTranscBits.makeInt((byte)(v >>> v3), (byte)(v >>> v2), (byte)(v >>> v1), (byte)(v >>> v0));
		}
	}

	static final public void swizzle_inplace_reverse(int[] data) {
		//swizzle_inplace(data, 0, 8, 16, 24);
		int size = data.length;
		for (int n = 0; n < size; n++) {
			int v = data[n];
			data[n] = Integer.reverseBytes(v);
		}
	}

	/*
	static public void swizzle_inplace_abcd_dbca(int[] data) {
		int size = data.length;
		for (int n = 0; n < size; n++) {
			int v = data[n];
			data[n] = (v << 24) | (v >>> 24) | (v & 0x00FFFF00);
		}
	}

	static public void swizzle_inplace_abcd_abdc(int[] data) {
		int size = data.length;
		for (int n = 0; n < size; n++) {
			int v = data[n];
			data[n] = (v & 0xFFFF0000) | ((v << 8) & 0xFF00) | ((v >> 8) & 0xFF);
		}
	}

	static public void swizzle_inplace_abcd_dcba(int[] data) {
		int size = data.length;
		for (int n = 0; n < size; n++) data[n] = Integer.reverseBytes(data[n]);
	}
	static public void swizzle_inplace_abcd_bcda(int[] data) {
		int size = data.length;
		for (int n = 0; n < size; n++) {
			int v = data[n];
			data[n] = (v << 8) | ((v >>> 24) & 0xFF);
		}
	}
	*/
}
