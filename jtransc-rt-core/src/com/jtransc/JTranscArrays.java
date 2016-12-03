package com.jtransc;

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.util.Arrays;

public class JTranscArrays {
	public static final byte[] EMPTY_BYTE = new byte[0];
	public static final Class<?>[] EMPTY_CLASS = new Class<?>[0];

	@HaxeMethodBody("return JA_B.fromBytes(p0.getBytes());")
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

	@HaxeMethodBody(value = "return true;")
	@JTranscMethodBody(target = "haxe", value = "return true;")
	@JTranscMethodBody(target = "js", value = "return true;")
	//@JTranscMethodBody(target = "cpp", value = "return true;")
	@JTranscMethodBody(target = "d", value = "return true;")
	static public boolean nativeReinterpretSupported() {
		return false;
	}

	@HaxeMethodBody(value = "return cast(p0, JA_0).toByteArray();")
	@JTranscMethodBody(target = "haxe", value = "return cast(p0, JA_0).toByteArray();")
	@JTranscMethodBody(target = "js", value = "return JA_B.wrapBuffer(p0.getBuffer());")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT(JA_0, p0)->toByteArray();")
	@JTranscMethodBody(target = "d", value = "return (cast(JA_0)p0).toByteArray();")
	native static public byte[] nativeReinterpretAsByte(Object data);

	@HaxeMethodBody(value = "return cast(p0, JA_0).toShortArray();")
	@JTranscMethodBody(target = "haxe", value = "return cast(p0, JA_0).toShortArray();")
	@JTranscMethodBody(target = "js", value = "return JA_S.wrapBuffer(p0.getBuffer());")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT(JA_0, p0)->toShortArray();")
	@JTranscMethodBody(target = "d", value = "return (cast(JA_0)p0).toShortArray();")
	native static public short[] nativeReinterpretAsShort(Object data);

	@HaxeMethodBody(value = "return cast(p0, JA_0).toCharArray();")
	@JTranscMethodBody(target = "haxe", value = "return cast(p0, JA_0).toCharArray();")
	@JTranscMethodBody(target = "js", value = "return JA_C.wrapBuffer(p0.getBuffer());")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT(JA_0, p0)->toCharArray();")
	@JTranscMethodBody(target = "d", value = "return (cast(JA_0)p0).toCharArray();")
	native static public char[] nativeReinterpretAsChar(Object data);

	@HaxeMethodBody(value = "return cast(p0, JA_0).toIntArray();")
	@JTranscMethodBody(target = "haxe", value = "return cast(p0, JA_0).toIntArray();")
	@JTranscMethodBody(target = "js", value = "return JA_I.wrapBuffer(p0.getBuffer());")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT(JA_0, p0)->toIntArray();")
	@JTranscMethodBody(target = "d", value = "return (cast(JA_0)p0).toIntArray();")
	native static public int[] nativeReinterpretAsInt(Object data);

	@HaxeMethodBody(value = "return cast(p0, JA_0).toFloatArray();")
	@JTranscMethodBody(target = "haxe", value = "return cast(p0, JA_0).toFloatArray();")
	@JTranscMethodBody(target = "js", value = "return JA_F.wrapBuffer(p0.getBuffer());")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT(JA_0, p0)->toFloatArray();")
	@JTranscMethodBody(target = "d", value = "return (cast(JA_0)p0).toFloatArray();")
	native static public float[] nativeReinterpretAsFloat(Object data);

	@HaxeMethodBody(value = "return cast(p0, JA_0).toDoubleArray();")
	@JTranscMethodBody(target = "haxe", value = "return cast(p0, JA_0).toDoubleArray();")
	@JTranscMethodBody(target = "js", value = "return JA_D.wrapBuffer(p0.getBuffer());")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT(JA_0, p0)->toDoubleArray();")
	@JTranscMethodBody(target = "d", value = "return (cast(JA_0)p0).toDoubleArray();")
	native static public double[] nativeReinterpretAsDouble(Object data);

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