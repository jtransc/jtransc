package javatest.lang;

import com.jtransc.io.JTranscConsole;

/**
 * Created by mike on 4/11/15.
 */
public class BasicTypesTest {

	public static void main(String[] args) throws Throwable {
		showInts();
		compareInts();
		parseTests();
		compareNans();
		byteTests();
		charTests();
		shortTests();
		integerTests();
		longTests();
		swapTests();
		checkNanInf();
		conversionTests();
		castNanInfinite();
	}

	private static void conversionTests(double v) {
		//System.out.println("conversionTests:" + v + " : " + (long)v);
		System.out.println("conversionTests(double):" + (long)v);
	}

	private static void conversionTests() {
		conversionTests(0.0);
		conversionTests(-1.0);
		conversionTests(0.125);
		conversionTests(1000.125);
		conversionTests(-1000);
		conversionTests(2195652608.0);
	}

	private static void swapTests() {
		System.out.println(Short.reverseBytes((short) 0x1234));
		System.out.println(Short.reverseBytes((short) 0xF5F2));
		System.out.println(Integer.reverseBytes(0x12345678));
		System.out.println(Integer.reverseBytes(0xF2345678));
		System.out.println(0x123456789ABCDEF0L);
		System.out.println(-1234567890);
		System.out.println(Long.reverseBytes(0x123456789ABCDEF0L));
		System.out.println(Long.reverseBytes(-1234567890));
	}

	private static void byteTests() {
		byte one = 1;
		byte min = Byte.MIN_VALUE;
		System.out.println("Byte MIN_VALUE: " + min);
		byte max = Byte.MAX_VALUE;
		System.out.println("Byte MAX_VALUE: " + max);
		byte maxOverflow = (byte) (max + one);
		System.out.println("Byte MAX overflow: " + maxOverflow);
		byte minOverflow = (byte) (max - one);
		System.out.println("Byte MIN overflow: " + minOverflow);
		System.out.println("Byte MAX - MIN: " + (max - min));
	}

	private static void charTests() {
		char one = 1;
		char min = Character.MIN_VALUE;
		System.out.println("Char MIN_VALUE: " + (int)min);
		char max = Character.MAX_VALUE;
		System.out.println("Char MAX_VALUE: " + (int)max);
		char maxOverflow = (char) (max + one);
		System.out.println("Char MAX overflow: " + (int)maxOverflow);
		char minOverflow = (char) (max - one);
		System.out.println("Char MIN overflow: " + (int)minOverflow);
		System.out.println("Char MAX - MIN: " + (int)(max - min));
	}

	private static void shortTests() {
		short one = 1;
		short min = Short.MIN_VALUE;
		System.out.println("Short MIN_VALUE: " + min);
		short max = Short.MAX_VALUE;
		System.out.println("Short MAX_VALUE: " + max);
		short maxOverflow = (short) (max + one);
		System.out.println("Short MAX overflow: " + maxOverflow);
		short minOverflow = (short) (max - one);
		System.out.println("Short MIN overflow: " + minOverflow);
		System.out.println("Short MAX - MIN: " + (max - min));

		short five = 5;
		short mult = (short) (five * five);
		System.out.println("Short 5 * 5: " + mult);

		short div = (short) (five / five);
		System.out.println("Short 5 / 5: " + div);

		short mod = (short) (five % 2);
		System.out.println("Short 5 % 2: " + mod);

		short shift = 256 >> 2;
		System.out.println("Short 256 >> 2: " + shift);

		short unshift = 256 << 2;
		System.out.println("Short 256 << 2: " + unshift);

	}

