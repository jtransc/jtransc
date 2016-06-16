package com.jtransc.mem;

@SuppressWarnings({"PointlessBitwiseExpression", "PointlessArithmeticExpression", "WeakerAccess", "unused"})
public class BytesWrite {
	public static void u16(byte[] out, int index, short value, boolean isLittleEndian) {
		if (isLittleEndian) {
			writeShortLE(out, index, value);
		} else {
			writeShortBE(out, index, value);
		}
	}

	public static void u32(byte[] out, int index, int value, boolean isLittleEndian) {
		if (isLittleEndian) {
			writeIntLE(out, index, value);
		} else {
			writeIntBE(out, index, value);
		}
	}

	public static void u64(byte[] out, int index, long value, boolean isLittleEndian) {
		if (isLittleEndian) {
			writeLongLE(out, index, value);
		} else {
			writeLongBE(out, index, value);
		}
	}

	public static void writeShortBE(byte[] out, int offset, short value) {
		out[offset + 0] = (byte) ((value >>> 8) & 0xFF);
		out[offset + 1] = (byte) ((value >>> 0) & 0xFF);
	}

	public static void writeIntBE(byte[] out, int offset, int value) {
		out[offset + 0] = (byte) ((value >>> 24) & 0xFF);
		out[offset + 1] = (byte) ((value >>> 16) & 0xFF);
		out[offset + 2] = (byte) ((value >>> 8) & 0xFF);
		out[offset + 3] = (byte) ((value >>> 0) & 0xFF);
	}

	public static void writeLongBE(byte[] out, int offset, long value) {
		int h = (int)(value >> 32);
		int l = (int)(value >> 0);
		out[offset + 0] = (byte) (h >>> 24);
		out[offset + 1] = (byte) (h >>> 16);
		out[offset + 2] = (byte) (h >>> 8);
		out[offset + 3] = (byte) (h >>> 0);
		out[offset + 4] = (byte) (l >>> 24);
		out[offset + 5] = (byte) (l >>> 16);
		out[offset + 6] = (byte) (l >>> 8);
		out[offset + 7] = (byte) (l >>> 0);
	}

	public static void writeShortLE(byte[] out, int offset, short value) {
		out[offset + 1] = (byte) ((value >>> 8) & 0xFF);
		out[offset + 0] = (byte) ((value >>> 0) & 0xFF);
	}

	public static void writeIntLE(byte[] out, int offset, int value) {
		out[offset + 3] = (byte) ((value >>> 24) & 0xFF);
		out[offset + 2] = (byte) ((value >>> 16) & 0xFF);
		out[offset + 1] = (byte) ((value >>> 8) & 0xFF);
		out[offset + 0] = (byte) ((value >>> 0) & 0xFF);
	}

	public static void writeLongLE(byte[] out, int offset, long value) {
		int h = (int)(value >> 32);
		int l = (int)(value >> 0);
		out[offset + 7] = (byte) (h >>> 24);
		out[offset + 6] = (byte) (h >>> 16);
		out[offset + 5] = (byte) (h >>> 8);
		out[offset + 4] = (byte) (h >>> 0);
		out[offset + 3] = (byte) (l >>> 24);
		out[offset + 2] = (byte) (l >>> 16);
		out[offset + 1] = (byte) (l >>> 8);
		out[offset + 0] = (byte) (l >>> 0);
	}
}