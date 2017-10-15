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

import com.jtransc.annotation.JTranscAsync;
import com.jtransc.annotation.JTranscKeep;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscSync;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.internal.JTranscCType;

@SuppressWarnings({"WeakerAccess", "unused", "NullableProblems"})
public final class Integer extends Number implements Comparable<Integer> {
	public static final int MIN_VALUE = 0x80000000;
	public static final int MAX_VALUE = 0x7fffffff;
	public static final int SIZE = 32;
	public static final int BYTES = SIZE / Byte.SIZE;

	static private Integer[] values;
	static private final int MIN = -128;
	static private final int MAX = 128;
	static private final int LENGTH = MAX - MIN;

	@SuppressWarnings("unchecked")
	public static final Class<Integer> TYPE = (Class<Integer>) Class.getPrimitiveClass("int");

	private int value;

	@JTranscSync
	public Integer(int value) {
		this.value = value;
	}

	@JTranscSync
	public Integer(String s) {
		this.value = parseInt(s, 10);
	}

	@HaxeMethodBody(target = "js", value = "return N.str(untyped __js__('p0.toString(p1)'));")
	@JTranscMethodBody(target = "js", value = "return N.str((p0|0).toString(p1));")
	@JTranscMethodBody(target = "as3", value = "return N.str((p0|0).toString(p1));")
	//@JTranscMethodBody(target = "cpp", value = "wchar_t temp[64] = {0}; ::_itow_s(p0, temp, sizeof(temp), p1); return N::str(std::wstring(temp));")
	@JTranscMethodBody(target = "dart", value = "return N.str(p0.toRadixString(p1));")
	//@JTranscMethodBody(target = "php", value = "return N::str(base_convert(\"$p0\", 10, $p1));")
	@JTranscSync
	public static String toString(int i, int radix) {
		if (i == 0) return "0";
		char[] out = new char[IntegerTools.countDigits(i, radix)];
		int count = IntegerTools.writeInt(out, 0, i, radix);
		return new String(out, 0, count);
	}

	@JTranscMethodBody(target = "js", value = "return N.str((p0 >>> 0).toString(p1));")
	@JTranscMethodBody(target = "as3", value = "return N.str(uint(p0).toString(p1));")
	@JTranscSync
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

	@JTranscSync
	public static String toHexString(int i) {
		return toUnsignedString(i, 16);
	}

	@JTranscSync
	public static String toOctalString(int i) {
		return toUnsignedString(i, 8);
	}

	@JTranscSync
	public static String toBinaryString(int i) {
		return toUnsignedString(i, 2);
	}

	@JTranscSync
	public static String toString(int i) {
		return toString(i, 10);
	}

	@JTranscSync
	public static String toUnsignedString(int i) {
		return toUnsignedString(i, 10);
	}

	@JTranscSync
	public static int parseInt(String input, int radix) {
		JTranscNumber.checkNumber(input, radix, false);
		return _parseInt(input, radix);
	}

