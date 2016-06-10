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
import com.jtransc.annotation.haxe.HaxeMethodBody;

public final class Math {
	private Math() {
	}

	public static final double E = 2.7182818284590452354;
	public static final double PI = 3.14159265358979323846;

	@JTranscInline
	@HaxeMethodBody("return Math.sin(p0);")
	native public static double sin(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.cos(p0);")
	native public static double cos(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.tan(p0);")
	native public static double tan(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.asin(p0);")
	native public static double asin(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.acos(p0);")
	native public static double acos(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.atan(p0);")
	native public static double atan(double a);

	@HaxeMethodBody("return p0 / 180.0 * Math.PI;")
	native public static double toRadians(double angdeg);

	@HaxeMethodBody("return p0 * 180.0 / Math.PI;")
	native public static double toDegrees(double angrad);

	@JTranscInline
	@HaxeMethodBody("return Math.exp(p0);")
	native public static double exp(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.log(p0);")
	native public static double log(double a);

	//@HaxeMethodBody("return Math.log10(p0);")
	native public static double log10(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.sqrt(p0);")
	@JTranscMethodBody(target = "js", value = "return Math.sqrt(p0);")
	native public static double sqrt(double a);

	static public double cbrt(double x) {
		double y = Math.pow(Math.abs(x), 1.0 / 3.0);
		return (x < 0) ? -y : y;
	}

	//@HaxeMethodBody("return Math.IEEEremainder(p0, p1);")
	native public static double IEEEremainder(double f1, double f2);

	@JTranscInline
	@HaxeMethodBody("return Math.ceil(p0);")
	native public static double ceil(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.floor(p0);")
	native public static double floor(double a);

	//@HaxeMethodBody("return Math.rint(p0);")
	public static double rint(double a) {
		double r = (double)(int)a;
		if (a < 0 && r == 0.0) return -0.0;
		return r;
	}

	@JTranscInline
	@HaxeMethodBody("return Math.atan2(p0, p1);")
	native public static double atan2(double y, double x);

	@JTranscInline
	@HaxeMethodBody("return Math.pow(p0, p1);")
	native public static double pow(double a, double b);

	@JTranscInline
	@HaxeMethodBody("return Math.round(p0);")
	native public static int round(float a);

	@JTranscInline
	@HaxeMethodBody("return Math.round(p0);")
	native public static long round(double a);

	@JTranscInline
	@HaxeMethodBody("return Math.random();")
	native public static double random();

	native public static int addExact(int x, int y);

	native public static long addExact(long x, long y);

	native public static int subtractExact(int x, int y);

	native public static long subtractExact(long x, long y);

	native public static int multiplyExact(int x, int y);

	native public static long multiplyExact(long x, long y);

	native public static int incrementExact(int a);

	native public static long incrementExact(long a);

	native public static int decrementExact(int a);

	native public static long decrementExact(long a);

	native public static int negateExact(int a);

	native public static long negateExact(long a);

	native public static int toIntExact(long value);

	native public static int floorDiv(int x, int y);

	native public static long floorDiv(long x, long y);

	native public static int floorMod(int x, int y);

	native public static long floorMod(long x, long y);

	// ABS

	@HaxeMethodBody("return (p0 >= 0) ? p0 : -p0;")
	native public static int abs(int a);

	@HaxeMethodBody("return (p0 >= 0) ? p0 : -p0;")
	native public static long abs(long a);

	@HaxeMethodBody("return (p0 >= 0) ? p0 : -p0;")
	native public static float abs(float a);

	@HaxeMethodBody("return (p0 >= 0) ? p0 : -p0;")
	native public static double abs(double a);

	// MAX

	@HaxeMethodBody("return (p0 > p1) ? p0 : p1;")
	native public static int max(int a, int b);

	@HaxeMethodBody("return (p0 > p1) ? p0 : p1;")
	native public static long max(long a, long b);

	@HaxeMethodBody("return (p0 > p1) ? p0 : p1;")
	native public static float max(float a, float b);

	@HaxeMethodBody("return (p0 > p1) ? p0 : p1;")
	native public static double max(double a, double b);

	// MIN

	@HaxeMethodBody("return (p0 < p1) ? p0 : p1;")
	native public static int min(int a, int b);

	@HaxeMethodBody("return (p0 < p1) ? p0 : p1;")
	native public static long min(long a, long b);

	@HaxeMethodBody("return (p0 < p1) ? p0 : p1;")
	native public static float min(float a, float b);

	@HaxeMethodBody("return (p0 < p1) ? p0 : p1;")
	native public static double min(double a, double b);

	native public static double ulp(double d);

	native public static float ulp(float f);

	@HaxeMethodBody("return (p0 == 0) ? 0 : ((p0 < 0) ? -1 : 1);")
	native public static double signum(double d);

	@HaxeMethodBody("return (p0 == 0) ? 0 : ((p0 < 0) ? -1 : 1);")
	native public static float signum(float f);

	//@HaxeMethodBody("return Math.sinh(p0);")
	native public static double sinh(double x);

	//@HaxeMethodBody("return Math.cosh(p0);")
	native public static double cosh(double x);

	//@HaxeMethodBody("return Math.tanh(p0);")
	native public static double tanh(double x);

	@HaxeMethodBody("return Math.sqrt((p0 * p0) + (p1 * p1));")
	native public static double hypot(double x, double y);

	native public static double expm1(double x);

	native public static double log1p(double x);

	native public static double copySign(double magnitude, double sign);

	native public static float copySign(float magnitude, float sign);

	native public static int getExponent(float f);

	native public static int getExponent(double d);

	native public static double nextAfter(double start, double direction);

	native public static float nextAfter(float start, double direction);

	native public static double nextUp(double d);

	native public static float nextUp(float f);

	native public static double nextDown(double d);

	native public static float nextDown(float f);

	//@HaxeMethodBody("return Math.scalab(p0, p1);")
	native public static double scalb(double d, int scaleFactor);

	//@HaxeMethodBody("return Math.scalab(p0, p1);")
	native public static float scalb(float f, int scaleFactor);

	//body("addExact", "*", "return p0 + p1;") // @TODO: Not like this! Check Overflows!
	//body("subtractExact", "*", "return p0 - p1;") // @TODO: Not like this! Check Overflows!
	//body("multiplyExact", "*", "return p0 * p1;") // @TODO: Not like this! Check Overflows!
	//body("incrementExact", "*", "return p0 + 1;") // @TODO: Not like this! Check Overflows!
	//body("decrementExact", "*", "return p0 - 1;") // @TODO: Not like this! Check Overflows!
	//body("negateExact", "*", "return -p0;") // @TODO: Not like this! Check Overflows!

	//fun toIntExact(value: Long): Int
	//fun floorDiv(x: Int, y: Int): Int
	//fun floorDiv(x: Long, y: Long): Long
	//fun floorMod(x: Int, y: Int): Int
	//fun floorMod(x: Long, y: Long): Long
	//fun ulp(d: Double): Double
	//fun ulp(f: Float): Float
	//fun expm1(x: Double): Double
	//fun log1p(x: Double): Double
	//fun copySign(magnitude: Double, sign: Double): Double
	//fun copySign(magnitude: Float, sign: Float): Float
	//fun getExponent(f: Float): Int
	//fun getExponent(d: Double): Int
	//fun nextAfter(start: Double, direction: Double): Double
	//fun nextAfter(start: Float, direction: Double): Float
	//fun nextUp(d: Double): Double
	//fun nextUp(f: Float): Float
	//fun nextDown(d: Double): Double
	//fun nextDown(f: Float): Float
}
