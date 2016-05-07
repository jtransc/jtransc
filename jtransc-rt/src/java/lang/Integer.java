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
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.internal.JTranscCType;

public final class Integer extends Number implements Comparable<Integer> {
	public static final int MIN_VALUE = 0x80000000;
	public static final int MAX_VALUE = 0x7fffffff;

	public static final Class<Integer> TYPE = (Class<Integer>) Class.getPrimitiveClass("int");

	private int value;

	public Integer(int value) {
		this.value = value;
	}

	public Integer(String s) {
		this.value = parseInt(s, 10);
	}

	static private char[] temp = new char[16];

	@HaxeMethodBody(target = "js", value = "return N.str(untyped __js__('p0.toString(p1)'));")
	public static String toString(int i, int radix) {
		if (i == 0) return "0";
		if (i == MIN_VALUE) return "-2147483648";
		boolean negative = (i < 0);
		if (i < 0) i = -i;
		int tempPos = temp.length;
		while (i != 0) {
			temp[--tempPos] = JTranscCType.encodeDigit(Integer.remainderUnsigned(i, radix));
			i = Integer.divideUnsigned(i, radix);
		}
		if (negative) temp[--tempPos] = '-';
		return new String(temp, tempPos, temp.length - tempPos);
	}

	public static String toUnsignedString(int i, int radix) {
		if (i == 0) return "0";
		StringBuilder out = new StringBuilder();
		while (i != 0) {
			out.append(JTranscCType.encodeDigit(Integer.remainderUnsigned(i, radix)));
			i = Integer.divideUnsigned(i, radix);
		}
		out.reverse();
		return out.toString();
	}

	public static String toHexString(int i) {
		return toUnsignedString(i, 16);
	}

	public static String toOctalString(int i) {
		return toUnsignedString(i, 8);
	}

	public static String toBinaryString(int i) {
		return toUnsignedString(i, 2);
	}

	public static String toString(int i) {
		return toString(i, 10);
	}

	public static String toUnsignedString(int i) {
		return toUnsignedString(i, 10);
	}

	public static int parseInt(String input, int radix) {
		String s = input;
		int result = 0;
		int sign = +1;
		if (s.startsWith("-")) {
			sign = -1;
			s = s.substring(1);
		} else if (s.startsWith("+")) {
			sign = +1;
			s = s.substring(1);
		}
		int len = s.length();
		for (int n = 0; n < len; n++) {
			char c = s.charAt(n);
			if (!JTranscCType.isDigit(c)) {
				throw new NumberFormatException("For input string: \"" + input + "\"");
			}
			result *= radix;
			result += JTranscCType.decodeDigit(c);
			//System.out.println(c + ": " + JTranscCType.decodeDigit(c));
		}
		return sign * result;
	}


	public static int parseInt(String s) {
		return parseInt(s, 10);
	}

	// @TODO: CHECK!
	public static int parseUnsignedInt(String s, int radix) {
		return parseInt(s, radix);
	}

	public static int parseUnsignedInt(String s) {
		return parseUnsignedInt(s, 10);
	}

	public static Integer valueOf(String s, int radix) {
		return new Integer(parseInt(s, radix));
	}

	public static Integer valueOf(String s) {
		return valueOf(s, 10);
	}

	static private Integer[] values;

	static private final int MIN = -128;
	static private final int MAX = 128;
	static private final int LENGTH = MAX - MIN;

	@JTranscKeep
	public static Integer valueOf(int i) {
		if (values == null) {
			values = new Integer[LENGTH];
			for (int n = MIN; n < MAX; n++) values[n - MIN] = new Integer(n);
		}
		if (i >= MIN && i < MAX) {
			return values[i - MIN];
		} else {
			return new Integer(i);
		}
	}

	public byte byteValue() {
		return (byte) value;
	}

	public short shortValue() {
		return (short) value;
	}

	public int intValue() {
		return value;
	}

	public long longValue() {
		return (long) value;
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
		return value;
	}

