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

package jtransc;

import jtransc.annotation.JTranscInvisible;

@JTranscInvisible
public class JTranscBits {
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

	static public int makeInt(byte b3, byte b2, byte b1, byte b0) {
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

	static public short makeShort(byte b1, byte b0) {
		return (short) (((b1 & 0xFF) << 8) | ((b0 & 0xFF) << 0));
	}

	static public short makeShort(byte[] bytes) {
		return makeShort(bytes[0], bytes[1]);
	}

	static public void i2bLittle4(int value, byte[] out, int offset) {
		if (offset < 0 || out.length - offset < 4) {
			throw new ArrayIndexOutOfBoundsException();
		}
		out[offset + 0] = (byte) (value >>  0);
		out[offset + 1] = (byte) (value >>  8);
		out[offset + 2] = (byte) (value >> 16);
		out[offset + 3] = (byte) (value >> 24);
	}

	static public void b2iLittle64(byte[] var0, int var1, int[] var2) {
		if (var1 >= 0 && var0.length - var1 >= 64 && var2.length >= 16) {
			b2iLittle(var0, var1, var2, 0, 64);
		} else {
			throw new ArrayIndexOutOfBoundsException();
		}
	}

	static public void b2iLittle(byte[] var0, int var1, int[] var2, int var3, int var4) {
		if (var1 >= 0 && var0.length - var1 >= var4 && var3 >= 0 && var2.length - var3 >= var4 / 4) {
			for (var4 += var1; var1 < var4; var1 += 4) {
				var2[var3++] = var0[var1] & 255 | (var0[var1 + 1] & 255) << 8 | (var0[var1 + 2] & 255) << 16 | var0[var1 + 3] << 24;
			}
		} else {
			throw new ArrayIndexOutOfBoundsException();
		}
	}

	static public void i2bLittle(int[] var0, int var1, byte[] var2, int var3, int var4) {
		if (var1 >= 0 && var0.length - var1 >= var4 / 4 && var3 >= 0 && var2.length - var3 >= var4) {
			int var5;
			for (var4 += var3; var3 < var4; var2[var3++] = (byte) (var5 >> 24)) {
				var5 = var0[var1++];
				var2[var3++] = (byte) var5;
				var2[var3++] = (byte) (var5 >> 8);
				var2[var3++] = (byte) (var5 >> 16);
			}
		} else {
			throw new ArrayIndexOutOfBoundsException();
		}
	}

	public static void writeShort(byte[] out, short value) {
		out[0] = (byte) ((value >>> 8) & 0xFF);
		out[1] = (byte) ((value >>> 0) & 0xFF);
	}

	public static void writeInt(byte[] out, int value) {
		out[0] = (byte) ((value >>> 24) & 0xFF);
		out[1] = (byte) ((value >>> 16) & 0xFF);
		out[2] = (byte) ((value >>> 8) & 0xFF);
		out[3] = (byte) ((value >>> 0) & 0xFF);
	}

	public static void writeLong(byte[] out, long v) {
		out[0] = (byte) (v >>> 56);
		out[1] = (byte) (v >>> 48);
		out[2] = (byte) (v >>> 40);
		out[3] = (byte) (v >>> 32);
		out[4] = (byte) (v >>> 24);
		out[5] = (byte) (v >>> 16);
		out[6] = (byte) (v >>> 8);
		out[7] = (byte) (v >>> 0);
	}

	public static int addUint(int base, int offset) {
		int out = base + offset;
		return (out >= 0) ? out : Integer.MAX_VALUE;
	}

	public static boolean isLittleEndian() {
		return true;
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
}
