package jtransc.bug;

import java.util.Locale;

public class JTranscBug244 {

	static public void main(String[] args) {
		System.out.println("JTranscBug244.main:");
		Locale.setDefault(Locale.ENGLISH);
		constTest();
		stringFormatTest();
		floatNaN2IntTest();
		floatNaN2LongTest();
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
		System.out.println("String.format(\"%.3f\", 0.1): " + String.format("%.3f", 0.1));
		System.out.println("String.format(\"%.2f\", 1.0): " + String.format("%.2f", 1.0));
		System.out.println("String.format(\"%.2f\", 123f): " + String.format("%.2f", 123f));
		System.out.println("String.format(\"%.2f\", 123.12345): " + String.format("%.2f", 123.12345));
		System.out.println("String.format(\"%.2f\", 0.0): " + String.format("%.2f", 0.0));
		System.out.println("String.format(\"+INF=%.2f\", Float.POSITIVE_INFINITY): " + String.format("+INF=%.2f", Float.POSITIVE_INFINITY));
		System.out.println("String.format(\"-INF=%.2f\", Float.NEGATIVE_INFINITY): " + String.format("-INF=%.2f", Float.NEGATIVE_INFINITY));
		System.out.println("String.format(\"NaN=%.2f\", Float.NaN): " + String.format("NaN=%.2f", Float.NaN));
		System.out.println("String.format ENGLISH: " + String.format(Locale.ENGLISH, "%.2f", 0.12345));
		System.out.println("String.format ITALIAN: " + String.format(Locale.ITALIAN, "%.2f", 0.12345));
		System.out.println("String.format FRENCH: " + String.format(Locale.FRENCH, "%.2f", 0.12345));
		System.out.println("String.format GERMAN: " + String.format(Locale.GERMAN, "%.2f", 0.12345));
		System.out.println("String.format JAPANESE: " + String.format(Locale.JAPANESE, "%.2f", 0.12345));
		System.out.println("String.format CHINESE: " + String.format(Locale.CHINESE, "%.2f", 0.12345));
		System.out.println("String.format RUSSIAN: " + String.format(Locale.forLanguageTag("ru"), "%.2f", 0.12345));
		System.out.println("String.format SPANISH: " + String.format(Locale.forLanguageTag("es"), "%.2f", 0.12345));
	}

	static private void floatNaN2IntTest() {
		System.out.println("floatNaN2LongTest:");
		for (float f : new float[] { Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NaN, 0f, 1f, -1f, 1000f }) {
			System.out.println((int)f);
		}
		for (double d : new double[] { Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN, 0, 1, -1, 1000 }) {
			System.out.println((int)d);
		}
	}

	static private void floatNaN2LongTest() {
		System.out.println("floatNaN2LongTest:");
		for (float f : new float[] { Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NaN, 0f, 1f, -1f, 1000f }) {
			System.out.println((long)f);
		}
		for (double d : new double[] { Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN, 0, 1, -1, 1000, 99999999.0, 9999999999.0, 999999999999999.0, 2251799813685248.0 }) { // Math.pow(2, 51)
			System.out.println((long)d);
		}
	}
}
