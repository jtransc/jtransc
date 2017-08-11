package jtransc.bug;

public class JTranscBug244 {

	static public void main(String[] args) {
		constTest();
		stringFormatTest();
	}

	static private void constTest() {
		System.out.println("Float.isInfinite(Float.POSITIVE_INFINITY): " + Float.isInfinite(Float.POSITIVE_INFINITY));
		System.out.println("Float.isInfinite(Float.NEGATIVE_INFINITY): " + Float.isInfinite(Float.NEGATIVE_INFINITY));
		System.out.println("Float.isInfinite(Float.NaN): " + Float.isInfinite(Float.NaN));

		System.out.println("Float.isFinite(Float.POSITIVE_INFINITY): " + Float.isFinite(Float.POSITIVE_INFINITY));
		System.out.println("Float.isFinite(Float.NEGATIVE_INFINITY): " + Float.isFinite(Float.NEGATIVE_INFINITY));
		System.out.println("Float.isFinite(Float.NaN): " + Float.isFinite(Float.NaN));

		System.out.println("Float.isNaN(Float.POSITIVE_INFINITY): " + Float.isNaN(Float.POSITIVE_INFINITY));
		System.out.println("Float.isNaN(Float.NEGATIVE_INFINITY): " + Float.isNaN(Float.NEGATIVE_INFINITY));
		System.out.println("Float.isNaN(Float.NaN): " + Float.isNaN(Float.NaN));
	}

	static private void stringFormatTest() {
		System.out.println("String.format(\"%.2f\", 0.12345): " + String.format("%.2f", 0.12345));
		System.out.println("String.format(\"%.0f\", 0.12345): " + String.format("%.0f", 0.12345));
		System.out.println("String.format(\"%.2f\", 0.1): " + String.format("%.2f", 0.1));
		System.out.println("String.format(\"+INF=%.2f\", Float.POSITIVE_INFINITY): " + String.format("+INF=%.2f", Float.POSITIVE_INFINITY));
		System.out.println("String.format(\"-INF=%.2f\", Float.NEGATIVE_INFINITY): " + String.format("-INF=%.2f", Float.NEGATIVE_INFINITY));
		System.out.println("String.format(\"NaN=%.2f\", Float.NaN): " + String.format("NaN=%.2f", Float.NaN));
	}

}
