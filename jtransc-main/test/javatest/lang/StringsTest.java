package javatest.lang;

import com.jtransc.io.JTranscConsole;
import com.jtransc.text.JTranscRegex;

import java.util.Arrays;

public class StringsTest {

	public static void main(String[] args) throws Throwable {
		basicConcatTest();
		compareToIgnoreCaseTest();
		regionMatchesTest();
		extendedTest();
		zeroTest();
		formatTest();
		trimTest();
	}

	private static void basicConcatTest() {
		System.out.println("HELLO" + " " + "WORLD!");
	}

	private static void compareToIgnoreCaseTest() {
		System.out.println("StringsTest.compareToIgnoreCaseTest:");
		System.out.println("abcdef".compareToIgnoreCase("abcdef"));
		System.out.println("abcdef".compareToIgnoreCase("ABCDEF"));
		System.out.println("abcdef".compareToIgnoreCase("aBcDeF"));
		System.out.println("abcdef".compareToIgnoreCase("aBcDeG"));
		System.out.println("AbCdEf".compareToIgnoreCase("aBcDeF"));
		System.out.println("AbCdEf".compareToIgnoreCase("BBcDeF"));
	}

	private static void regionMatchesTest() {
		System.out.println("StringsTest.regionMatchesTest:");
		System.out.println("abcdef".regionMatches(1, "__bc_", 2, 2));
		System.out.println("abcdef".regionMatches(1, "__bc_", 0, 2));
		System.out.println("abcdef".regionMatches(0, "__bc_", 2, 2));

		System.out.println("abcdef".regionMatches(true, 1, "__BC_", 2, 2));
		System.out.println("abcdef".regionMatches(false, 1, "__BC_", 2, 2));
	}

	private static void extendedTest() {
		JTranscConsole.log("StringsTest.extendedTest:");
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

	static public void zeroTest() {
		String a = "abcdefg\0A\0B\0C\0";
		JTranscConsole.log("StringsTest.zeroTest:");
		JTranscConsole.log(a.length());
		for (int n = 0; n < a.length(); n++) JTranscConsole.log((int)a.charAt(n));
	}

	static public void formatTest() {
		JTranscConsole.log("StringsTest.formatTest:");
		JTranscConsole.log(String.format("%d", 10));
		JTranscConsole.log(String.format("%08X", -1));
		JTranscConsole.log(String.format("%d%%", 10));
	}

	static public void trimTest() {
		JTranscConsole.log("".trim());
		JTranscConsole.log("HELLO  ".trim());
		JTranscConsole.log("  HELLO".trim());
		JTranscConsole.log("  HELLO  ".trim());
		JTranscConsole.log("HELLO".trim());
		JTranscConsole.log("A".trim());
		JTranscConsole.log("AB".trim());
	}
}