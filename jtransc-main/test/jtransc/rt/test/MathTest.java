package jtransc.rt.test;

public class MathTest {
	static public void main(String[] args) {
		cbrt();
		rint();
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
}
