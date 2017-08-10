package javatest.utils.regex;

import com.jtransc.io.JTranscConsole;

import java.util.Arrays;
import java.util.regex.Pattern;

public class RegexTest {
	static public void main(String[] args) {
		patternCreateTest();
		stringMatchesTest();
		stringReplaceAllTest();
		regexMatchesTest();
		replaceTest();
		testRegex();
	}

	private static void patternCreateTest() {
		System.out.println("patternCreateTest:");
		Pattern.compile("HELLO");
		System.out.println("/patternCreateTest:");
	}

	private static void stringMatchesTest() {
		JTranscConsole.log("StringsTest.matchesTest:");
		System.out.println("hello".matches(".*el+.*"));
		System.out.println("hello".matches("el+"));
		System.out.println("hello".matches("ell"));
		System.out.println("hello".matches(".*ell"));
		System.out.println("hello".matches("elll"));
		System.out.println("hello".matches(".*ell.*"));
	}

	static private void regexMatchesTest() {
		Pattern NUMERIC = Pattern.compile("[-]?[0-9]+");
		System.out.println(NUMERIC.matcher("1").matches());
		System.out.println(NUMERIC.matcher("1.0").matches());
		System.out.println(NUMERIC.matcher("1.1").matches());
		System.out.println(NUMERIC.matcher("-1.1").matches());
		System.out.println(NUMERIC.matcher("1.1").find());
		//System.out.println(NUMERIC.matcher("1.1").group());
	}

	private static void stringReplaceAllTest() {
		System.out.println("StringsTest.replaceAllTest:");
		System.out.println("\\\\hello\\\\world".replaceAll("\\\\", "/"));
		System.out.println("\\\\hello\\\\world".replaceFirst("\\\\", "/"));
	}

	private static void replaceTest() {
		System.out.println(Pattern.compile("l").matcher("hello").replaceAll("_"));
		System.out.println(Pattern.compile("l").matcher("hello").replaceFirst("_"));
	}

	private static void testRegex() {
		System.out.println("regex.numbers[true]:" + Pattern.matches("^\\d+$", "10000"));
		System.out.println("regex.numbers[false]:" + Pattern.matches("^\\d+$", "a"));
		System.out.println("regex.split:" + Arrays.toString(Pattern.compile(",+").split("hello,,,world,,b,,c,,d")));
	}
}
