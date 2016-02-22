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

package java.nio;

import jtransc.JTranscBits;

class Bits {
	private Bits() {
	}

	static char getCharL(ByteBuffer bb, int bi) {
		return JTranscBits.makeChar(bb._get(bi + 1), bb._get(bi));
	}

	static char getCharB(ByteBuffer bb, int bi) {
		return JTranscBits.makeChar(bb._get(bi), bb._get(bi + 1));
	}

	static char getChar(ByteBuffer bb, int bi, boolean bigEndian) {
		return bigEndian ? getCharB(bb, bi) : getCharL(bb, bi);
	}

	private static byte char1(char x) {
		return (byte) (x >> 8);
	}

	private static byte char0(char x) {
		return (byte) (x);
	}

	static void putCharL(ByteBuffer bb, int bi, char x) {
		bb._put(bi, char0(x));
		bb._put(bi + 1, char1(x));
	}

	static void putCharB(ByteBuffer bb, int bi, char x) {
		bb._put(bi, char1(x));
		bb._put(bi + 1, char0(x));
	}

	static void putChar(ByteBuffer bb, int bi, char x, boolean bigEndian) {
		if (bigEndian) putCharB(bb, bi, x);
		else putCharL(bb, bi, x);
	}

	static short getShortL(ByteBuffer bb, int bi) {
		return JTranscBits.makeShort(bb._get(bi + 1), bb._get(bi));
	}

	static short getShortB(ByteBuffer bb, int bi) {
		return JTranscBits.makeShort(bb._get(bi), bb._get(bi + 1));
	}

	static short getShort(ByteBuffer bb, int bi, boolean bigEndian) {
		return bigEndian ? getShortB(bb, bi) : getShortL(bb, bi);
	}

	private static byte short1(short x) {
		return (byte) (x >> 8);
	}

	private static byte short0(short x) {
		return (byte) (x);
	}

	static void putShortL(ByteBuffer bb, int bi, short x) {
		bb._put(bi, short0(x));
		bb._put(bi + 1, short1(x));
	}

	static void putShortB(ByteBuffer bb, int bi, short x) {
		bb._put(bi, short1(x));
		bb._put(bi + 1, short0(x));
	}

	static void putShort(ByteBuffer bb, int bi, short x, boolean bigEndian) {
		if (bigEndian)
			putShortB(bb, bi, x);
		else
			putShortL(bb, bi, x);
	}

	// -- get/put int --

	static int getIntL(ByteBuffer bb, int bi) {
		return JTranscBits.makeInt(bb._get(bi + 3), bb._get(bi + 2), bb._get(bi + 1), bb._get(bi));
	}

	static int getIntB(ByteBuffer bb, int bi) {
		return JTranscBits.makeInt(bb._get(bi), bb._get(bi + 1), bb._get(bi + 2), bb._get(bi + 3));
	}

	static int getInt(ByteBuffer bb, int bi, boolean bigEndian) {
		return bigEndian ? getIntB(bb, bi) : getIntL(bb, bi);
	}

	private static byte int3(int x) {
		return (byte) (x >> 24);
	}

	private static byte int2(int x) {
		return (byte) (x >> 16);
	}

	private static byte int1(int x) {
		return (byte) (x >> 8);
	}

	private static byte int0(int x) {
		return (byte) (x);
	}

	static void putIntL(ByteBuffer bb, int bi, int x) {
		bb._put(bi + 3, int3(x));
		bb._put(bi + 2, int2(x));
		bb._put(bi + 1, int1(x));
		bb._put(bi, int0(x));
	}

	static void putIntB(ByteBuffer bb, int bi, int x) {
		bb._put(bi, int3(x));
		bb._put(bi + 1, int2(x));
		bb._put(bi + 2, int1(x));
		bb._put(bi + 3, int0(x));
	}

	static void putInt(ByteBuffer bb, int bi, int x, boolean bigEndian) {
		if (bigEndian) putIntB(bb, bi, x);
		else putIntL(bb, bi, x);
	}

	// -- get/put long --

	static long getLongL(ByteBuffer bb, int bi) {
		return JTranscBits.makeLong(bb._get(bi + 7), bb._get(bi + 6), bb._get(bi + 5), bb._get(bi + 4), bb._get(bi + 3), bb._get(bi + 2), bb._get(bi + 1), bb._get(bi));
	}

	static long getLongB(ByteBuffer bb, int bi) {
		return JTranscBits.makeLong(bb._get(bi), bb._get(bi + 1), bb._get(bi + 2), bb._get(bi + 3), bb._get(bi + 4), bb._get(bi + 5), bb._get(bi + 6), bb._get(bi + 7));
	}

