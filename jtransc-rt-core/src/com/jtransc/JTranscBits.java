/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc;

import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBodyList;

import java.nio.ByteBuffer;

@JTranscInvisible
@SuppressWarnings({"PointlessBitwiseExpression", "PointlessArithmeticExpression", "WeakerAccess", "unused"})
public class JTranscBits {
	static public int unsignedMod(int value, int mod) {
		return ((value % mod) + mod) % mod;
	}

	static public short swap(short x) {
		return Short.reverseBytes(x);
	}

	static public char swap(char x) {
		return Character.reverseBytes(x);
	}

	static public int swap(int x) {
		return Integer.reverseBytes(x);
	}

	static public long swap(long x) {
		return Long.reverseBytes(x);
	}

	static public char makeChar(byte b1, byte b0) {
		return (char) ((b1 << 8) | (b0 & 0xff));
	}

	static public long makeLong(byte b7, byte b6, byte b5, byte b4, byte b3, byte b2, byte b1, byte b0) {
		return ((((long) b7) << 56) | (((long) b6 & 0xff) << 48) | (((long) b5 & 0xff) << 40) | (((long) b4 & 0xff) << 32) | (((long) b3 & 0xff) << 24) | (((long) b2 & 0xff) << 16) | (((long) b1 & 0xff) << 8) | (((long) b0 & 0xff)));
	}