	private static void integerTests() {
		int one = 1;
		int min = Integer.MIN_VALUE;
		System.out.println("Int MIN_VALUE: " + min);
		int max = Integer.MAX_VALUE;
		System.out.println("Int MAX_VALUE: " + max);
		System.out.println("Int MAX overflow: " + (max + one));
		System.out.println("Int MIN overflow: " + (min - one));
		System.out.println("Int MAX - MIN: " + (max - min));

		int mult = 5 * 5;
		System.out.println("Int 5 * 5: " + mult);

		int div = 5 / 5;
		System.out.println("Int 5 / 5: " + div);

		int mod = 5 % 2;
		System.out.println("Int 5 % 2: " + mod);

		int shift = 256 >> 2;
		System.out.println("Int 256 >> 2: " + shift);

		int unshift = 256 << 2;
		System.out.println("Int 256 << 2: " + unshift);

		System.out.println("Int .toUnsignedString: " + Integer.toUnsignedString(-1));
		System.out.println("Int .toUnsignedString: " + Integer.toUnsignedString(-2));
		System.out.println("Int .toUnsignedString: " + Integer.toUnsignedString(-123456));
		System.out.println("Int .toUnsignedString: " + Integer.toUnsignedString(1));
		System.out.println("Int .toUnsignedString: " + Integer.toUnsignedString(-999999));
		System.out.println("Int .toUnsignedString: " + Integer.toUnsignedString(999999));
		System.out.println("Int .toUnsignedString: " + Integer.toUnsignedString(0));

		System.out.println("Int .toHexString: " + Integer.toHexString(-1));
		System.out.println("Int .toHexString: " + Integer.toHexString(-2));
		System.out.println("Int .toHexString: " + Integer.toHexString(-123456));
		System.out.println("Int .toHexString: " + Integer.toHexString(1));
		System.out.println("Int .toHexString: " + Integer.toHexString(-999999));
		System.out.println("Int .toHexString: " + Integer.toHexString(999999));
		System.out.println("Int .toHexString: " + Integer.toHexString(0));

		System.out.println("Int .toOctalString: " + Integer.toOctalString(-1));
		System.out.println("Int .toOctalString: " + Integer.toOctalString(+1));
		System.out.println("Int .toOctalString: " + Integer.toOctalString(999999));

		System.out.println("Int .toBinaryString: " + Integer.toBinaryString(-1));
		System.out.println("Int .toBinaryString: " + Integer.toBinaryString(+1));
		System.out.println("Int .toBinaryString: " + Integer.toBinaryString(999999));

		System.out.println("Int .toUnsignedString: " + Integer.toUnsignedString(-1, 16));
		System.out.println("Int .toUnsignedString: " + Integer.toUnsignedString(-2, 16));
		System.out.println("Int .toUnsignedString: " + Integer.toUnsignedString(-123456, 16));
		System.out.println("Int .toUnsignedString: " + Integer.toUnsignedString(1, 16));
		System.out.println("Int .toUnsignedString: " + Integer.toUnsignedString(-999999, 16));
		System.out.println("Int .toUnsignedString: " + Integer.toUnsignedString(999999, 16));
		System.out.println("Int .toUnsignedString: " + Integer.toUnsignedString(0, 16));
	}

	private static void longTests() {
		long min = Long.MIN_VALUE;
		System.out.println("Long MIN_VALUE: " + min);
		long max = Long.MAX_VALUE;
		System.out.println("Long MAX_VALUE: " + max);
		System.out.println("Long MAX overflow: " + (max + 1));
		System.out.println("Long MIN overflow: " + (min - 1));
		System.out.println("Long MAX - MIN: " + (max - min));

		long five = 5;
		long mult = five * five;
		System.out.println("Long 5 * 5: " + mult);

		long div = five / five;
		System.out.println("Long 5 / 5: " + div);

		long mod = five % 2L;
		System.out.println("Long 5 % 2: " + mod);

		long shift = 256L >> 2L;
		System.out.println("Long 256 >> 2: " + shift);

		long unshift = 256L << 2L;
		System.out.println("Long 256 << 2: " + unshift);

		System.out.println("Long 0x12345678L: " + 0x12345678L);
		System.out.println("Long 0xFFFFFFFFL: " + 0xFFFFFFFFL);
		System.out.println("Long -3L & 0xFFFFFFFFL: " + (-3L & 0xFFFFFFFFL));

		System.out.println("Long.Int 0x12345678L: " + (int) 0x12345678L);
		System.out.println("Long.Int 0xFFFFFFFFL: " + (int) 0xFFFFFFFFL);
		System.out.println("Long.Int -3L & 0xFFFFFFFFL: " + (int) (-3L & 0xFFFFFFFFL));

		System.out.println("Long .toString: " + Long.toString(-1L));
		System.out.println("Long .toString: " + Long.toString(1L));
		System.out.println("Long .toString: " + Long.toString(-999999L));
		System.out.println("Long .toString: " + Long.toString(999999L));
		System.out.println("Long .toString: " + Long.toString(0L));

		System.out.println("Long .toString: " + Long.toString(-1L, 16));
		System.out.println("Long .toString: " + Long.toString(1L, 16));
		System.out.println("Long .toString: " + Long.toString(-999999L, 16));
		System.out.println("Long .toString: " + Long.toString(999999L, 16));
		System.out.println("Long .toString: " + Long.toString(0L, 16));

		System.out.println("Long .toUnsignedString: " + Long.toUnsignedString(-1L));
		System.out.println("Long .toUnsignedString: " + Long.toUnsignedString(-2L));
		System.out.println("Long .toUnsignedString: " + Long.toUnsignedString(-123456L));
		System.out.println("Long .toUnsignedString: " + Long.toUnsignedString(1L));
		System.out.println("Long .toUnsignedString: " + Long.toUnsignedString(-999999L));
		System.out.println("Long .toUnsignedString: " + Long.toUnsignedString(999999L));
		System.out.println("Long .toUnsignedString: " + Long.toUnsignedString(0L));

		System.out.println("Long .toUnsignedString: " + Long.toUnsignedString(-1L, 16));
		System.out.println("Long .toUnsignedString: " + Long.toUnsignedString(1L, 16));
		System.out.println("Long .toUnsignedString: " + Long.toUnsignedString(-999999L, 16));
		System.out.println("Long .toUnsignedString: " + Long.toUnsignedString(999999L, 16));
		System.out.println("Long .toUnsignedString: " + Long.toUnsignedString(0L, 16));
	}

