package jtransc.rt.test;

import com.jtransc.io.JTranscConsole;

public class MathTest {
	static public void main(String[] args) {
		//if (JTranscSystem.usingJTransc()) {
		//	throw new RuntimeException("testing that travis fails!");
		//}

		System.out.println("MathTest:");
		dumpIntBounds();
		zeros();
		cbrt();
		rint();
		nanInf();
		rawBitsFloat();
		rawBitsDouble();
		mathTest();
		intDouble();
	}

	static private void mathTest() {
		System.out.println("mathTest:");
		System.out.println((int) (Math.hypot(3, 7) * 1000));
		System.out.println((int) (Math.cos(33) * 1000));
		System.out.println((int) (Math.sin(33) * 1000));
	}

	static private void dumpIntBounds() {
		System.out.println("dumpIntBounds:");
		JTranscConsole.log(1234000012);
		JTranscConsole.log(Integer.toString(Integer.MIN_VALUE));
		JTranscConsole.log(String.valueOf(Integer.MIN_VALUE));
		JTranscConsole.log(Integer.MIN_VALUE);
		JTranscConsole.log((long) Integer.MIN_VALUE);
		System.out.println(Integer.MIN_VALUE);
		System.out.println(((long)Integer.MIN_VALUE) >> 32);
		System.out.println((int)(((long)Integer.MIN_VALUE) >> 32));
		System.out.println("A(" + (int)(((long)Integer.MIN_VALUE) >> 32) + ")");
		System.out.println(Long.MIN_VALUE);
		JTranscConsole.log(((Integer) Integer.MIN_VALUE).toString());
		JTranscConsole.log(Integer.toString(Integer.MIN_VALUE, 7));
		JTranscConsole.log(Integer.toString(Integer.MIN_VALUE, 2));
		JTranscConsole.log(String.format("%d", Integer.MIN_VALUE));
	}

	static private void zeros() {
		System.out.println("zeros:");
		int[] ints = {-1, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 77777777, 0x1234567, 0x1, 0x7, 0x77, 0x777, 0x7777, 0x77777, 0x777777, 0x7777777, 0x77777777};
		long[] longs = {-1, 0, Long.MIN_VALUE, Long.MAX_VALUE, 77777777, 0x1234567, 0x1, 0x7, 0x77, 0x777, 0x7777, 0x77777, 0x777777, 0x7777777, 0x77777777, 0x7777777788888888L};

		System.out.print("ints:");
		for (int v : ints) System.out.printf("%d,", v);
		System.out.println();

		System.out.print("numberOfLeadingZeros:");
		for (int v : ints) System.out.printf("%d,", Integer.numberOfLeadingZeros(v));
		System.out.println();

		System.out.print("numberOfTrailingZeros:");
		for (int v : ints) System.out.printf("%d,", Integer.numberOfTrailingZeros(v));
		System.out.println();

		System.out.print("bitCount:");
		for (int anInt1 : ints) System.out.printf("%d,", Integer.bitCount(anInt1));
		System.out.println();

		System.out.print("rotateLeft:");
		for (int v : ints) System.out.printf("%d,", Integer.rotateLeft(v, 7));
		System.out.println();

		System.out.print("rotateRight:");
		for (int v : ints) System.out.printf("%d,", Integer.rotateRight(v, 7));
		System.out.println();

		System.out.print("rotateLeft[long]:");
		for (long v : longs) System.out.printf("%d,", Long.rotateLeft(v, 7));
		System.out.println();

		System.out.print("rotateRight[long]:");
		for (long v : longs) System.out.printf("%d,", Long.rotateRight(v, 7));
		System.out.println();

		System.out.print("reverse:");
		for (int v : ints) System.out.printf("%d,", Integer.reverse(v));
		System.out.println();

		System.out.print("reverseBytes:");
		for (int v : ints) System.out.printf("%d,", Integer.reverseBytes(v));
		System.out.println();

		System.out.print("signum:");
		for (int v : ints) System.out.printf("%d,", Integer.signum(v));
		System.out.println();

		System.out.print("highestOneBit:");
		for (int v : ints) System.out.printf("%d,", Integer.highestOneBit(v));
		System.out.println();
	}

	static private double[] doubles = new double[]{-1.1, -1.0, -0.5, -0.45, 0.0, 0.45, 0.5, 1.0, 1.1};

	static private void cbrt() {
		System.out.println("cbrt:");
		//for (double v : new double[] { 1.0, 3.0, 9.0, 27.0 }) {
		for (double v : new double[]{1.0, 27.0}) {
			System.out.print(Math.cbrt(v) + ",");
		}
		System.out.println();
	}

	static private void rint() {
		//for (double v : doubles) System.out.print(Math.rint(v) + ",");
		//System.out.println();
	}

	static private void nanInf() {
		System.out.println("nanInf:");
		float[] floats = new float[]{0, 6f, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY};
		double[] doubles = new double[]{0, 6.35, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};

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

	static private void rawBitsFloat() {
		System.out.println("rawBitsFloat:");
		float[] values = new float[]{0, -128, 6, Float.NaN, -Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY};

		for (float a : values) {
			System.out.print(a);
			System.out.printf("=%08X,", Float.floatToRawIntBits(a));
		}
		System.out.println();
		System.out.println("rawBitsFloat+copySign:");
		for (float a : values) {
			for (float b : values) {
				float d = Math.copySign(a, b);
				System.out.print(d);
				System.out.printf("=%08X,", Float.floatToRawIntBits(d));
			}
			System.out.println();
		}
	}

	static private void rawBitsDouble() {
		System.out.println("rawBitsDouble:");
		double[] values = new double[]{0, -128, 6, Double.NaN, -Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};

		for (double a : values) {
			System.out.print(a);
			System.out.printf("=%016X,", Double.doubleToRawLongBits(a));
		}
		System.out.println();
		System.out.println("rawBitsDouble+copySign:");
		for (double a : values) {
			for (double b : values) {
				double d = Math.copySign(a, b);
				System.out.print(d);
				System.out.printf("=%016X,", Double.doubleToRawLongBits(d));
			}
			System.out.println();
		}
	}

	static private void intDouble() {
		short ba = 1;
		short bb = 2;
		short sa = 1;
		short sb = 2;
		int ia = 1;
		int ib = 2;
		long la = 1L;
		long lb = 2L;
		System.out.println("intDouble:");
		System.out.println(ia / ib);
		System.out.println((float) ba / (float) bb);
		System.out.println((double) ba / (double) bb);
		System.out.println((float) sa / (float) sb);
		System.out.println((double) sa / (double) sb);
		System.out.println((float) ia / (float) ib);
		System.out.println((double) ia / (double) ib);
		System.out.println((float) la / (float) lb);
		System.out.println((double) la / (double) lb);
		System.out.println(Math.ceil((float) la / (float) lb));
		System.out.println(Math.ceil((double) la / (double) lb));
	}
}
