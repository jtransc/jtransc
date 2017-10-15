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
import com.jtransc.annotation.JTranscSync;
import com.jtransc.annotation.haxe.HaxeMethodBody;

@SuppressWarnings({"UnnecessaryUnboxing", "unchecked", "WeakerAccess", "unused", "UnnecessaryBoxing"})
public final class Short extends Number implements Comparable<Short> {
	public static final int SIZE = 16;
	public static final int BYTES = SIZE / Byte.SIZE;

	public static final short MIN_VALUE = -32768;
	public static final short MAX_VALUE = 32767;
	public static final Class<Short> TYPE = (Class<Short>) Class.getPrimitiveClass("short");

	private final short value;

	@JTranscSync
	public static String toString(short s) {
		return Integer.toString((int) s, 10);
	}

	@JTranscSync
	private static int checkDecode(String value, int decoded) throws NumberFormatException {
		if (decoded < MIN_VALUE || decoded > MAX_VALUE)
			throw new NumberFormatException("Value " + decoded + " out of range from input " + value);
		return decoded;
	}

	@JTranscSync
	public static short parseShort(String s, int radix) throws NumberFormatException {
		return (short) checkDecode(s, Integer.parseInt(s, radix));
	}

	@JTranscSync
	public static short parseShort(String s) throws NumberFormatException {
		return parseShort(s, 10);
	}

	@JTranscSync
	public static Short valueOf(String s, int radix) throws NumberFormatException {
		return valueOf(parseShort(s, radix));
	}

	@JTranscSync
	public static Short valueOf(String s) throws NumberFormatException {
		return valueOf(parseShort(s, 10));
	}

	@JTranscKeep
	@JTranscSync
	public static Short valueOf(short s) {
		// @TODO: Cache!
		return new Short(s);
	}

	@JTranscSync
	public static Short decode(String nm) throws NumberFormatException {
		return valueOf((byte) checkDecode(nm, Integer.decode(nm)));
	}

	@JTranscSync
	public Short(short value) {
		this.value = value;
	}

	@JTranscSync
	public Short(String s) throws NumberFormatException {
		this.value = parseShort(s, 10);
	}

	@JTranscSync
	public byte byteValue() {
		return (byte) value;
	}

	@JTranscSync
	public short shortValue() {
		return value;
	}

	@JTranscSync
	public int intValue() {
		return (int) value;
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
		return Integer.toString((int) value);
	}

	@Override
	@JTranscSync
	public int hashCode() {
		return Short.hashCode(value);
	}

	@JTranscSync
	public static int hashCode(short value) {
		return (int) value;
	}

	@JTranscSync
	public boolean equals(Object obj) {
		return obj instanceof Short && value == ((Short) obj).shortValue();
	}

	@JTranscSync
	public int compareTo(Short anotherShort) {
		return compare(this.value, anotherShort.value);
	}

	@JTranscSync
	public static int compare(short x, short y) {
		return x - y;
	}

	@HaxeMethodBody("return N.swap16(p0);")
	@JTranscMethodBody(target = "cpp", value = "return N::bswap16(p0);")
	@JTranscSync
	public static short reverseBytes(short value) {
		return (short) (((value & 0xFF00) >> 8) | ((value & 0xFF) << 8));
	}

	@JTranscSync
	public static int toUnsignedInt(short value) {
		return ((int) value) & 0xffff;
	}

	@JTranscSync
	public static long toUnsignedLong(short value) {
		return ((long) value) & 0xffffL;
	}
}
