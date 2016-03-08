package javatest.lang;

import java.util.Arrays;

public class StringsTest {

	public static void main(String[] args) throws Throwable {
		basicConcatTest();
		extendedTest();
		matchesTest();
	}

	private static void basicConcatTest() {
		System.out.println("HELLO" + " " + "WORLD!");
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

		for (String s : new String[]{"a", "b", "aa"}) {
			System.out.println("aaaaa".indexOf(s));
			System.out.println("aaaaa".indexOf(s, 2));
			System.out.println("aaaaa".lastIndexOf(s));
			System.out.println("aaaaa".lastIndexOf(s, 2));
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