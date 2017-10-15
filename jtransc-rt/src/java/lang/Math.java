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
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscSync;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.lang.DoubleInfo;
import com.jtransc.lang.FloatInfo;

import java.util.Random;

@SuppressWarnings("WeakerAccess")
public final class Math {
	@JTranscSync
	private Math() {
	}

	public static final double E = 2.7182818284590452354;
	public static final double PI = 3.14159265358979323846;

	@JTranscInline
	@HaxeMethodBody("return Math.sin(p0);")
	@JTranscMethodBody(target = "js", value = "return Math.sin(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::sin(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.sin(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Sin(p0);")
	@JTranscMethodBody(target = "as3", value = "return Math.sin(p0);")
	@JTranscMethodBody(target = "dart", value = "return Math.sin(p0);")
	@JTranscMethodBody(target = "php", value = "return sin($p0);")
	@JTranscSync
	native public static double sin(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.cos(p0);")
	@JTranscMethodBody(target = "js", value = "return Math.cos(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::cos(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.cos(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Cos(p0);")
	@JTranscMethodBody(target = "as3", value = "return Math.cos(p0);")
	@JTranscMethodBody(target = "dart", value = "return Math.cos(p0);")
	@JTranscMethodBody(target = "php", value = "return cos($p0);")
	@JTranscSync
	native public static double cos(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.tan(p0);")
	@JTranscMethodBody(target = "js", value = "return Math.tan(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::tan(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.tan(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Tan(p0);")
	@JTranscMethodBody(target = "as3", value = "return Math.tan(p0);")
	@JTranscMethodBody(target = "dart", value = "return Math.tan(p0);")
	@JTranscMethodBody(target = "php", value = "return tan($p0);")
	@JTranscSync
	public static double tan(double a) {
		return Math.sin(a) / Math.cos(a);
	}

	@JTranscInline
	@HaxeMethodBody("return Math.asin(p0);")
	@JTranscMethodBody(target = "js", value = "return Math.asin(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::asin(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.asin(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Asin(p0);")
	@JTranscMethodBody(target = "as3", value = "return Math.asin(p0);")
	@JTranscMethodBody(target = "dart", value = "return Math.asin(p0);")
	@JTranscMethodBody(target = "php", value = "return asin($p0);")
	@JTranscSync
	native public static double asin(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.acos(p0);")
	@JTranscMethodBody(target = "js", value = "return Math.acos(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::acos(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.acos(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Acos(p0);")
	@JTranscMethodBody(target = "as3", value = "return Math.acos(p0);")
	@JTranscMethodBody(target = "dart", value = "return Math.acos(p0);")
	@JTranscMethodBody(target = "php", value = "return acos($p0);")
	@JTranscSync
	native public static double acos(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.atan(p0);")
	@JTranscMethodBody(target = "js", value = "return Math.atan(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::atan(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.atan(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Atan(p0);")
	@JTranscMethodBody(target = "as3", value = "return Math.atan(p0);")
	@JTranscMethodBody(target = "dart", value = "return Math.atan(p0);")
	@JTranscMethodBody(target = "php", value = "return atan($p0);")
	@JTranscSync
	native public static double atan(double a);

	@JTranscSync
	public static double toRadians(double angdeg) {
		return angdeg / 180.0 * Math.PI;
	}

	@JTranscSync
	public static double toDegrees(double angrad) {
		return angrad * 180.0 / Math.PI;
	}

	@JTranscInline
	@HaxeMethodBody("return Math.exp(p0);")
	@JTranscMethodBody(target = "js", value = "return Math.exp(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::exp(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.exp(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Exp(p0);")
	@JTranscMethodBody(target = "as3", value = "return Math.exp(p0);")
	@JTranscMethodBody(target = "php", value = "return exp($p0);")
	@JTranscSync
	public static double exp(double a) {
		return Math.pow(a, Math.E);
	}

	@JTranscInline
	@HaxeMethodBody("return Math.log(p0);")
	@JTranscMethodBody(target = "js", value = "return Math.log(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::log(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.log(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Log(p0);")
	@JTranscMethodBody(target = "as3", value = "return Math.log(p0);")
	@JTranscMethodBody(target = "dart", value = "return Math.log(p0);")
	@JTranscMethodBody(target = "php", value = "return log($p0);")
	@JTranscSync
	native public static double log(double a);

	@HaxeMethodBody("return Math.log(p0) / Math.log(10);")
	@JTranscMethodBody(target = "js", value = "return Math.log10(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::log10(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.log10(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Log10(p0);")
	@JTranscMethodBody(target = "php", value = "return log10($p0);")
	@JTranscSync
	public static double log10(double a) {
		return log(a) / log(10.0);
	}

	@JTranscInline
	@HaxeMethodBody("return Math.sqrt(p0);")
	@JTranscMethodBody(target = "js", value = "return Math.sqrt(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::sqrt(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.sqrt(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Sqrt(p0);")
	@JTranscMethodBody(target = "as3", value = "return Math.sqrt(p0);")
	@JTranscMethodBody(target = "dart", value = "return Math.sqrt(p0);")
	@JTranscMethodBody(target = "php", value = "return sqrt($p0);")
	@JTranscSync
	public static double sqrt(double a) {
		return Math.pow(a, 0.5);
	}

	@JTranscSync
	static public double cbrt(double x) {
		double y = Math.pow(Math.abs(x), 1.0 / 3.0);
		return (x < 0) ? -y : y;
	}

	//@HaxeMethodBody("return Math.IEEEremainder(p0, p1);")
	@JTranscSync
	native public static double IEEEremainder(double f1, double f2);

	@JTranscInline
	@HaxeMethodBody("return Math.ceil(p0);")
	@JTranscMethodBody(target = "js", value = "return Math.ceil(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::ceil(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.ceil(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Ceiling(p0);")
	@JTranscMethodBody(target = "as3", value = "return Math.ceil(p0);")
	@JTranscMethodBody(target = "dart", value = "return (p0).ceil();")
	@JTranscMethodBody(target = "php", value = "return ceil($p0);")
	@JTranscSync
	native public static double ceil(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.floor(p0);")
	@JTranscMethodBody(target = "js", value = "return Math.floor(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::floor(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.floor(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Floor(p0);")
	@JTranscMethodBody(target = "as3", value = "return Math.floor(p0);")
	@JTranscMethodBody(target = "dart", value = "return (p0).floor();")
	@JTranscMethodBody(target = "php", value = "return floor($p0);")
	@JTranscSync
	native public static double floor(double a);

	//@HaxeMethodBody("return Math.rint(p0);")
	@JTranscSync
	public static double rint(double a) {
		double r = (double) (int) a;
		if (a < 0 && r == 0.0) return -0.0;
		return r;
	}

	@JTranscInline
	@HaxeMethodBody("return Math.atan2(p0, p1);")
	@JTranscMethodBody(target = "js", value = "return Math.atan2(p0, p1);")
	@JTranscMethodBody(target = "cpp", value = "return ::atan2(p0, p1);")
	@JTranscMethodBody(target = "d", value = "return std.math.atan2(p0, p1);")
	@JTranscMethodBody(target = "cs", value = "return Math.Atan2(p0, p1);")
	@JTranscMethodBody(target = "as3", value = "return Math.atan2(p0, p1);")
	@JTranscMethodBody(target = "dart", value = "return Math.atan2(p0, p1);")
	@JTranscMethodBody(target = "php", value = "return atan2($p0, $p1);")
	@JTranscSync
	native public static double atan2(double y, double x);

	@JTranscInline
	@HaxeMethodBody("return Math.pow(p0, p1);")
	@JTranscMethodBody(target = "js", value = "return Math.pow(p0, p1);")
	@JTranscMethodBody(target = "cpp", value = "return ::pow(p0, p1);")
	@JTranscMethodBody(target = "d", value = "return std.math.pow(p0, p1);")
	@JTranscMethodBody(target = "cs", value = "return Math.Pow(p0, p1);")
	@JTranscMethodBody(target = "as3", value = "return Math.pow(p0, p1);")
	@JTranscMethodBody(target = "dart", value = "return Math.pow(p0, p1);")
	@JTranscMethodBody(target = "php", value = "return pow($p0, $p1);")
	@JTranscSync
	native public static double pow(double a, double b);

	@JTranscInline
	@HaxeMethodBody("return Math.round(p0);")
	@JTranscMethodBody(target = "js", value = "return Math.round(p0);")
	@JTranscMethodBody(target = "cpp", value = "return (int32_t)::lround(p0);")
	@JTranscMethodBody(target = "d", value = "return cast(int)std.math.round(p0);")
	@JTranscMethodBody(target = "cs", value = "return (int)Math.Round(p0);")
	@JTranscMethodBody(target = "as3", value = "return int(Math.round(p0));")
	@JTranscMethodBody(target = "dart", value = "return p0.round();")
	@JTranscMethodBody(target = "php", value = "return (int)round($p0);")
	@JTranscSync
	native public static int round(float a);

	@JTranscInline
	@HaxeMethodBody("return Math.round(p0);")
	@JTranscMethodBody(target = "js", value = "return N.lnewFloat(Math.round(p0));")
	@JTranscMethodBody(target = "cpp", value = "return (int64_t)::llround(p0);")
	@JTranscMethodBody(target = "d", value = "return cast(long)std.math.round(p0);")
	@JTranscMethodBody(target = "cs", value = "return (long)Math.Round(p0);")
	@JTranscMethodBody(target = "as3", value = "return N.lnewFloat(Math.round(p0));")
	@JTranscMethodBody(target = "dart", value = "return p0.round();")
	@JTranscMethodBody(target = "php", value = "return N::d2j($p0);")
	@JTranscSync
	native public static long round(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.random();")
	@JTranscMethodBody(target = "js", value = "return Math.random();")
	@JTranscMethodBody(target = "d", value = "return std.random.uniform(0.0, 1.0);")
	@JTranscMethodBody(target = "as3", value = "return Math.random();")
	@JTranscMethodBody(target = "php", value = "return mt_rand() / mt_getrandmax();")
	@JTranscSync
	public static double random() {
		if (random == null) random = new Random();
		return random.nextDouble();
	}

	static private Random random;

	@JTranscSync
	native public static int addExact(int x, int y);

	@JTranscSync
	native public static long addExact(long x, long y);

	@JTranscSync
	native public static int subtractExact(int x, int y);

	@JTranscSync
	native public static long subtractExact(long x, long y);

	@JTranscSync
	native public static int multiplyExact(int x, int y);

	@JTranscSync
	native public static long multiplyExact(long x, long y);

	@JTranscSync
	native public static int incrementExact(int a);

	@JTranscSync
	native public static long incrementExact(long a);

	@JTranscSync
	native public static int decrementExact(int a);

	@JTranscSync
	native public static long decrementExact(long a);

	@JTranscSync
	native public static int negateExact(int a);

	@JTranscSync
	native public static long negateExact(long a);

	@JTranscSync
	native public static int toIntExact(long value);

	@JTranscSync
	native public static int floorDiv(int x, int y);

	@JTranscSync
	native public static long floorDiv(long x, long y);

	@JTranscSync
	native public static int floorMod(int x, int y);

	@JTranscSync
	native public static long floorMod(long x, long y);

	// ABS

	@JTranscSync
	public static int abs(int a) {
		return (a >= 0) ? a : -a;
	}

	@JTranscSync
	public static long abs(long a) {
		return (a >= 0) ? a : -a;
	}

	@JTranscSync
	public static float abs(float a) {
		return (a >= 0) ? a : -a;
	}

	@JTranscSync
	public static double abs(double a) {
		return (a >= 0) ? a : -a;
	}

	// MAX

	@JTranscSync
	public static int max(int a, int b) {
		return (a > b) ? a : b;
	}

	@JTranscSync
	public static long max(long a, long b) {
		return (a > b) ? a : b;
	}

	@JTranscSync
	public static float max(float a, float b) {
		return (a > b) ? a : b;
	}

	@JTranscSync
	public static double max(double a, double b) {
		return (a > b) ? a : b;
	}

	// MIN

	@JTranscSync
	public static int min(int a, int b) {
		return (a < b) ? a : b;
	}

	@JTranscSync
	public static long min(long a, long b) {
		return (a < b) ? a : b;
	}

	@JTranscSync
	public static float min(float a, float b) {
		return (a < b) ? a : b;
	}

	@JTranscSync
	public static double min(double a, double b) {
		return (a < b) ? a : b;
	}

	@JTranscSync
	native public static double ulp(double d);

	@JTranscSync
	native public static float ulp(float f);

	@HaxeMethodBody("return (p0 == 0) ? 0 : ((p0 < 0) ? -1 : 1);")
	@JTranscMethodBody(target = "js", value = "return Math.sign(p0);")
	@JTranscMethodBody(target = "cs", value = "return (double)Math.Sign(p0);")
	@JTranscSync
	public static double signum(double v) {
		if (v < 0) return -1;
		if (v > 0) return +1;
		return 0;
	}

	@HaxeMethodBody("return (p0 == 0) ? 0 : ((p0 < 0) ? -1 : 1);")
	@JTranscMethodBody(target = "js", value = "return Math.sign(p0);")
	@JTranscMethodBody(target = "cs", value = "return (float)Math.Sign(p0);")
	@JTranscSync
	public static float signum(float v) {
		if (v < 0) return -1;
		if (v > 0) return +1;
		return 0;
	}

	//@HaxeMethodBody("return Math.sinh(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::sinh(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.sinh(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Sinh(p0);")
	@JTranscSync
	public static double sinh(double x) {
		return (Math.pow(Math.E, x) - Math.pow(Math.E, -x)) / 2.0;
	}

	//@HaxeMethodBody("return Math.cosh(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::cosh(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.cosh(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Cosh(p0);")
	@JTranscSync
	public static double cosh(double x) {
		return (Math.pow(Math.E, x) + Math.pow(Math.E, -x)) / 2.0;
	}

	//@HaxeMethodBody("return Math.tanh(p0);")
	@JTranscMethodBody(target = "cpp", value = "return ::tanh(p0);")
	@JTranscMethodBody(target = "d", value = "return std.math.tanh(p0);")
	@JTranscMethodBody(target = "cs", value = "return Math.Tanh(p0);")
	@JTranscSync
	public static double tanh(double x) {
		return sinh(x) / cosh(x);
	}

	@HaxeMethodBody("return Math.sqrt((p0 * p0) + (p1 * p1));")
	@JTranscMethodBody(target = "js", value = "return Math.hypot(p0, p1);")
	@JTranscMethodBody(target = "cpp", value = "return ::hypot(p0, p1);")
	@JTranscMethodBody(target = "d", value = "return std.math.hypot(p0, p1);")
	@JTranscSync
	public static double hypot(double x, double y) {
		return sqrt((x * x) + (y * y));
	}

	@JTranscSync
	public static double expm1(double x) {
		return pow(E, x) - 1;
	}

	@JTranscSync
	native public static double log1p(double x);

	@JTranscSync
	public static double copySign(double magnitude, double sign) {
		long magnitudeBits = Double.doubleToRawLongBits(magnitude);
		long signBits = Double.doubleToRawLongBits(sign);
		magnitudeBits = (magnitudeBits & ~DoubleInfo.SIGN_MASK) | (signBits & DoubleInfo.SIGN_MASK);
		return Double.longBitsToDouble(magnitudeBits);
	}

	@JTranscSync
	public static float copySign(float magnitude, float sign) {
		int magnitudeBits = Float.floatToRawIntBits(magnitude);
		int signBits = Float.floatToRawIntBits(sign);
		magnitudeBits = (magnitudeBits & ~FloatInfo.SIGN_MASK) | (signBits & FloatInfo.SIGN_MASK);
		return Float.intBitsToFloat(magnitudeBits);
	}

	@JTranscSync
	native public static int getExponent(float f);

	@JTranscSync
	native public static int getExponent(double d);

	@JTranscSync
	native public static double nextAfter(double start, double direction);

	@JTranscSync
	native public static float nextAfter(float start, double direction);

	@JTranscSync
	native public static double nextUp(double d);

	@JTranscSync
	native public static float nextUp(float f);

	@JTranscSync
	native public static double nextDown(double d);

	@JTranscSync
	native public static float nextDown(float f);

	//@HaxeMethodBody("return Math.scalab(p0, p1);")
	@JTranscSync
	public static double scalb(double d, int scaleFactor) {
		return d * Math.pow(2, scaleFactor);
	}

	//@HaxeMethodBody("return Math.scalab(p0, p1);")
	@JTranscSync
	public static float scalb(float f, int scaleFactor) {
		return (float) (f * Math.pow(2, scaleFactor));
	}
}
