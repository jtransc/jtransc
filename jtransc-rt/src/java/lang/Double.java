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
import com.jtransc.text.JTranscStringTools;

@SuppressWarnings({"NumericOverflow", "unchecked", "UnnecessaryBoxing", "ConstantConditions", "UnnecessaryUnboxing", "unused"})
public final class Double extends Number implements Comparable<Double> {
	public static final double POSITIVE_INFINITY = 1.0 / 0.0;
	public static final double NEGATIVE_INFINITY = -1.0 / 0.0;
	public static final double NaN = 0.0d / 0.0;
	public static final double MAX_VALUE = 0x1.fffffffffffffP+1023; // 1.7976931348623157e+308
	public static final double MIN_NORMAL = 0x1.0p-1022; // 2.2250738585072014E-308
	public static final double MIN_VALUE = 0x0.0000000000001P-1022; // 4.9e-324
	public static final int MAX_EXPONENT = 1023;
	public static final int MIN_EXPONENT = -1022;
	public static final int SIZE = 64;
	public static final int BYTES = SIZE / Byte.SIZE;

	public static final Class<Double> TYPE = (Class<Double>) Class.getPrimitiveClass("double");

	public static String toString(double d) {
		return JTranscStringTools.toString(d);
	}

	native public static String toHexString(double d);

	public static Double valueOf(String s) {
		return valueOf(parseDouble(s));
	}

	@JTranscKeep
	public static Double valueOf(double d) {
		return new Double(d);
	}

	@HaxeMethodBody("return Std.parseFloat(p0._str);")
	@JTranscMethodBody(target = "js", value = "return parseFloat(N.istr(p0));")
	@JTranscMethodBody(target = "cpp", value = "char temp[256] = {0}; N::writeChars(p0, temp, sizeof(temp)); return ::atof(temp);")
	@JTranscMethodBody(target = "d", value = "return to!double(N.istr(p0));")
	native private static double _parseDouble(String value);

	public static double parseDouble(String value) {
		JTranscNumber.checkNumber(value, 10, true);
		return _parseDouble(value);
	}

	@HaxeMethodBody("return Math.isNaN(p0);")
	@JTranscMethodBody(target = "js", value = "return isNaN(p0);")
	@JTranscMethodBody(target = "cpp", value = "return std::isnan(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.isNaN(p0);")
	native public static boolean isNaN(double v);

	@HaxeMethodBody("return Math.isFinite(p0);")
	@JTranscMethodBody(target = "js", value = "return isFinite(p0);")
	@JTranscMethodBody(target = "cpp", value = "return std::isfinite(p0);")
	@JTranscMethodBody(target = "d", value = "return to!bool(std.math.isFinite(p0));")
	native private static boolean _isFinite(double v);

	public static boolean isInfinite(double v) {
		return !isNaN(v) && !_isFinite(v);
	}

	public static boolean isFinite(double d) {
		return _isFinite(d);
	}

	private final double value;

	public Double(double value) {
		this.value = value;
	}

	public Double(String s) {
		value = parseDouble(s);
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
		return (byte) this.value;
	}

	public short shortValue() {
		return (short) this.value;
	}

	public int intValue() {
		return (int) this.value;
	}

	public long longValue() {
		return (long) this.value;
	}

	public float floatValue() {
		return (float) this.value;
	}

	public double doubleValue() {
		return this.value;
	}

	@Override
	public int hashCode() {
		return hashCode(doubleValue());
	}

	public static int hashCode(double value) {
		return (int) doubleToLongBits(value);
	}

	public boolean equals(Object obj) {
		return (obj instanceof Double) && (doubleToLongBits(((Float) obj).doubleValue()) == doubleToLongBits(this.doubleValue()));
	}

	@HaxeMethodBody("return N.doubleToLongBits(p0);")
	@JTranscMethodBody(target = "js", value = "return N.doubleToLongBits(p0);")
	@JTranscMethodBody(target = "cpp", value = "return *(int64_t *)&p0;")
	@JTranscMethodBody(target = "d", value = "return *cast(long *)&p0;")
	native public static long doubleToLongBits(double value);

	@HaxeMethodBody("return N.doubleToLongBits(p0);")
	@JTranscMethodBody(target = "js", value = "return N.doubleToLongBits(p0);")
	@JTranscMethodBody(target = "cpp", value = "return *(int64_t *)&p0;")
	@JTranscMethodBody(target = "d", value = "return *cast(long *)&p0;")
	native public static long doubleToRawLongBits(double value);

	@HaxeMethodBody("return N.longBitsToDouble(p0);")
	@JTranscMethodBody(target = "js", value = "return N.longBitsToDouble(p0);")
	@JTranscMethodBody(target = "cpp", value = "return *(double *)&p0;")
	@JTranscMethodBody(target = "d", value = "return *cast(double *)&p0;")
	public static native double longBitsToDouble(long bits);

	public int compareTo(Double that) {
		return Double.compare(this.value, that.value);
	}

	public static int compare(double d1, double d2) {
		if (d1 < d2) return -1;
		if (d1 > d2) return 1;
		long b1 = Double.doubleToLongBits(d1);
		long b2 = Double.doubleToLongBits(d2);
		return (b1 == b2 ? 0 : (b1 < b2 ? -1 : 1));
	}

	public static double sum(double a, double b) {
		return a + b;
	}

	public static double max(double a, double b) {
		return Math.max(a, b);
	}

	public static double min(double a, double b) {
		return Math.min(a, b);
	}
}