	@JTranscMethodBody(target = "js", value = "return parseInt(N.istr(p0), p1);")
	@JTranscMethodBody(target = "as3", value = "return parseInt(N.istr(p0), p1);")
	@JTranscSync
	public static int _parseInt(String input, int radix) {
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

	@JTranscSync
	public static int parseInt(String s) {
		return parseInt(s, 10);
	}

	@JTranscSync
	public static int parseUnsignedInt(String s, int radix) {
		return parseInt(s, radix);
	}

	@JTranscSync
	public static int parseUnsignedInt(String s) {
		return parseUnsignedInt(s, 10);
	}

	@JTranscSync
	public static Integer valueOf(String s, int radix) {
		return parseInt(s, radix);
	}

	@JTranscSync
	public static Integer valueOf(String s) {
		return valueOf(s, 10);
	}

	@SuppressWarnings("UnnecessaryBoxing")
	@JTranscKeep
	@JTranscSync
	public static Integer valueOf(int i) {
		if (values == null) {
			values = new Integer[LENGTH];
			for (int n = MIN; n < MAX; n++) {
				values[n - MIN] = new Integer(n);
			}
		}
		if (i >= MIN && i < MAX) {
			return values[i - MIN];
		} else {
			return new Integer(i);
		}
	}

	@JTranscSync
	public byte byteValue() {
		return (byte) value;
	}

	@JTranscSync
	public short shortValue() {
		return (short) value;
	}

	@JTranscSync
	public int intValue() {
		return value;
	}

	@JTranscSync
	public long longValue() {
		return (long) value;
	}

	@JTranscSync
	public float floatValue() {
		return (float) value;
	}

	@JTranscSync
	public double doubleValue() {
		return (double) value;
	}

	@JTranscSync
	public String toString() {
		return toString(value);
	}

	@Override
	@JTranscSync
	public int hashCode() {
		return value;
	}

	@JTranscSync
	public static int hashCode(int value) {
		return value;
	}

	@JTranscSync
	public boolean equals(Object obj) {
		return obj instanceof Integer && ((Integer) obj).value == value;
	}

	@JTranscAsync
	public static Integer getInteger(String nm) {
		return getInteger(nm, null);
	}

	@JTranscAsync
	public static Integer getInteger(String nm, int val) {
		Integer result = getInteger(nm, null);
		return (result == null) ? new Integer(val) : result;
	}

	@JTranscAsync
	public static Integer getInteger(String nm, Integer val) {
		String out = System.getProperty(nm);
		if (out == null) return val;
		try {
			return decode(nm);
		} catch (NumberFormatException e) {
			return val;
		}
	}

	@JTranscSync
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

	@JTranscSync
	public int compareTo(Integer anotherInteger) {
		return Integer.compare(value, anotherInteger.value);
	}

	@JTranscMethodBody(target = "js", value = "return (p0 < p1) ? -1 : ((p0 > p1) ? 1 : 0);")
	@JTranscMethodBody(target = "cpp", value = "return (p0 < p1) ? -1 : ((p0 > p1) ? 1 : 0);")
	@JTranscMethodBody(target = "as3", value = "return (p0 < p1) ? -1 : ((p0 > p1) ? 1 : 0);")
	@JTranscSync
	public static int compare(int l, int r) {
		return (l < r) ? -1 : ((l > r) ? 1 : 0);
	}

	@JTranscSync
	public static int compareUnsigned(int l, int r) {
		return compare(l ^ MIN_VALUE, r ^ MIN_VALUE);
	}

	@JTranscSync
	public static long toUnsignedLong(int value) {
		return ((long) value) & 0xffffffffL;
	}

	@JTranscMethodBody(target = "cpp", value = "return (int32_t)((uint32_t)p0 / (uint32_t)p1);")
	@JTranscMethodBody(target = "as3", value = "return uint(p0) / uint(p1);")
	@JTranscSync
	public static int divideUnsigned(int dividend, int divisor) {
		//return (int) (toUnsignedLong(dividend) / toUnsignedLong(divisor));
		if (divisor < 0) return (compareUnsigned(dividend, divisor) < 0) ? 0 : 1;
		if (dividend >= 0) return dividend / divisor;
		int quotient = ((dividend >>> 1) / divisor) << 1;
		int rem = dividend - quotient * divisor;
		return quotient + (compareUnsigned(rem, divisor) >= 0 ? 1 : 0);
	}

	@JTranscMethodBody(target = "cpp", value = "return (int32_t)((uint32_t)p0 % (uint32_t)p1);")
	@JTranscMethodBody(target = "as3", value = "return uint(p0) % uint(p1);")
	@JTranscSync
	public static int remainderUnsigned(int dividend, int divisor) {
		//return (int) (toUnsignedLong(dividend) % toUnsignedLong(divisor));
		if (divisor < 0) return (compareUnsigned(dividend, divisor) < 0) ? dividend : (dividend - divisor);
		if (dividend >= 0) return dividend % divisor;
		int quotient = ((dividend >>> 1) / divisor) << 1;
		int rem = dividend - quotient * divisor;
		return rem - (compareUnsigned(rem, divisor) >= 0 ? divisor : 0);
	}

	@JTranscSync
	public static int highestOneBit(int i) {
		// Hacker's Delight, Figure 3-1
		i |= (i >> 1);
		i |= (i >> 2);
		i |= (i >> 4);
		i |= (i >> 8);
		i |= (i >> 16);
		return i - (i >>> 1);
	}

	@JTranscSync
	public static int lowestOneBit(int i) {
		return i & -i;
	}

	@JTranscMethodBody(target = "js", value = "return Math.clz32(p0);")
	@JTranscSync
	public static int numberOfLeadingZeros(int i) {
		// Hacker's Delight, Figure 5-6
		if (i <= 0) {
			return (~i >> 26) & 32;
		}
		int n = 1;
		if (i >> 16 == 0) {
			n += 16;
			i <<= 16;
		}
		if (i >> 24 == 0) {
			n += 8;
			i <<= 8;
		}
		if (i >> 28 == 0) {
			n += 4;
			i <<= 4;
		}
		if (i >> 30 == 0) {
			n += 2;
			i <<= 2;
		}
		return n - (i >>> 31);
	}

	@JTranscSync
	public static int numberOfTrailingZeros(int i) {
		return NTZ_TABLE[((i & -i) * 0x0450FBAF) >>> 26];
	}

	private static final byte[] NTZ_TABLE = {
		32, 0, 1, 12, 2, 6, -1, 13, 3, -1, 7, -1, -1, -1, -1, 14,
		10, 4, -1, -1, 8, -1, -1, 25, -1, -1, -1, -1, -1, 21, 27, 15,
		31, 11, 5, -1, -1, -1, -1, -1, 9, -1, -1, 24, -1, -1, 20, 26,
		30, -1, -1, -1, -1, 23, -1, 19, 29, -1, 22, 18, 28, 17, 16, -1
	};

	@JTranscSync
	public static int bitCount(int i) {
		// Hacker's Delight, Figure 5-2
		i -= (i >> 1) & 0x55555555;
		i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
		i = ((i >> 4) + i) & 0x0F0F0F0F;
		i += i >> 8;
		i += i >> 16;
		return i & 0x0000003F;
	}

	@JTranscSync
	public static int rotateLeft(int value, int distance) {
		return (value << distance) | (value >>> -distance);
	}

	@JTranscSync
	public static int rotateRight(int value, int distance) {
		return (value >>> distance) | (value << -distance);
	}

	@JTranscSync
	public static int reverse(int i) {
		// Hacker's Delight 7-1, with minor tweak from Veldmeijer
		// http://graphics.stanford.edu/~seander/bithacks.html
		i = ((i >>> 1) & 0x55555555) | ((i & 0x55555555) << 1);
		i = ((i >>> 2) & 0x33333333) | ((i & 0x33333333) << 2);
		i = ((i >>> 4) & 0x0F0F0F0F) | ((i & 0x0F0F0F0F) << 4);
		i = ((i >>> 8) & 0x00FF00FF) | ((i & 0x00FF00FF) << 8);
		return ((i >>> 16)) | ((i) << 16);
	}

	@JTranscSync
	public static int signum(int value) {
		return (value >> 31) | (-value >>> 31);
	}

	@HaxeMethodBody("return N.swap32(p0);")
	@JTranscMethodBody(target = "cpp", value = "return N::bswap32(p0);")
	@JTranscSync
	public static int reverseBytes(int value) {
		return ((value >>> 24)) | ((value >> 8) & 0xFF00) | ((value << 8) & 0xFF0000) | ((value << 24));
	}

	@JTranscSync
	public static int sum(int l, int r) {
		return l + r;
	}

	@JTranscSync
	public static int max(int l, int r) {
		return Math.max(l, r);
	}

	@JTranscSync
	public static int min(int l, int r) {
		return Math.min(l, r);
	}
}