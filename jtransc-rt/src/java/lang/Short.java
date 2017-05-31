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
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;

@SuppressWarnings({"UnnecessaryUnboxing", "unchecked", "WeakerAccess", "unused", "UnnecessaryBoxing"})
public final class Short extends Number implements Comparable<Short> {
	public static final int SIZE = 16;
	public static final int BYTES = SIZE / Byte.SIZE;

	public static final short MIN_VALUE = -32768;
	public static final short MAX_VALUE = 32767;
	public static final Class<Short> TYPE = (Class<Short>) Class.getPrimitiveClass("short");

	public static String toString(short s) {
		return Integer.toString((int) s, 10);
	}

	private static int checkDecode(String value, int decoded) throws NumberFormatException {
		if (decoded < MIN_VALUE || decoded > MAX_VALUE)
			throw new NumberFormatException("Value " + decoded + " out of range from input " + value);
		return decoded;
	}

	public static short parseShort(String s, int radix) throws NumberFormatException {
		return (short) checkDecode(s, Integer.parseInt(s, radix));
	}

	public static short parseShort(String s) throws NumberFormatException {
		return parseShort(s, 10);
	}

	public static Short valueOf(String s, int radix) throws NumberFormatException {
		return valueOf(parseShort(s, radix));
	}

	public static Short valueOf(String s) throws NumberFormatException {
		return valueOf(parseShort(s, 10));
	}

	@JTranscKeep
	public static Short valueOf(short s) {
		// @TODO: Cache!
		return new Short(s);
	}

	public static Short decode(String nm) throws NumberFormatException {
		return valueOf((byte) checkDecode(nm, Integer.decode(nm)));
	}

	private final short value;

	public Short(short value) {
		this.value = value;
	}

	public Short(String s) throws NumberFormatException {
		this.value = parseShort(s, 10);
	}

	public byte byteValue() {
		return (byte) value;
	}

	public short shortValue() {
		return value;
	}

	public int intValue() {
		return (int) value;
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
		return Integer.toString((int) value);
	}

	@Override
	public int hashCode() {
		return Short.hashCode(value);
	}

	public static int hashCode(short value) {
		return (int) value;
	}

	public boolean equals(Object obj) {
		return obj instanceof Short && value == ((Short) obj).shortValue();
	}

	public int compareTo(Short anotherShort) {
		return compare(this.value, anotherShort.value);
	}

	public static int compare(short x, short y) {
		return x - y;
	}

	@HaxeMethodBody("return N.swap16(p0);")
	@JTranscMethodBody(target = "cpp", value = "return N::bswap16(p0);")
	public static short reverseBytes(short value) {
		return (short) (((value & 0xFF00) >> 8) | ((value & 0xFF) << 8));
	}

	public static int toUnsignedInt(short value) {
		return ((int) value) & 0xffff;
	}

	public static long toUnsignedLong(short value) {
		return ((long) value) & 0xffffL;
	}
}
