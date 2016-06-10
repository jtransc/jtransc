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

import com.jtransc.annotation.JTranscKeep;
import com.jtransc.internal.JTranscCType;

public final class Long extends Number implements Comparable<Long> {
	public static final long MIN_VALUE = 0x8000000000000000L;
	public static final long MAX_VALUE = 0x7fffffffffffffffL;
	public static final Class<Long> TYPE = (Class<Long>) Class.getPrimitiveClass("long");

	public static String toString(long i, int radix) {
		if (i == 0) return "0";
		if (i == MIN_VALUE) return "-9223372036854775808";
		StringBuilder out = new StringBuilder();
		boolean negative = (i < 0);
		if (negative) i = -i;
		while (i != 0) {
			out.append(JTranscCType.encodeDigit((int) (((i % radix) + radix) % radix)));
			i /= radix;
		}
		if (negative) out.append("-");
		out.reverse();
		return out.toString();
	}

	public static String toUnsignedString(long i, int radix) {
		if (i == 0) return "0";
		StringBuilder out = new StringBuilder();
		while (i != 0) {
			out.append(JTranscCType.encodeDigit((int) Long.remainderUnsigned(i, radix)));
			i = Long.divideUnsigned(i, radix);
		}
		out.reverse();
		return out.toString();
	}

	public static String toHexString(long i) {
		return toUnsignedString(i, 16);
	}

	public static String toOctalString(long i) {
		return toUnsignedString(i, 8);
	}

	public static String toBinaryString(long i) {
		return toUnsignedString(i, 2);
	}

	//@HaxeMethodBody("return HaxeNatives.str('' + p0);")
	public static String toString(long i) {
		return toString(i, 10);
	}

	public static String toUnsignedString(long i) {
		return toUnsignedString(i, 10);
	}

	public static long parseLong(String s, int radix) throws NumberFormatException {
		long result = 0;
		int len = s.length();
		boolean negative = (s.charAt(0) == '-');
		int sign = negative ? -1 : 1;
		int n = negative ? 1 : 0;
		for (; n < len; n++) {
			result *= radix;
			result += JTranscCType.decodeDigit(s.charAt(n));
		}
		return result * sign;
	}

	// @TODO: CHECK!
	public static long parseUnsignedLong(String s, int radix) throws NumberFormatException {
		return parseLong(s, radix);
	}

	public static Long valueOf(String s, int radix) throws NumberFormatException {
		return Long.valueOf(parseLong(s, radix));
	}

	public static long parseLong(String s) throws NumberFormatException {
		return parseLong(s, 10);
	}

	public static long parseUnsignedLong(String s) throws NumberFormatException {
		return parseUnsignedLong(s, 10);
	}

	public static Long valueOf(String s) throws NumberFormatException {
		return valueOf(s, 10);
	}

	@JTranscKeep
	public static Long valueOf(long l) {
		return new Long(l);
	}

	public static Long decode(String nm) throws NumberFormatException {
		if (nm.length() == 0) throw new NumberFormatException("Zero length string");
		if (nm.startsWith("-")) return -decode(nm.substring(1));
		if (nm.startsWith("+")) return decode(nm.substring(1));
		if (nm.startsWith("0x")) return parseLong(nm.substring(2), 16);
		if (nm.startsWith("0X")) return parseLong(nm.substring(2), 16);
		if (nm.startsWith("#")) return parseLong(nm.substring(1), 16);
		if (nm.startsWith("0")) return parseLong(nm.substring(1), 8);
		return parseLong(nm, 10);
	}

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

	public static Long getLong(String nm) {
		return getLong(nm, null);
	}

	public static Long getLong(String nm, long val) {
		return getLong(nm, Long.valueOf(val));
	}

	public static Long getLong(String nm, Long val) {
		String out = System.getProperty(nm);
		if (out == null) return val;
		try {
			return decode(nm);
		} catch (NumberFormatException e) {
			return val;
		}
	}

	public int compareTo(Long anotherLong) {
		return compare(this.value, anotherLong.value);
	}

	public static int compare(long x, long y) {
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}

	public static int compareUnsigned(long x, long y) {
		return compare(x ^ MIN_VALUE, y ^ MIN_VALUE);
	}

