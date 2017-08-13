package jtransc.bug;

import java.util.Locale;

public class JTranscBug244 {

	static public void main(String[] args) {
		System.out.println("JTranscBug244.main:");
		Locale.setDefault(Locale.ENGLISH);
		constTest();
		stringFormatTest();
		floatNaN2IntTest();
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
		float f1 = Float.POSITIVE_INFINITY;
		float f2 = Float.NEGATIVE_INFINITY;
		float f3 = Float.NaN;
		int i1 = (int) f1;
		int i2 = (int) f2;
		int i3 = (int) f3;
		System.out.println(i1);
		System.out.println(i2);
		System.out.println(i3);
	}
}
