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

public final class Math {
    private Math() {
    }

    public static final double E = 2.7182818284590452354;
    public static final double PI = 3.14159265358979323846;

    native public static double sin(double a);

    native public static double cos(double a);

    native public static double tan(double a);

    native public static double asin(double a);

    native public static double acos(double a);

    native public static double atan(double a);

    native public static double toRadians(double angdeg);

    native public static double toDegrees(double angrad);

    native public static double exp(double a);

    native public static double log(double a);

    native public static double log10(double a);

    native public static double sqrt(double a);

    native public static double cbrt(double a);

    native public static double IEEEremainder(double f1, double f2);

    native public static double ceil(double a);

    native public static double floor(double a);

    native public static double rint(double a);

    native public static double atan2(double y, double x);

    native public static double pow(double a, double b);

    native public static int round(float a);

    native public static long round(double a);

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

    native public static int abs(int a);

    native public static long abs(long a);

    native public static float abs(float a);

    native public static double abs(double a);

    native public static int max(int a, int b);

    native public static long max(long a, long b);

    native public static float max(float a, float b);

    native public static double max(double a, double b);

    native public static int min(int a, int b);

    native public static long min(long a, long b);

    native public static float min(float a, float b);

    native public static double min(double a, double b);

    native public static double ulp(double d);

    native public static float ulp(float f);

    native public static double signum(double d);

    native public static float signum(float f);

    native public static double sinh(double x);

    native public static double cosh(double x);

    native public static double tanh(double x);

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

    native public static double scalb(double d, int scaleFactor);

    native public static float scalb(float f, int scaleFactor);
}
