package com.jtransc;

import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.util.Arrays;

public class JTranscArrays {
	@HaxeMethodBody("return HaxeArrayByte.fromBytes(p0.getBytes());")
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
			data[n] = JTranscBits.makeInt((v >>> v3), (v >>> v2), (v >>> v1), (v >>> v0));
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

	@HaxeMethodBody("for (n in 0 ... p0) p1.data[p2 + n] = p3.data[p4 + n] + p5.data[p6 + n];")
	static public void add(int count, byte[] target, int targetpos, byte[] a, int apos, byte[] b, int bpos) {
		for (int n = 0; n < count; n++) target[targetpos + n] = (byte) (a[apos + n] + b[bpos + n]);
	}

	@HaxeMethodBody("for (n in 0 ... p0) p1.data[p2 + n] = p3.data[p4 + n] - p5.data[p6 + n];")
	static public void sub(int count, byte[] target, int targetpos, byte[] a, int apos, byte[] b, int bpos) {
		for (int n = 0; n < count; n++) target[targetpos + n] = (byte) (a[apos + n] - b[bpos + n]);
	}

	@HaxeMethodBody("var p8 = 1 - p7; for (n in 0 ... p0) p1.data[p2 + n] = Std.int(p3.data[p4 + n] * p7 + p5.data[p6 + n] * p8);")
	static public void mixUnsigned(int count, byte[] target, int targetpos, byte[] a, int apos, byte[] b, int bpos, double ratio) {
		double ratiob = 1.0 - ratio;
		for (int n = 0; n < count; n++)
			target[targetpos + n] = (byte) ((a[apos + n] & 0xFF) * ratio + (b[bpos + n] & 0xFF) * ratiob);
	}

	// Use clamped array?
	static public void addUnsignedClamped(int count, byte[] target, int targetpos, byte[] a, int apos, byte[] b, int bpos) {
		for (int n = 0; n < count; n++)
			target[targetpos + n] = (byte) clamp255((a[apos + n] & 0xFF) + (b[bpos + n] & 0xFF));
	}

	static private int clamp255(int v) {
		return Math.min(Math.max(v, 0), 255);
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

	public static void checkOffsetAndCount(int arrayLength, int offset, int count) {
		if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
			throw new ArrayIndexOutOfBoundsException("length=" + arrayLength + "; regionStart=" + offset + "; regionLength=" + count);
		}
	}
}
