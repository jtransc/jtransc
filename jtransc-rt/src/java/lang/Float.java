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

public final class Float extends Number implements Comparable<Float> {
	public static final float POSITIVE_INFINITY = 1.0f / 0.0f;
	public static final float NEGATIVE_INFINITY = -1.0f / 0.0f;
	public static final float NaN = 0.0f / 0.0f;
	public static final float MAX_VALUE = 0x1.fffffeP+127f; // 3.4028235e+38f
	public static final float MIN_NORMAL = 0x1.0p-126f; // 1.17549435E-38f
	public static final float MIN_VALUE = 0x0.000002P-126f; // 1.4e-45f
	public static final int MAX_EXPONENT = 127;
	public static final int MIN_EXPONENT = -126;
	public static final int SIZE = 32;
	public static final int BYTES = SIZE / Byte.SIZE;
	public static final Class<Float> TYPE = (Class<Float>) Class.getPrimitiveClass("float");

	native public static String toString(float value);

	native public static String toHexString(float value);

	native public static Float valueOf(String value) throws NumberFormatException;

	@JTranscKeep
	public static Float valueOf(float value) {
		return new Float(value);
	}

	native public static float parseFloat(String value) throws NumberFormatException;

	native public static boolean isNaN(float value);

	native public static boolean isInfinite(float value);

	native public static boolean isFinite(float value);

	private final float value;

	public Float(float value) {
		this.value = value;
	}

	public Float(double value) {
		this.value = (float) value;
	}

	public Float(String s) throws NumberFormatException {
		value = parseFloat(s);
	}

	native public boolean isNaN();

	native public boolean isInfinite();

	native public String toString();

	native public byte byteValue();

	native public short shortValue();

	public int intValue() {
		return (int) value;
	}

	public long longValue() {
		return (long) value;
	}

	public float floatValue() {
		return value;
	}

	public double doubleValue() {
		return (double) value;
	}

	@Override
	public int hashCode() {
		return Float.hashCode(value);
	}

	public static int hashCode(float value) {
		return floatToIntBits(value);
	}

	public boolean equals(Object obj) {
		return (obj instanceof Float) && (floatToIntBits(((Float)obj).value) == floatToIntBits(value));
	}

	native public static int floatToIntBits(float value);

	public static native int floatToRawIntBits(float value);

	public static native float intBitsToFloat(int bits);

	native public int compareTo(Float that);

	native public static int compare(float l, float r);

	native public static float sum(float l, float r);

	native public static float max(float l, float r);

	native public static float min(float l, float r);
}
