package jtransc.rt.test;

public class MathTest {
	static public void main(String[] args) {
		cbrt();
		rint();
		nanInf();
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
}
