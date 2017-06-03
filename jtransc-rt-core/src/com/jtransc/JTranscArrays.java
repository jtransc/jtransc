package com.jtransc;

import java.util.Arrays;

public class JTranscArrays {
	public static final byte[] EMPTY_BYTE = new byte[0];
	public static final Class<?>[] EMPTY_CLASS = new Class<?>[0];

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

	static public byte[] copyReinterpret(short[] data) {
		byte[] out = new byte[data.length * 2];
		int m = 0;
		for (int value : data) {
			out[m++] = JTranscBits.int0(value);
			out[m++] = JTranscBits.int1(value);
		}
		return out;
	}

	static public int[] copyReinterpretInt_LE(byte[] data) {
		int[] out = new int[data.length / 4];
		int m = 0;
		for (int n = 0; n < data.length; n += 4) {
			out[m++] = JTranscBits.makeInt(data[n + 3], data[n + 2], data[n + 1], data[n + 0]);
		}
		return out;
	}

	static public short[] copyReinterpretShort_LE(byte[] data) {
		short[] out = new short[data.length / 2];
		int m = 0;
		for (int n = 0; n < data.length; n += 2) {
			out[m++] = JTranscBits.makeShort(data[n + 1], data[n + 0]);
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

	static public void add(int count, byte[] target, int targetpos, byte[] a, int apos, byte[] b, int bpos) {
		for (int n = 0; n < count; n++) target[targetpos + n] = (byte) (a[apos + n] + b[bpos + n]);
	}

	static public void sub(int count, byte[] target, int targetpos, byte[] a, int apos, byte[] b, int bpos) {
		for (int n = 0; n < count; n++) target[targetpos + n] = (byte) (a[apos + n] - b[bpos + n]);
	}

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

	public static void checkOffsetAndCount(int arrayLength, int offset, int count) {
		if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
			throw new ArrayIndexOutOfBoundsException("length=" + arrayLength + "; regionStart=" + offset + "; regionLength=" + count);
		}
	}

	static public String toString(Object array) {
		if (array == null) return "null";
		if (array instanceof boolean[]) return Arrays.toString((boolean[]) array);
		if (array instanceof byte[]) return Arrays.toString((byte[]) array);
		if (array instanceof short[]) return Arrays.toString((short[]) array);
		if (array instanceof char[]) return Arrays.toString((char[]) array);
		if (array instanceof int[]) return Arrays.toString((int[]) array);
		if (array instanceof long[]) return Arrays.toString((long[]) array);
		if (array instanceof float[]) return Arrays.toString((float[]) array);
		if (array instanceof double[]) return Arrays.toString((double[]) array);
		if (array instanceof Object[]) return Arrays.toString((Object[]) array);
		return array.toString();
	}

	static public String toStringCharsAsInts(Object array) {
		if (array instanceof char[]) {
			char[] a = (char[]) array;
			int[] o = new int[a.length];
			for (int n = 0; n < a.length; n++) o[n] = (int) a[n];
			return toString(o);
		} else {
			return toString(array);
		}
	}
}