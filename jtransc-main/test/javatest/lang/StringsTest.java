package javatest.lang;

import com.jtransc.text.JTranscRegex;

import java.util.Arrays;

public class StringsTest {

	public static void main(String[] args) throws Throwable {
		basicConcatTest();
		compareToIgnoreCaseTest();
		regionMatchesTest();
		extendedTest();
		matchesTest();
		replaceAllTest();
	}

	private static void basicConcatTest() {
		System.out.println("HELLO" + " " + "WORLD!");
	}

	private static void replaceAllTest() {
		System.out.println("replaceAllTest:");
		System.out.println("\\\\hello\\\\world".replaceAll("\\\\", "/"));
		System.out.println("\\\\hello\\\\world".replaceFirst("\\\\", "/"));
	}

	private static void compareToIgnoreCaseTest() {
		System.out.println("compareToIgnoreCaseTest:");
		System.out.println("abcdef".compareToIgnoreCase("abcdef"));
		System.out.println("abcdef".compareToIgnoreCase("ABCDEF"));
		System.out.println("abcdef".compareToIgnoreCase("aBcDeF"));
		System.out.println("abcdef".compareToIgnoreCase("aBcDeG"));
		System.out.println("AbCdEf".compareToIgnoreCase("aBcDeF"));
		System.out.println("AbCdEf".compareToIgnoreCase("BBcDeF"));
	}

	private static void regionMatchesTest() {
		System.out.println("regionMatchesTest:");
		System.out.println("abcdef".regionMatches(1, "__bc_", 2, 2));
		System.out.println("abcdef".regionMatches(1, "__bc_", 0, 2));
		System.out.println("abcdef".regionMatches(0, "__bc_", 2, 2));

		System.out.println("abcdef".regionMatches(true, 1, "__BC_", 2, 2));
		System.out.println("abcdef".regionMatches(false, 1, "__BC_", 2, 2));
	}

	private static void extendedTest() {
		Comparable<String> a = "a";
		System.out.println("a".equals("a"));
		System.out.println("a".equals("b"));

		System.out.println("abcdef".equals("abcdef"));
		System.out.println("abcdef".equals("abcde_"));
		System.out.println("abcdef".equals("_bcdef"));

		System.out.println("abcdef".hashCode());

		System.out.println("a".toUpperCase());
		System.out.println("a".compareTo("b"));
		System.out.println("a".compareTo("a"));
		System.out.println("b".compareTo("a"));
		System.out.println(a.compareTo("b"));

		System.out.println("hello".replace('l', 'p'));
		System.out.println("hello".replace("ll", "__"));

		for (String s : new String[]{"a", "b", "aa", "aaaaaaa"}) {
			System.out.println("i0:" + "aaaaa".indexOf(s));
			System.out.println("i1:" + "aaaaa".indexOf(s, 2));
			System.out.println("i2:" + "aaaaa".lastIndexOf(s));
			System.out.println("i3:" + "aaaaa".lastIndexOf(s, 2));
		}

		System.out.println(Arrays.toString("1:2:3:4:5".split(":")));
		System.out.println(Arrays.toString("1:2:3:4:5".split(":", 2)));

		//try {
		//	((Test0<Integer>) null).getTest();
		//} catch (Throwable t) {
		//}
	}

	static public void matchesTest() {
		System.out.println("hello".matches(".*el+.*"));
		System.out.println("hello".matches("el+"));
		System.out.println("hello".matches("ell"));
		System.out.println("hello".matches(".*ell"));
		System.out.println("hello".matches("elll"));
		System.out.println("hello".matches(".*ell.*"));
	}
}