	private static void parseTests() {
		System.out.println("parseTests:");
		System.out.println("+10: " + Integer.parseInt("+10"));
		System.out.println("+20: " + Integer.parseInt("+20", 16));
		try {
			System.out.println("+20: " + Integer.parseInt("+20 ", 16));
		} catch (NumberFormatException nfe) {
			System.out.println(nfe.getMessage());
		}
		try {
			System.out.println("++20: " + Integer.parseInt("++20", 16));
		} catch (NumberFormatException nfe) {
			System.out.println(nfe.getMessage());
		}
		try {
			System.out.println("--20: " + Integer.parseInt("--20", 16));
		} catch (NumberFormatException nfe) {
			System.out.println(nfe.getMessage());
		}

		System.out.println(Integer.parseInt("0"));
		System.out.println(Integer.parseInt("-1"));
		System.out.println(Integer.parseInt("123456789"));
		System.out.println(Integer.parseInt("-123456789"));

		System.out.println(Integer.parseInt("2147483647"));
		//System.out.println(Integer.parseInt("2147483648"));
		//System.out.println(Integer.parseInt("2147483649"));
		System.out.println(Integer.parseInt("-2147483648"));
		//System.out.println(Integer.parseInt("-2147483649"));
		//System.out.println(Integer.parseInt("-11111111111"));
		//System.out.println(Integer.parseInt("+11111111111"));
		//System.out.println(Integer.parseInt("11111111111"));

		System.out.println(Integer.parseInt("0", 16));
		System.out.println(Integer.parseInt("-1", 16));
		System.out.println(Integer.parseInt("12345678", 16));
		System.out.println(Integer.parseInt("-12345678", 16));

		System.out.println(Long.parseLong("0"));
		System.out.println(Long.parseLong("-1"));
		System.out.println(Long.parseLong("123456789"));
		System.out.println(Long.parseLong("-123456789"));
		System.out.println(Long.parseLong("0", 16));
		System.out.println(Long.parseLong("-1", 16));
		System.out.println(Long.parseLong("12345678", 16));
		System.out.println(Long.parseLong("-12345678", 16));

		System.out.println(Float.parseFloat("0.0"));
		System.out.println(Float.parseFloat("1.0"));

		System.out.println(Double.parseDouble("0.0"));
		System.out.println(Double.parseDouble("1.0"));
		System.out.println(Double.parseDouble("-74"));

		System.out.println(Long.parseLong("74"));
		System.out.println((double)(int)Long.parseLong("74"));
		System.out.println((double)Long.parseLong("74"));

		System.out.println(Long.parseLong("-74"));
		System.out.println((double)(int)Long.parseLong("-74"));
		System.out.println((double)Long.parseLong("-74"));
		System.out.println((double)Long.MIN_VALUE < 0.0);
		System.out.println((double)Long.MAX_VALUE < 0.0);
	}

	private static void showInts() {
		System.out.println("showInts:");
		for (int a : ints) {
			System.out.println(Integer.toString(a, 2));
			System.out.println(Integer.toString(a, 8));
			System.out.println(Integer.toString(a, 16));
			System.out.println(Integer.toString(a, 20));
		}
	}

	static private int[] ints = {Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -9999, -2, -1, 0, +1, +2, 9999, Integer.MAX_VALUE - 1, Integer.MAX_VALUE};