	static public long makeLong(byte[] bytes) {
		return makeLong(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
	}

	//@JTranscInline
	static final public int makeInt(byte b3, byte b2, byte b1, byte b0) {
		return (((b3 & 0xFF) << 24) | ((b2 & 0xFF) << 16) | ((b1 & 0xFF) << 8) | ((b0 & 0xFF) << 0));
	}

	static final public int makeInt(int b3, int b2, int b1, int b0) {
		return (((b3 & 0xFF) << 24) | ((b2 & 0xFF) << 16) | ((b1 & 0xFF) << 8) | ((b0 & 0xFF) << 0));
	}

	static public int makeInt(byte[] bytes) {
		return makeInt(bytes[0], bytes[1], bytes[2], bytes[3]);
	}

	static public int makeInt(byte[] bytes, int offset) {
		return makeInt(bytes[offset + 0], bytes[offset + 1], bytes[offset + 2], bytes[offset + 3]);
	}

	public static byte long7(long x) {
		return (byte) (x >> 56);
	}

	public static byte long6(long x) {
		return (byte) (x >> 48);
	}

	public static byte long5(long x) {
		return (byte) (x >> 40);
	}

	public static byte long4(long x) {
		return (byte) (x >> 32);
	}

	public static byte long3(long x) {
		return (byte) (x >> 24);
	}

	public static byte long2(long x) {
		return (byte) (x >> 16);
	}

	public static byte long1(long x) {
		return (byte) (x >> 8);
	}

	public static byte long0(long x) {
		return (byte) (x >> 0);
	}

	@JTranscInline
	public static byte int3(int x) {
		return (byte) (x >> 24);
	}

	@JTranscInline
	public static byte int2(int x) {
		return (byte) (x >> 16);
	}

	@JTranscInline
	public static byte int1(int x) {
		return (byte) (x >> 8);
	}

	@JTranscInline
	public static byte int0(int x) {
		return (byte) (x >> 0);
	}

	static public short makeShort(byte b1, byte b0) {
		return (short) (((b1 & 0xFF) << 8) | ((b0 & 0xFF) << 0));
	}

	static public short makeShort(byte[] bytes) {
		return makeShort(bytes[0], bytes[1]);
	}

	public static void writeCharBE(byte[] out, int offset, char value) {
		out[offset + 0] = (byte) ((value >>> 8) & 0xFF);
		out[offset + 1] = (byte) ((value >>> 0) & 0xFF);
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
		int h = (int) (value >> 32);
		int l = (int) (value >> 0);
		out[offset + 0] = (byte) (h >>> 24);
		out[offset + 1] = (byte) (h >>> 16);
		out[offset + 2] = (byte) (h >>> 8);
		out[offset + 3] = (byte) (h >>> 0);
		out[offset + 4] = (byte) (l >>> 24);
		out[offset + 5] = (byte) (l >>> 16);
		out[offset + 6] = (byte) (l >>> 8);
		out[offset + 7] = (byte) (l >>> 0);
	}

	public static void writeFloatBE(byte[] out, int offset, float value) {
		writeIntBE(out, offset, Float.floatToRawIntBits(value));
	}

	public static void writeDoubleBE(byte[] out, int offset, double value) {
		writeLongBE(out, offset, Double.doubleToRawLongBits(value));
	}

	public static void writeCharLE(byte[] out, int offset, char value) {
		out[offset + 1] = (byte) ((value >>> 8) & 0xFF);
		out[offset + 0] = (byte) ((value >>> 0) & 0xFF);
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

	public static void writeFloatLE(byte[] out, int offset, float value) {
		writeIntLE(out, offset, Float.floatToRawIntBits(value));
	}

	public static void writeLongLE(byte[] out, int offset, long value) {
		int h = (int) (value >> 32);
		int l = (int) (value >> 0);
		out[offset + 7] = (byte) (h >>> 24);
		out[offset + 6] = (byte) (h >>> 16);
		out[offset + 5] = (byte) (h >>> 8);
		out[offset + 4] = (byte) (h >>> 0);
		out[offset + 3] = (byte) (l >>> 24);
		out[offset + 2] = (byte) (l >>> 16);
		out[offset + 1] = (byte) (l >>> 8);
		out[offset + 0] = (byte) (l >>> 0);
	}

	public static void writeDoubleLE(byte[] out, int offset, double value) {
		writeLongLE(out, offset, Double.doubleToRawLongBits(value));
	}

	public static void writeShortBE(byte[] out, short value) {
		writeShortBE(out, 0, value);
	}

	public static void writeIntBE(byte[] out, int value) {
		writeIntBE(out, 0, value);
	}

	public static void writeLongBE(byte[] out, long value) {
		writeLongBE(out, 0, value);
	}

	public static void writeShortLE(byte[] out, short value) {
		writeShortLE(out, 0, value);
	}

	public static void writeIntLE(byte[] out, int value) {
		writeIntLE(out, 0, value);
	}

	public static void writeLongLE(byte[] out, long value) {
		writeLongLE(out, 0, value);
	}

	public static void writeFloatLE(byte[] out, float value) {
		writeFloatLE(out, 0, value);
	}

	public static void writeDoubleLE(byte[] out, double value) {
		writeDoubleLE(out, 0, value);
	}

	public static void writeShort(byte[] out, short value) {
		writeShortBE(out, value);
	}

	public static void writeInt(byte[] out, int value) {
		writeIntBE(out, value);
	}

	public static void writeLong(byte[] out, long value) {
		writeLongBE(out, value);
	}

	public static void writeShort(byte[] out, int offset, short value, boolean isLittleEndian) {
		if (isLittleEndian) {
			writeShortLE(out, offset, value);
		} else {
			writeShortBE(out, offset, value);
		}
	}

	public static void writeChar(byte[] out, int offset, char value, boolean isLittleEndian) {
		if (isLittleEndian) {
			writeShortLE(out, offset, (short) value);
		} else {
			writeShortBE(out, offset, (short) value);
		}
	}

	public static void writeInt(byte[] out, int offset, int value, boolean isLittleEndian) {
		if (isLittleEndian) {
			writeIntLE(out, offset, value);
		} else {
			writeIntBE(out, offset, value);
		}
	}

	public static void writeFloat(byte[] out, int offset, float value, boolean isLittleEndian) {
		if (isLittleEndian) {
			writeIntLE(out, offset, Float.floatToRawIntBits(value));
		} else {
			writeIntBE(out, offset, Float.floatToRawIntBits(value));
		}
	}

	public static void writeLong(byte[] out, int offset, long value, boolean isLittleEndian) {
		if (isLittleEndian) {
			writeLongLE(out, offset, value);
		} else {
			writeLongBE(out, offset, value);
		}
	}

	public static void writeDouble(byte[] out, int offset, double value, boolean isLittleEndian) {
		if (isLittleEndian) {
			writeLongLE(out, offset, Double.doubleToRawLongBits(value));
		} else {
			writeLongBE(out, offset, Double.doubleToRawLongBits(value));
		}
	}

	public static int addUint(int base, int offset) {
		int out = base + offset;
		return (out >= 0) ? out : Integer.MAX_VALUE;
	}

	@JTranscMethodBody(target = "js", value = "return N.isLittleEndian;")
	public static boolean isLittleEndian() {
		// @TODO: Check!
		return true;
	}

	public static int mask(int bits) {
		return ((1 << bits) - 1);
	}

	public static long lowMask(String chars) {
		int n = chars.length();
		long m = 0;
		for (int i = 0; i < n; i++) {
			char c = chars.charAt(i);
			if (c < 64) m |= (1L << c);
		}
		return m;
	}

	public static long highMask(String chars) {
		int n = chars.length();
		long m = 0;
		for (int i = 0; i < n; i++) {
			char c = chars.charAt(i);
			if ((c >= 64) && (c < 128)) m |= (1L << (c - 64));
		}
		return m;
	}

	public static long lowMask(char first, char last) {
		long m = 0;
		int f = Math.max(Math.min(first, 63), 0);
		int l = Math.max(Math.min(last, 63), 0);
		for (int i = f; i <= l; i++) m |= 1L << i;
		return m;
	}

	public static long highMask(char first, char last) {
		long m = 0;
		int f = Math.max(Math.min(first, 127), 64) - 64;
		int l = Math.max(Math.min(last, 127), 64) - 64;
		for (int i = f; i <= l; i++) m |= 1L << i;
		return m;
	}

	static public byte[] getInt64BE(byte[] data, long v) {
		data[0] = (byte) (v >> 56);
		data[1] = (byte) (v >> 48);
		data[2] = (byte) (v >> 40);
		data[3] = (byte) (v >> 32);
		data[4] = (byte) (v >> 24);
		data[5] = (byte) (v >> 16);
		data[6] = (byte) (v >> 8);
		data[7] = (byte) (v >> 0);
		return data;
	}

	static public byte[] getInt32BE(byte[] data, int v) {
		data[0] = (byte) (v >> 24);
		data[1] = (byte) (v >> 16);
		data[2] = (byte) (v >> 8);
		data[3] = (byte) (v >> 0);
		return data;
	}

	static public byte[] getInt16BE(byte[] data, short v) {
		data[0] = (byte) (v >> 8);
		data[1] = (byte) (v >> 0);
		return data;
	}

	static public byte[] getInt8BE(byte[] data, byte v) {
		data[0] = v;
		return data;
	}

	static public short readInt16BE(byte[] data, int offset) {
		return (short) (
			((data[offset + 0] & 0xFF) << 8) |
				((data[offset + 1] & 0xFF) << 0)
		);
	}

	static public int readInt32BE(byte[] data, int offset) {
		return (
			((data[offset + 0] & 0xFF) << 24) |
				((data[offset + 1] & 0xFF) << 16) |
				((data[offset + 2] & 0xFF) << 8) |
				((data[offset + 3] & 0xFF) << 0)
		);
	}

	static public long readInt64BE(byte[] data, int offset) {
		return (
			((long) (data[offset + 0] & 0xFF) << 56) |
				((long) (data[offset + 1] & 0xFF) << 48) |
				((long) (data[offset + 2] & 0xFF) << 42) |
				((long) (data[offset + 3] & 0xFF) << 32) |
				((long) (data[offset + 4] & 0xFF) << 24) |
				((long) (data[offset + 5] & 0xFF) << 16) |
				((long) (data[offset + 6] & 0xFF) << 8) |
				((long) (data[offset + 7] & 0xFF) << 0)
		);
	}

	static public short readInt16BE(byte[] data) {
		return readInt16BE(data, 0);
	}

	static public int readInt32BE(byte[] data) {
		return readInt32BE(data, 0);
	}

	static public long readInt64BE(byte[] data) {
		return readInt64BE(data, 0);
	}

	static public short readInt16(byte[] data, int offset, boolean LE) {
		return LE ? readInt16LE(data, offset) : readInt16BE(data, offset);
	}

	static public short readInt16LE(byte[] data, int offset) {
		return (short) (
			((data[offset + 1] & 0xFF) << 8) |
				((data[offset + 0] & 0xFF) << 0)
		);
	}

	static public int readInt32LE(byte[] data, int offset) {
		return (
			((data[offset + 3] & 0xFF) << 24) |
				((data[offset + 2] & 0xFF) << 16) |
				((data[offset + 1] & 0xFF) << 8) |
				((data[offset + 0] & 0xFF) << 0)
		);
	}

	static public long readInt64LE(byte[] data, int offset) {
		return (
			((long) (data[offset + 7] & 0xFF) << 56) |
				((long) (data[offset + 6] & 0xFF) << 48) |
				((long) (data[offset + 5] & 0xFF) << 42) |
				((long) (data[offset + 4] & 0xFF) << 32) |
				((long) (data[offset + 3] & 0xFF) << 24) |
				((long) (data[offset + 2] & 0xFF) << 16) |
				((long) (data[offset + 1] & 0xFF) << 8) |
				((long) (data[offset + 0] & 0xFF) << 0)
		);
	}

	public static int clamp(int v, int min, int max) {
		if (v < min) return min;
		if (v > max) return max;
		return v;
	}

	@JTranscInline
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "flash", value = "return flash.Memory.signExtend1(p0);"),
	})
	static public int sxi1(int value) {
		return (value << 31) >> 31;
	}

	@JTranscInline
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "flash", value = "return flash.Memory.signExtend8(p0);"),
		@HaxeMethodBody("return N.i2b(p0);"),
	})
	static public int sxi8(int value) {
		return (value << 24) >> 24;
	}

	@JTranscInline
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "flash", value = "return flash.Memory.signExtend16(p0);"),
		@HaxeMethodBody("return N.i2s(p0);"),
	})
	static public int sxi16(int value) {
		return (value << 16) >> 16;
	}


	static public short readInt16(ByteBuffer in, boolean LE) {
		int _0 = in.get() & 0xFF;
		int _1 = in.get() & 0xFF;

		return (short) (
			LE  ?
				(_1 << 8) | (_0 << 0)
				:
				(_0 << 8) | (_1 << 0)
		);
	}

	static public char reverseBytes(char value) {
		return Character.reverseBytes(value);
	}

	static public short reverseBytes(short value) {
		return Short.reverseBytes(value);
	}

	static public int reverseBytes(int value) {
		return Integer.reverseBytes(value);
	}

	static public long reverseBytes(long value) {
		return Long.reverseBytes(value);
	}

	//static public double reverseBytes(double value) {
	//	return Double.longBitsToDouble(Long.reverseBytes(Double.doubleToRawLongBits(value)));
	//}
//
	//static public float reverseBytes(float value) {
	//	return Float.floatToRawIntBits(Integer.reverseBytes(Float.floatToRawIntBits(value)));
	//}
}