	public static int hashCode(int value) {
		return value;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Integer) {
			return ((Integer) obj).value == value;
		} else {
			return false;
		}
	}

	public static Integer getInteger(String nm) {
		return getInteger(nm, null);
	}

	public static Integer getInteger(String nm, int val) {
		Integer result = getInteger(nm, null);
		return (result == null) ? new Integer(val) : result;
	}

	public static Integer getInteger(String nm, Integer val) {
		String out = System.getProperty(nm);
		if (out == null) return val;
		try {
			return decode(nm);
		} catch (NumberFormatException e) {
			return val;
		}
	}

	public static Integer decode(String nm) throws NumberFormatException {
		if (nm.length() == 0) throw new NumberFormatException("Zero length string");
		if (nm.startsWith("-")) return -decode(nm.substring(1));
		if (nm.startsWith("+")) return decode(nm.substring(1));
		if (nm.startsWith("0x")) return parseInt(nm.substring(2), 16);
		if (nm.startsWith("0X")) return parseInt(nm.substring(2), 16);
		if (nm.startsWith("#")) return parseInt(nm.substring(1), 16);
		if (nm.startsWith("0")) return parseInt(nm.substring(1), 8);
		return parseInt(nm, 10);
	}

	public int compareTo(Integer anotherInteger) {
		return Integer.compare(value, anotherInteger.value);
	}

	public static int compare(int l, int r) {
		return (l < r) ? -1 : ((l > r) ? 1 : 0);
	}

	public static int compareUnsigned(int l, int r) {
		return compare(l ^ MIN_VALUE, r ^ MIN_VALUE);
	}

	public static long toUnsignedLong(int value) {
		return ((long) value) & 0xffffffffL;
	}

	public static int divideUnsigned(int dividend, int divisor) {
		//return (int) (toUnsignedLong(dividend) / toUnsignedLong(divisor));
		if (divisor < 0) return (compareUnsigned(dividend, divisor) < 0) ? 0 : 1;
		if (dividend >= 0) return dividend / divisor;
		int quotient = ((dividend >>> 1) / divisor) << 1;
		int rem = dividend - quotient * divisor;
		return quotient + (compareUnsigned(rem, divisor) >= 0 ? 1 : 0);
	}

	public static int remainderUnsigned(int dividend, int divisor) {
		//return (int) (toUnsignedLong(dividend) % toUnsignedLong(divisor));
		if (divisor < 0) return (compareUnsigned(dividend, divisor) < 0) ? dividend : (dividend - divisor);
		if (dividend >= 0) return dividend % divisor;
		int quotient = ((dividend >>> 1) / divisor) << 1;
		int rem = dividend - quotient * divisor;
		return rem - (compareUnsigned(rem, divisor) >= 0 ? divisor : 0);
	}

	public static final int SIZE = 32;
	public static final int BYTES = SIZE / Byte.SIZE;

	public static int highestOneBit(int value) {
		value |= (value >> 1);
		value |= (value >> 2);
		value |= (value >> 4);
		value |= (value >> 8);
		value |= (value >> 16);
		return value - (value >>> 1);
	}

	public static int lowestOneBit(int value) {
		return value & -value;
	}

	public static int numberOfLeadingZeros(int value) {
		if (value == 0) return 32;
		int n = 1;
		if (value >>> 16 == 0) {
			n += 16;
			value <<= 16;
		}
		if (value >>> 24 == 0) {
			n += 8;
			value <<= 8;
		}
		if (value >>> 28 == 0) {
			n += 4;
			value <<= 4;
		}
		if (value >>> 30 == 0) {
			n += 2;
			value <<= 2;
		}
		n -= value >>> 31;
		return n;
	}

	public static int numberOfTrailingZeros(int value) {
		int y;
		if (value == 0) return 32;
		int n = 31;
		y = value << 16;
		if (y != 0) {
			n = n - 16;
			value = y;
		}
		y = value << 8;
		if (y != 0) {
			n = n - 8;
			value = y;
		}
		y = value << 4;
		if (y != 0) {
			n = n - 4;
			value = y;
		}
		y = value << 2;
		if (y != 0) {
			n = n - 2;
			value = y;
		}
		return n - ((value << 1) >>> 31);
	}

	public static int bitCount(int value) {
		value = value - ((value >>> 1) & 0x55555555);
		value = (value & 0x33333333) + ((value >>> 2) & 0x33333333);
		value = (value + (value >>> 4)) & 0x0f0f0f0f;
		value = value + (value >>> 8);
		value = value + (value >>> 16);
		return value & 0x3f;
	}

	public static int rotateLeft(int value, int distance) {
		return (value << distance) | (value >>> -distance);
	}

	public static int rotateRight(int value, int distance) {
		return (value >>> distance) | (value << -distance);
	}

	public static int reverse(int value) {
		value = (value & 0x55555555) << 1 | (value >>> 1) & 0x55555555;
		value = (value & 0x33333333) << 2 | (value >>> 2) & 0x33333333;
		value = (value & 0x0f0f0f0f) << 4 | (value >>> 4) & 0x0f0f0f0f;
		value = (value << 24) | ((value & 0xff00) << 8) | ((value >>> 8) & 0xff00) | (value >>> 24);
		return value;
	}

	public static int signum(int value) {
		return (value >> 31) | (-value >>> 31);
	}

	@HaxeMethodBody("return HaxeNatives.swap32(p0);")
	public static int reverseBytes(int value) {
		return ((value >>> 24)) | ((value >> 8) & 0xFF00) | ((value << 8) & 0xFF0000) | ((value << 24));
	}

	public static int sum(int l, int r) {
		return l + r;
	}

	public static int max(int l, int r) {
		return Math.max(l, r);
	}

	public static int min(int l, int r) {
		return Math.min(l, r);
	}
}