	static long getLong(ByteBuffer bb, int bi, boolean bigEndian) {
		return bigEndian ? getLongB(bb, bi) : getLongL(bb, bi);
	}

	static void putLongL(ByteBuffer bb, int bi, long x) {
		bb._put(bi + 7, JTranscBits.long7(x));
		bb._put(bi + 6, JTranscBits.long6(x));
		bb._put(bi + 5, JTranscBits.long5(x));
		bb._put(bi + 4, JTranscBits.long4(x));
		bb._put(bi + 3, JTranscBits.long3(x));
		bb._put(bi + 2, JTranscBits.long2(x));
		bb._put(bi + 1, JTranscBits.long1(x));
		bb._put(bi + 0, JTranscBits.long0(x));
	}

	static void putLongB(ByteBuffer bb, int bi, long x) {
		bb._put(bi + 0, JTranscBits.long7(x));
		bb._put(bi + 1, JTranscBits.long6(x));
		bb._put(bi + 2, JTranscBits.long5(x));
		bb._put(bi + 3, JTranscBits.long4(x));
		bb._put(bi + 4, JTranscBits.long3(x));
		bb._put(bi + 5, JTranscBits.long2(x));
		bb._put(bi + 6, JTranscBits.long1(x));
		bb._put(bi + 7, JTranscBits.long0(x));
	}

	static void putLong(ByteBuffer bb, int bi, long x, boolean bigEndian) {
		if (bigEndian) putLongB(bb, bi, x);
		else putLongL(bb, bi, x);
	}

	// -- get/put float --

	static float getFloatL(ByteBuffer bb, int bi) {
		return Float.intBitsToFloat(getIntL(bb, bi));
	}

	static float getFloatB(ByteBuffer bb, int bi) {
		return Float.intBitsToFloat(getIntB(bb, bi));
	}

	static float getFloat(ByteBuffer bb, int bi, boolean bigEndian) {
		return bigEndian ? getFloatB(bb, bi) : getFloatL(bb, bi);
	}

	static void putFloatL(ByteBuffer bb, int bi, float x) {
		putIntL(bb, bi, Float.floatToRawIntBits(x));
	}

	static void putFloatB(ByteBuffer bb, int bi, float x) {
		putIntB(bb, bi, Float.floatToRawIntBits(x));
	}

	static void putFloat(ByteBuffer bb, int bi, float x, boolean bigEndian) {
		if (bigEndian) putFloatB(bb, bi, x);
		else putFloatL(bb, bi, x);
	}

	static double getDoubleL(ByteBuffer bb, int bi) {
		return Double.longBitsToDouble(getLongL(bb, bi));
	}

	static double getDoubleB(ByteBuffer bb, int bi) {
		return Double.longBitsToDouble(getLongB(bb, bi));
	}

	static double getDouble(ByteBuffer bb, int bi, boolean bigEndian) {
		return bigEndian ? getDoubleB(bb, bi) : getDoubleL(bb, bi);
	}

	static void putDoubleL(ByteBuffer bb, int bi, double x) {
		putLongL(bb, bi, Double.doubleToRawLongBits(x));
	}

	static void putDoubleB(ByteBuffer bb, int bi, double x) {
		putLongB(bb, bi, Double.doubleToRawLongBits(x));
	}

	static void putDouble(ByteBuffer bb, int bi, double x, boolean bigEndian) {
		if (bigEndian) putDoubleB(bb, bi, x);
		else putDoubleL(bb, bi, x);
	}

	private static int pageSize = 4096;

	static int pageSize() {
		return pageSize;
	}

	static int pageCount(long size) {
		return (int) (size + (long) pageSize() - 1L) / pageSize();
	}

	static boolean unaligned() {
		return false;
	}

	private static volatile long maxMemory = 4096;
	private static volatile long reservedMemory;
	private static volatile long totalCapacity;
	private static volatile long count;
	private static boolean memoryLimitSet = false;

	static void unreserveMemory(long size, int cap) {
		if (reservedMemory > 0) {
			reservedMemory -= size;
			totalCapacity -= cap;
			count--;
			assert (reservedMemory > -1);
		}
	}

	static void copyFromCharArray(Object src, long srcPos, long dstAddr, long length) {
		copyFromShortArray(src, srcPos, dstAddr, length);
	}

	static native void copyFromShortArray(Object src, long srcPos, long dstAddr, long length);

	static native void copyFromIntArray(Object src, long srcPos, long dstAddr, long length);

	static native void copyFromLongArray(Object src, long srcPos, long dstAddr, long length);

}
