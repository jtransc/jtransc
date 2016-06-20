package jtransc.rt.test;

import com.jtransc.io.JTranscConsole;

public class MathTest {
	static public void main(String[] args) {
		dumpIntBounds();
		zeros();
		cbrt();
		rint();
		nanInf();
		copySign();
		mathTest();
	}

	static private void mathTest() {
		System.out.println("mathTest:");
		System.out.println((int)(Math.hypot(3, 7) * 1000));
		System.out.println((int)(Math.cos(33) * 1000));
		System.out.println((int)(Math.sin(33) * 1000));
	}

	static private void dumpIntBounds() {
		System.out.println("dumpIntBounds:");
		JTranscConsole.log(Integer.toString(Integer.MIN_VALUE));
		JTranscConsole.log(String.valueOf(Integer.MIN_VALUE));
		JTranscConsole.log((long)Integer.MIN_VALUE);
		JTranscConsole.log(((Integer)Integer.MIN_VALUE).toString());
		JTranscConsole.log(Integer.toString(Integer.MIN_VALUE, 7));
		JTranscConsole.log(Integer.toString(Integer.MIN_VALUE, 2));
		JTranscConsole.log(String.format("%d", Integer.MIN_VALUE));
	}

	static private void zeros() {
		System.out.println("zeros:");
		int[] ints = {-1,0,Integer.MIN_VALUE,Integer.MAX_VALUE,77777777,0x1234567,0x1,0x7,0x77,0x777,0x7777,0x77777,0x777777,0x7777777,0x77777777};
		for (int n = 0; n < ints.length; n++) System.out.printf("%d,", ints[n]);
		System.out.println();
		for (int n = 0; n < ints.length; n++) System.out.printf("%d,", Integer.numberOfLeadingZeros(ints[n]));
		System.out.println();
		for (int n = 0; n < ints.length; n++) System.out.printf("%d,", Integer.numberOfTrailingZeros(ints[n]));
		System.out.println();
		for (int n = 0; n < ints.length; n++) System.out.printf("%d,", Integer.bitCount(ints[n]));
		System.out.println();
		for (int n = 0; n < ints.length; n++) System.out.printf("%d,", Integer.rotateLeft(ints[n], 7));
		System.out.println();
		for (int n = 0; n < ints.length; n++) System.out.printf("%d,", Integer.rotateRight(ints[n], 7));
		System.out.println();
		for (int n = 0; n < ints.length; n++) System.out.printf("%d,", Integer.reverse(ints[n]));
		System.out.println();
		for (int n = 0; n < ints.length; n++) System.out.printf("%d,", Integer.reverseBytes(ints[n]));
		System.out.println();
		for (int n = 0; n < ints.length; n++) System.out.printf("%d,", Integer.signum(ints[n]));
		System.out.println();
		for (int n = 0; n < ints.length; n++) System.out.printf("%d,", Integer.highestOneBit(ints[n]));
		System.out.println();
	}

	static private double[] doubles = new double[] { -1.1, -1.0, -0.5, -0.45, 0.0, 0.45, 0.5, 1.0, 1.1 };

	static private void cbrt() {
		for (double v : new double[] { 1.0, 3.0, 9.0, 27.0 }) {
			System.out.print(Math.cbrt(v) + ",");
		}
		System.out.println();
	}

	static private void rint() {
		//for (double v : doubles) System.out.print(Math.rint(v) + ",");
		//System.out.println();
	}

	static private void nanInf() {
		float[] floats = new float[] { 0, 6f, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY };
		double[] doubles = new double[] { 0, 6.35, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY };

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

	static private void copySign() {
		System.out.println("copySign:");
		double[] doubles = new double[] { 0, -128, 6, Double.NaN, -Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY };

		for (double a : doubles) {
			for (double b : doubles) {
				System.out.print(Math.copySign(a, b));
				System.out.print(",");
			}
			System.out.println();
		}
	}
}
