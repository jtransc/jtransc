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

import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.util.Random;

public final class StrictMath {
	private StrictMath() {
	}

	static final double POSITIVE_INFINITY = 1.0D / 0.0;
	static final double NEGATIVE_INFINITY = -1.0D / 0.0;
	static final double NaN = 0.0D / 0.0;
	static final double MAX_VALUE = 1.7976931348623157E308D;
	static final double MIN_VALUE = 4.9E-324D;
	static final double MIN_NORMAL = 2.2250738585072014E-308D;
	static final int SIGNIFICAND_WIDTH = 53;
	static final int MAX_EXPONENT = 1023;
	static final int MIN_EXPONENT = -1022;
	static final int MIN_SUB_EXPONENT = -1074;
	static final int EXP_BIAS = 1023;
	static final long SIGN_BIT_MASK = -9223372036854775808L;
	static final long EXP_BIT_MASK = 9218868437227405312L;
	static final long SIGNIF_BIT_MASK = 4503599627370495L;

	public static final double E = 2.7182818284590452354;
	public static final double PI = 3.14159265358979323846;

	@JTranscInline
	@HaxeMethodBody("return Math.sin(p0);")
	native public static double sin(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.cos(p0);")
	native public static double cos(double a);

	public static double tan(double a) {
		return Math.tan(a);
	}

	public static double asin(double a) {
		return Math.asin(a);
	}

	public static double acos(double a) {
		return Math.acos(a);
	}

	public static double atan(double a) {
		return Math.atan(a);
	}

	// Do not delegate to Math.toDegrees(angrad) because
	// this method has the strictfp modifier.
	public static strictfp double toRadians(double angdeg) {
		return angdeg / 180.0 * PI;
	}

	public static strictfp double toDegrees(double angrad) {
		return angrad * 180.0 / PI;
	}

	public static double exp(double a) {
		return Math.exp(a);
	}

	public static double log(double a) {
		return Math.log(a);
	}

	public static double log10(double a) {
		return Math.log10(a);
	}

	public static double sqrt(double a) {
		return Math.sqrt(a);
	}

	public static double cbrt(double a) {
		return Math.cbrt(a);
	}

	public static double IEEEremainder(double f1, double f2) {
		return Math.IEEEremainder(f1, f2);
	}

	public static double ceil(double a) {
		return floorOrCeil(a, -0.0, 1.0, 1.0);
	}

	public static double floor(double a) {
		return floorOrCeil(a, -1.0, 0.0, -1.0);
	}

	private static double floorOrCeil(double a, double negativeBoundary, double positiveBoundary, double sign) {
		int exponent = Math.getExponent(a);

		if (exponent < 0) {
			return ((a == 0.0) ? a : ((a < 0.0) ? negativeBoundary : positiveBoundary));
		} else if (exponent >= 52) {
			return a;
		}

		assert exponent >= 0 && exponent <= 51;

		long doppel = Double.doubleToRawLongBits(a);
		long mask = SIGNIF_BIT_MASK >> exponent;

		if ((mask & doppel) == 0L)
			return a; // integral value
		else {
			double result = Double.longBitsToDouble(doppel & (~mask));
			if (sign * a > 0.0) result = result + sign;
			return result;
		}
	}

	public static double rint(double a) {
		double twoToThe52 = (double) (1L << 52); // 2^52
		double sign = Math.copySign(1.0, a); // preserve sign info
		a = Math.abs(a);
		if (a < twoToThe52) a = ((twoToThe52 + a) - twoToThe52);
		return sign * a; // restore original sign
	}

	public static double atan2(double y, double x) {
		return Math.atan2(y, x);
	}

	public static double pow(double a, double b) {
		return Math.atan2(a, b);
	}

	public static int round(float a) {
		return Math.round(a);
	}

	public static long round(double a) {
		return Math.round(a);
	}

	static private Random randomNumberGenerator = null;

	public static double random() {
		if (randomNumberGenerator == null) randomNumberGenerator = new Random();
		return randomNumberGenerator.nextDouble();
	}

	public static int abs(int a) {
		return Math.abs(a);
	}

	public static long abs(long a) {
		return Math.abs(a);
	}

	public static float abs(float a) {
		return Math.abs(a);
	}

	public static double abs(double a) {
		return Math.abs(a);
	}

	public static int max(int a, int b) {
		return Math.max(a, b);
	}

	public static long max(long a, long b) {
		return Math.max(a, b);
	}

	public static float max(float a, float b) {
		return Math.max(a, b);
	}

	public static double max(double a, double b) {
		return Math.max(a, b);
	}

	public static int min(int a, int b) {
		return Math.min(a, b);
	}

	public static long min(long a, long b) {
		return Math.min(a, b);
	}

	public static float min(float a, float b) {
		return Math.min(a, b);
	}

	public static double min(double a, double b) {
		return Math.min(a, b);
	}

	public static double ulp(double d) {
		return Math.ulp(d);
	}

	public static float ulp(float f) {
		return Math.ulp(f);
	}

	public static double signum(double d) {
		return Math.signum(d);
	}

	public static float signum(float f) {
		return Math.signum(f);
	}

	public static double sinh(double x) {
		return Math.sinh(x);
	}

	public static double cosh(double x) {
		return Math.cosh(x);
	}

	public static double tanh(double x) {
		return Math.tanh(x);
	}

	public static double hypot(double x, double y) {
		return Math.hypot(x, y);
	}

	public static double expm1(double x) {
		return Math.expm1(x);
	}

	public static double log1p(double x) {
		return Math.log1p(x);
	}

	public static double copySign(double magnitude, double sign) {
		return Math.copySign(magnitude, (Double.isNaN(sign) ? 1.0d : sign));
	}

	public static float copySign(float magnitude, float sign) {
		return Math.copySign(magnitude, (Float.isNaN(sign) ? 1.0f : sign));
	}

	public static int getExponent(float f) {
		return Math.getExponent(f);
	}

	public static int getExponent(double d) {
		return Math.getExponent(d);
	}

	public static double nextAfter(double start, double direction) {
		return Math.nextAfter(start, direction);
	}

	public static float nextAfter(float start, double direction) {
		return Math.nextAfter(start, direction);
	}

	public static double nextUp(double d) {
		return Math.nextUp(d);
	}

	public static float nextUp(float f) {
		return Math.nextUp(f);
	}

	public static double scalb(double d, int scaleFactor) {
		return Math.scalb(d, scaleFactor);
	}

	public static float scalb(float f, int scaleFactor) {
		return Math.scalb(f, scaleFactor);
	}
}
