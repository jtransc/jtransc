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

package java.lang;

import jtransc.annotation.JTranscKeep;
import jtransc.annotation.haxe.HaxeMethodBody;
import jtransc.internal.JTranscCType;

public final class Long extends Number implements Comparable<Long> {
	public static final long MIN_VALUE = 0x8000000000000000L;
	public static final long MAX_VALUE = 0x7fffffffffffffffL;
	public static final Class<Long> TYPE = (Class<Long>) Class.getPrimitiveClass("long");

	native public static String toString(long i, int radix);

	native public static String toUnsignedString(long i, int radix);

	private static String toBaseString(long i, int base) {
		String out = "";
		if (i < 0) {
			out += "-";
			i = -i;
		}
		while (i > 0) {
			out += JTranscCType.encodeDigit((int) (i % base));
			i /= base;
		}
		return out;
	}

	public static String toHexString(long i) {
		return toBaseString(i, 16);
	}

	public static String toOctalString(long i) {
		return toBaseString(i, 8);
	}

	public static String toBinaryString(long i) {
		return toBaseString(i, 2);
	}

    @HaxeMethodBody("return HaxeNatives.str('' + p0);")
    native public static String toString(long i);

	native public static String toUnsignedString(long i);

	native public static long parseLong(String s, int radix) throws NumberFormatException;

	native public static long parseLong(String s) throws NumberFormatException;

	native public static long parseUnsignedLong(String s, int radix) throws NumberFormatException;

	native public static long parseUnsignedLong(String s) throws NumberFormatException;

	native public static Long valueOf(String s, int radix) throws NumberFormatException;

	native public static Long valueOf(String s) throws NumberFormatException;

	@JTranscKeep
	public static Long valueOf(long l) {
		return new Long(l);
	}

	native public static Long decode(String nm) throws NumberFormatException;

	private final long value;

	public Long(long value) {
		this.value = value;
	}

	public Long(String s) throws NumberFormatException {
		this.value = parseLong(s, 10);
	}

	public byte byteValue() {
		return (byte) value;
	}

	public short shortValue() {
		return (short) value;
	}

	public int intValue() {
		return (int) value;
	}

	public long longValue() {
		return value;
	}

	public float floatValue() {
		return (float) value;
	}

	public double doubleValue() {
		return (double) value;
	}

	public String toString() {
		return toString(value);
	}

	@Override
	public int hashCode() {
		return Long.hashCode(value);
	}

	public static int hashCode(long value) {
		return (int) (value ^ (value >>> 32));
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Long)) return false;
		return value == ((Long) obj).longValue();
	}


	native public static Long getLong(String nm);

	native public static Long getLong(String nm, long val);

	native public int compareTo(Long anotherLong);

	public static int compare(long x, long y) {
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}

	public static int compareUnsigned(long x, long y) {
		return compare(x + MIN_VALUE, y + MIN_VALUE);
	}

	native public static long divideUnsigned(long dividend, long divisor);

	native public static long remainderUnsigned(long dividend, long divisor);

	public static final int SIZE = 64;
	public static final int BYTES = SIZE / Byte.SIZE;

	native public static long highestOneBit(long value);

	native public static long lowestOneBit(long value);

	native public static int numberOfLeadingZeros(long value);

	public static int numberOfTrailingZeros(long value) {
		// HD, Figure 5-14
		int x, y;
		if (value == 0) return 64;
		int n = 63;
		y = (int) value;
		if (y != 0) {
			n = n - 32;
			x = y;
		} else x = (int) (value >>> 32);
		y = x << 16;
		if (y != 0) {
			n = n - 16;
			x = y;
		}
		y = x << 8;
		if (y != 0) {
			n = n - 8;
			x = y;
		}
		y = x << 4;
		if (y != 0) {
			n = n - 4;
			x = y;
		}
		y = x << 2;
		if (y != 0) {
			n = n - 2;
			x = y;
		}
		return n - ((x << 1) >>> 31);
	}

	native public static int bitCount(long value);

	native public static long rotateLeft(long value, int distance);

	native public static long rotateRight(long value, int distance);

	native public static long reverse(long value);

	native public static int signum(long value);

	native public static long reverseBytes(long value);

	native public static long sum(long l, long r);

	native public static long max(long l, long r);

	native public static long min(long l, long r);
}