	private static void compareInts() {
		System.out.println("compareInts:");
		for (int a : ints) {
			for (int b : ints) {
				System.out.print(Integer.compare(a, b));
				System.out.print(",");
				System.out.print(Integer.compareUnsigned(a, b));
				System.out.print(",");
				if (b != 0) {
					System.out.print(Integer.divideUnsigned(a, b));
				} else {
					System.out.print("-");
				}
				System.out.print(",");
				if (b != 0) {
					System.out.print(Integer.remainderUnsigned(a, b));
				} else {
					System.out.print("-");
				}
				System.out.print(",");
			}
			System.out.println();
		}
	}

	private static void compareNans() {
		System.out.println("compareNans:");
		float[] floats = new float[]{0f, -1f, +1f, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NaN, -Float.NaN};
		double[] doubles = new double[]{0.0, -1.0, +1.0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN, -Double.NaN};
		System.out.println("compareNans.float:");
		//for (float v : floats) JTranscConsole.log(v);
		for (float l : floats) {
			System.out.print(l + ": ");
			for (float r : floats) System.out.print(", " + Float.compare(l, r));
			System.out.println();
		}
		System.out.println("compareNans.double:");
		//for (double v : doubles) JTranscConsole.log(v);
		for (double l : doubles) {
			System.out.print(l + ": ");
			for (double r : doubles) System.out.print(", " + Double.compare(l, r));
			System.out.println();
		}
		float[] floats2 = new float[] {-360f, -180f, 0f, 1f, 180f, 360f};
		System.out.println("floats2 variable comparisons:");
		for (double l : floats2) {
			System.out.println(l + ": ");
			for (double r : doubles) System.out.print(", " + (l < r));
			System.out.println();
			for (double r : doubles) System.out.print(", " + (l >= r));
			System.out.println();
			for (double r : doubles) System.out.print(", " + (l == r));
			System.out.println();
			for (double r : doubles) System.out.print(", " + (l != r));
			System.out.println();
		}
		System.out.println("floats2 constant comparisons:");
		for (double l : floats2) {
			System.out.println(l + ": ");
			System.out.print(", " + (l < 180f));
			System.out.println();
			System.out.print(", " + (l >= 1f));
			System.out.println();
			System.out.print(", " + (l == 180f));
			System.out.println();
			System.out.print(", " + (l != 0f));
			System.out.println();
		}
		System.out.println("Reinterprets:");
		System.out.println(Float.floatToIntBits(1f));
		System.out.println(Float.floatToRawIntBits(1f));
		System.out.println(Double.doubleToLongBits(1.0));
		System.out.println(Double.doubleToRawLongBits(1.0));

		System.out.println(Float.intBitsToFloat(1065353216));
		System.out.println(Double.longBitsToDouble(4607182418800017408L));

		//System.out.println(Float.toHexString(12345.125f));
		//System.out.println(Double.toHexString(12345.125f));
	}

	static private void checkNanInf() {
		System.out.println("checkNanInf:");
		float[] floats = new float[] { 0, 6f, -Float.NaN, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY };
		double[] doubles = new double[] { 0, 6.35, -Float.NaN, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY };

		for (float v : floats) {
			System.out.print(v);
			System.out.print(":");
			System.out.print(Float.isNaN(v));
			System.out.print(Float.isFinite(v));
			System.out.print(Float.isInfinite(v));
			System.out.print(",");
		}
		System.out.println();
		for (double v : doubles) {
			System.out.print(v);
			System.out.print(":");
			System.out.print(Double.isNaN(v));
			System.out.print(Double.isFinite(v));
			System.out.print(Double.isInfinite(v));
			System.out.print(",");
		}
		System.out.println();
	}

	static private void castNanInfinite() {
		System.out.println((long)Double.NaN);
		System.out.println((int)Double.NaN);
		System.out.println((long)Double.POSITIVE_INFINITY);
		System.out.println((int)Double.POSITIVE_INFINITY);
		System.out.println((long)Double.NEGATIVE_INFINITY);
		System.out.println((int)Double.NEGATIVE_INFINITY);

		System.out.println((long)Float.NaN);
		System.out.println((int)Float.NaN);
		System.out.println((long)Float.POSITIVE_INFINITY);
		System.out.println((int)Float.POSITIVE_INFINITY);
		System.out.println((long)Float.NEGATIVE_INFINITY);
		System.out.println((int)Float.NEGATIVE_INFINITY);
	}
}