	// https://github.com/google/guava/blob/master/guava/src/com/google/common/primitives/UnsignedLongs.java
	// https://github.com/google/guava/blob/master/guava/src/com/google/common/primitives/UnsignedLong.java
	public static long divideUnsigned(long dividend, long divisor) {
		if (divisor < 0) return (compareUnsigned(dividend, divisor) < 0) ? 0 : 1;
		if (dividend >= 0) return dividend / divisor;
		long quotient = ((dividend >>> 1) / divisor) << 1;
		long rem = dividend - quotient * divisor;
		return quotient + (compareUnsigned(rem, divisor) >= 0 ? 1 : 0);
	}

	public static long remainderUnsigned(long dividend, long divisor) {
		if (divisor < 0) return (compareUnsigned(dividend, divisor) < 0) ? dividend : (dividend - divisor);
		if (dividend >= 0) return dividend % divisor;
		long quotient = ((dividend >>> 1) / divisor) << 1;
		long rem = dividend - quotient * divisor;
		return rem - (compareUnsigned(rem, divisor) >= 0 ? divisor : 0);
	}

	public static final int SIZE = 64;
	public static final int BYTES = SIZE / Byte.SIZE;

	public static long highestOneBit(long v) {
		v |= (v >> 1);
		v |= (v >> 2);
		v |= (v >> 4);
		v |= (v >> 8);
		v |= (v >> 16);
		v |= (v >> 32);
		return v - (v >>> 1);
	}

	public static long lowestOneBit(long v) {
		return v & -v;
	}

	public static int numberOfLeadingZeros(long v) {
		if (v == 0) return 64;
		int n = 1;
		int x = (int) (v >>> 32);
		if (x == 0) {
			n += 32;
			x = (int) v;
		}
		if (x >>> 16 == 0) {
			n += 16;
			x <<= 16;
		}
		if (x >>> 24 == 0) {
			n += 8;
			x <<= 8;
		}
		if (x >>> 28 == 0) {
			n += 4;
			x <<= 4;
		}
		if (x >>> 30 == 0) {
			n += 2;
			x <<= 2;
		}
		n -= x >>> 31;
		return n;
	}

	public static int numberOfTrailingZeros(long value) {
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

	public static int bitCount(long v) {
		v = v - ((v >>> 1) & 0x5555555555555555L);
		v = (v & 0x3333333333333333L) + ((v >>> 2) & 0x3333333333333333L);
		v = (v + (v >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
		v = v + (v >>> 8);
		v = v + (v >>> 16);
		v = v + (v >>> 32);
		return (int) v & 0x7f;
	}

	public static long rotateLeft(long value, int distance) {
		return (value << distance) | (value >>> -distance);
	}

	public static long rotateRight(long value, int distance) {
		return (value >>> distance) | (value << -distance);
	}

	public static long reverse(long v) {
		v = (v & 0x5555555555555555L) << 1 | (v >>> 1) & 0x5555555555555555L;
		v = (v & 0x3333333333333333L) << 2 | (v >>> 2) & 0x3333333333333333L;
		v = (v & 0x0f0f0f0f0f0f0f0fL) << 4 | (v >>> 4) & 0x0f0f0f0f0f0f0f0fL;
		v = (v & 0x00ff00ff00ff00ffL) << 8 | (v >>> 8) & 0x00ff00ff00ff00ffL;
		v = (v << 48) | ((v & 0xffff0000L) << 16) | ((v >>> 16) & 0xffff0000L) | (v >>> 48);
		return v;
	}

	public static int signum(long v) {
		return (int) ((v >> 63) | (-v >>> 63));
	}

	public static long reverseBytes(long v) {
		v = (v & 0x00ff00ff00ff00ffL) << 8 | (v >>> 8) & 0x00ff00ff00ff00ffL;
		return (v << 48) | ((v & 0xffff0000L) << 16) | ((v >>> 16) & 0xffff0000L) | (v >>> 48);
	}

	public static long sum(long l, long r) {
		return l + r;
	}

	public static long max(long l, long r) {
		return Math.max(l, r);
	}

	public static long min(long l, long r) {
		return Math.min(l, r);
	}
}
