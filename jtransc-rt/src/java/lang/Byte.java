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
import com.jtransc.annotation.JTranscSync;

@SuppressWarnings({"unchecked", "UnnecessaryBoxing", "WeakerAccess", "UnnecessaryUnboxing", "PointlessArithmeticExpression"})
public final class Byte extends Number implements Comparable<Byte> {
	public static final byte MIN_VALUE = -128;
	public static final byte MAX_VALUE = 127;
	public static final Class<Byte> TYPE = (Class<Byte>) Class.getPrimitiveClass("byte");

	public static String toString(byte value) {
		return Integer.toString((int) value, 10);
	}

	private static final Byte cache[] = new Byte[256];

	@JTranscKeep
	@JTranscSync
	public static Byte valueOf(byte value) {
		final int index = value + 128;
		if (cache[index] == null) cache[index] = new Byte(value);
		return cache[index];
	}

	public static byte parseByte(String value, int radix) throws NumberFormatException {
		return (byte) checkDecode(value, Integer.parseInt(value, radix));
	}

	public static byte parseByte(String value) throws NumberFormatException {
		return parseByte(value, 10);
	}

	public static Byte valueOf(String value, int radix) throws NumberFormatException {
		return valueOf(parseByte(value, radix));
	}

	public static Byte valueOf(String value) throws NumberFormatException {
		return valueOf(parseByte(value));
	}

	private static int checkDecode(String value, int decoded) throws NumberFormatException {
		if (decoded < MIN_VALUE || decoded > MAX_VALUE)
			throw new NumberFormatException("Value " + decoded + " out of range from input " + value);
		return decoded;
	}

	public static Byte decode(String value) throws NumberFormatException {
		return valueOf((byte) checkDecode(value, Integer.decode(value)));
	}

	private final byte value;

	@JTranscSync
	public Byte(byte value) {
		this.value = value;
	}

	public Byte(String value) throws NumberFormatException {
		this.value = parseByte(value, 10);
	}

	@JTranscSync
	public byte byteValue() {
		return value;
	}

	@JTranscSync
	public short shortValue() {
		return (short) value;
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

	public String toString() {
		return Integer.toString((int) value);
	}

	@Override
	public int hashCode() {
		return Byte.hashCode(value);
	}

	@JTranscSync
	public static int hashCode(byte value) {
		return (int) value;
	}

	public boolean equals(Object obj) {
		return obj instanceof Byte && value == ((Byte) obj).byteValue();
	}

	public int compareTo(Byte that) {
		return compare(this.value, that.value);
	}

	@JTranscSync
	public static int compare(byte l, byte r) {
		return l - r;
	}

	@JTranscSync
	public static int toUnsignedInt(byte value) {
		return ((int) value) & 0xff;
	}

	@JTranscSync
	public static long toUnsignedLong(byte value) {
		return ((long) value) & 0xffL;
	}

	// Bits
	public static final int SIZE = 8;
	public static final int BYTES = SIZE / Byte.SIZE;
}
