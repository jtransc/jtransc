package com.jtransc.mem;

public class BytesRead {
	static private int _u8(byte[] data, int offset) {
		return data[offset] & 0xFF;
	}

	static private int _u16(byte[] data, int offset) {
		return (_u8(data, offset + 1) << 0) | (_u8(data, offset + 0) << 8);
	}

	static private int _s32(byte[] data, int offset) {
		return (_u16(data, offset + 2) << 0) | (_u16(data, offset + 0) << 16);
	}

	static private long _u32(byte[] data, int offset) {
		return ((long) _s32(data, offset)) & 0xFFFFFFFFL;
	}

	static private long _s64(byte[] data, int offset) {
		return (_u32(data, offset + 4) << 0) | (_u32(data, offset + 0) << 32);
	}

	static public int u8l(byte[] data, int offset) {
		return _u8(data, offset);
	}

	static public int u8b(byte[] data, int offset) {
		return _u8(data, offset);
	}

	static public int u16l(byte[] data, int offset) {
		return java.lang.Short.reverseBytes((short) _u16(data, offset)) & 0xFFFF;
	}

	static public int u16b(byte[] data, int offset) {
		return _u16(data, offset);
	}

	static public long u32l(byte[] data, int offset) {
		return ((long) java.lang.Integer.reverseBytes(_s32(data, offset))) & 0xFFFFFFFFL;
	}

	static public long u32b(byte[] data, int offset) {
		return ((long) _s32(data, offset)) & 0xFFFFFFFFL;
	}

	static public int s8l(byte[] data, int offset) {
		return (byte) u8l(data, offset);
	}

	static public int s8b(byte[] data, int offset) {
		return (byte) u8b(data, offset);
	}

	static public int s16l(byte[] data, int offset) {
		return (short) u16l(data, offset);
	}

	static public int s16b(byte[] data, int offset) {
		return (short) u16b(data, offset);
	}

	static public int s32l(byte[] data, int offset) {
		return java.lang.Integer.reverseBytes(_s32(data, offset));
	}

	static public int s32b(byte[] data, int offset) {
		return _s32(data, offset);
	}

	static public long s64l(byte[] data, int offset) {
		return java.lang.Long.reverseBytes(_s64(data, offset));
	}

	static public long s64b(byte[] data, int offset) {
		return _s64(data, offset);
	}

	static public float f32l(byte[] data, int offset) {
		return java.lang.Float.intBitsToFloat(s32l(data, offset));
	}

	static public float f32b(byte[] data, int offset) {
		return java.lang.Float.intBitsToFloat(s32b(data, offset));
	}

	static public double f64l(byte[] data, int offset) {
		return java.lang.Double.longBitsToDouble(s64l(data, offset));
	}

	static public double f64b(byte[] data, int offset) {
		return java.lang.Double.longBitsToDouble(s64b(data, offset));
	}
}
