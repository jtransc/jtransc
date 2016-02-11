package jtransc.game.math;

public class MathUtils {
	//static public int clamp(int v, int min, int max) {
		//return Math.min(Math.max(v, min), max);
	//}

	//static public double clamp(double v, double min, double max) {
		//return Math.min(Math.max(v, min), max);
	//}

	static public int divCeil(int x, int y){
		return 1 + ((x - 1) / y);
	} 
	static public float cos(float value) { return (float) Math.cos(value); }
	static public float sin(float value){ return (float) Math.sin(value); }
	static public int sqrt(float value) { return (int) Math.sqrt(value); }
	//fun reinterpretIntFloat(value: Int): Float = java.lang.Float.intBitsToFloat(value)

	static public double len(double a, double b) {
		return Math.hypot(a, b);
	}
	static public float reinterpretIntFloat(int value) {
		return java.lang.Float.intBitsToFloat(value);
	}

	static public int interpolate(int min, int max, double ratio) {
		return (int) (min + ((max - min) * ratio));
	}
	static public long interpolate(long min, long max, double ratio) {
		return (long) (min + ((max - min) * ratio));
	}
	static public double interpolate(double min, double max, double ratio) {
		//return (double) (min + ((max - min) * ratio));
		return min * (1 - ratio) + max * ratio;
	}

	//fun interpolate(min: Double, max: Double, ratio: Double): Double = min + ((max - min) * ratio)
	static public <T extends Interpolable<T>> T interpolate(T min, T max, double ratio) {
		return min.interpolate(max, ratio);
	}

	static private <T extends Interpolable<T>> T _interpolate(T min, T max, double ratio) {
		return min.interpolate(max, ratio);
	}

	static public <T> T interpolateAny(T min, T max, double ratio) {
		if (min instanceof Integer) return (T)Integer.valueOf(interpolate((Integer)min, (Integer)max, ratio));
		if (min instanceof Long) return (T)Long.valueOf(interpolate((Long)min, (Long)max, ratio));
		if (min instanceof Double) return (T)Double.valueOf(interpolate((Double)min, (Double)max, ratio));
		if (min instanceof Interpolable) return (T) ((Interpolable) min).interpolate(max, ratio);
		throw new RuntimeException("Cannot interpolate");
	}


	/*
	fun interpolate<T : Any>(min:T, max:T, ratio:Double):T {
	}
	*/

	static public double min(double a, double b, double c, double d){ return Math.min(Math.min(a, b), Math.min(c, d)); }
	static public double max(double a, double b, double c, double d){ return Math.max(Math.max(a, b), Math.max(c, d)); }

	static public long clamp(long v, long min, long max) {
		if (v < min) return min; else if (v > max) return max; else return v;
	}
	static public int clamp(int v, int min, int max){
		if (v < min) return min; else if (v > max) return max; else return v;
	}
	static public double clamp(double value, double min, double max) {
		if (value < min) return min; else if (value > max) return max; else return value;
	}
	static public float clamp(float value, float min, float max) {
		if (value < min) return min;
		else if (value > max) return max;
		else return value;
	}
	static public int clampInt(int value, int min, int max) {
		if (value < min) return min; else if (value > max) return max; else return value;
	}
	static public int clampf255(double v) {
		if (v < 0.0) return 0; else if (v > 1.0) return 255; else return (int)(v * 255);
	}
	static public double clampf01(double v) {
		if (v < 0.0) return 0.0; else if (v > 1.0) return 1.0; else return v;
	}
	static public int clampn255(int v) {
		if (v < -255) return -255; else if (v > 255) return 255; else return v;
	}
	static public int clamp255(int v) {
		if (v < 0) return 0; else if (v > 255) return 255; else return v;
	}

	static public double distance(double a, double b) {
		return Math.abs(a - b);
	}
	static public double distanceXY(double x1, double y1, double x2, double y2) {
		return Math.hypot(x1 - x2, y1 - y2);
	}
	static public double distancePoint(Point a, Point b) {
		return distanceXY(a.x, a.y, b.x, b.y);
	}

	static public double smoothstep(double edge0, double edge1, double step) {
		double step2 = clampf01((step - edge0) / (edge1 - edge0));
		return step2 * step2 * (3 - 2 * step2);
	}


	//static public double interpolate(double v0, double v1, double step){
		//return v0 * (1 - step) + v1 * step;
	//}

	static public double modUnsigned(double num, double den) {
		double result = (num % den);
		if (result < 0) result += den;
		return result;
	}

	static public boolean between(double value, double min, double max) {
		return (value >= min) && (value <= max);
	}

	static public double convertRange(double value, double minSrc, double maxSrc, double minDst, double maxDst) {
		return (((value - minSrc) / (maxSrc - minSrc)) * (maxDst - minDst)) + minDst;
	}

	static public int sign(double x) {
		if (x < 0) return -1; else if (x > 0) return +1; else return 0;
	}
	static public int signNonZeroM1(double x) {
		return (x <= 0) ? -1 : +1;
	}
	static public int signNonZeroP1(double x) {
		return (x >= 0) ? +1 : -1;
	}

	static public int multiplyIntegerUnsigned(int a, int b) {
		return (a * b) | 0;
	}
	static public int multiplyIntegerSigned(int a, int b) {
		return (a * b) | 0;
	}
	static public int divideIntegerUnsigned(int a, int b){
		return (a / b) | 0;
	}
	static public int divideIntegerSigned(int a, int b) {
		return (a / b) | 0;
	}
	static public double hypot(double x, double y) { return Math.hypot(x, y); }
	static public double hypotNoSqrt(double x, double y) { return (x * x + y * y); }

	static public double roundDecimalPlaces(double value, int places) {
		double placesFactor = Math.pow(10.0, places);
		return Math.round(value * placesFactor) / placesFactor;
	}

	static public boolean isEquivalent(double a, double b, double epsilon) {
		return (a - epsilon < b) && (a + epsilon > b);
	}
	static public boolean isEquivalent(double a, double b) {
		return isEquivalent(a, b, 0.0001);
	}
	static public int packUintFast(int r, int g, int b, int a) {
		return (a << 24) | (b << 16) | (g << 8) | (r << 0);
	}
	static public int pack4fUint(double r, double g, double b, double a) {
		return packUintFast(clampf255(r), clampf255(g), clampf255(b), clampf255(a));
	}
	static public int log2(int v) {
		return (int) (Math.log(v) / Math.log(2.0));
	}

}
