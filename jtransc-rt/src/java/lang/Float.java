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

	@HaxeMethodBody("return Std.parseFloat(p0._str);")
	native public static float parseFloat(String value) throws NumberFormatException;

	public static String toString(float value) {
		return Double.toString((double) value);
	}

	// @TODO: CHECK!
	public static String toHexString(float value) {
		return Double.toHexString(value);
	}

	public static Float valueOf(String value) throws NumberFormatException {
		return valueOf(parseFloat(value));
	}

	@JTranscKeep
	public static Float valueOf(float value) {
		return new Float(value);
	}

	@HaxeMethodBody("return Math.isNaN(p0);")
	native public static boolean isNaN(float value);

	@HaxeMethodBody("return Math.isFinite(p0);")
	native private static boolean _isFinite(float v);

	public static boolean isInfinite(float v) {
		return _isFinite(v);
	}

	public static boolean isFinite(float d) {
		return !_isFinite(d);
	}

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

	public boolean isNaN() {
		return isNaN(value);
	}

	public boolean isInfinite() {
		return isInfinite(value);
	}

	public String toString() {
		return toString(value);
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
		return (obj instanceof Float) && (floatToIntBits(((Float) obj).value) == floatToIntBits(value));
	}

	@HaxeMethodBody("return HaxeNatives.floatToIntBits(p0);")
	native public static int floatToIntBits(float value);

	@HaxeMethodBody("return HaxeNatives.floatToIntBits(p0);")
	native public static int floatToRawIntBits(float value);

	@HaxeMethodBody("return HaxeNatives.intBitsToFloat(p0);")
	native public static float intBitsToFloat(int bits);

	public int compareTo(Float that) {
		return compare(this.value, that.value);
	}

	strictfp public static int compare(float f1, float f2) {
		if (f1 < f2) return -1;
		if (f1 > f2) return 1;
		int b1 = Float.floatToIntBits(f1);
		int b2 = Float.floatToIntBits(f2);
		return (b1 == b2 ? 0 : (b1 < b2 ? -1 : 1));
	}

	public static float sum(float l, float r) {
		return l + r;
	}

	public static float max(float l, float r) {
		return Math.max(l, r);
	}

	public static float min(float l, float r) {
		return Math.min(l, r);
	}
}
