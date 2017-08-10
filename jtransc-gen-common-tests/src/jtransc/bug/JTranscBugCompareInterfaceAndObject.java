package jtransc.bug;

public class JTranscBugCompareInterfaceAndObject {
	interface A { }
	interface B { }
	interface C extends B { }

	static public void main(String[] args) {
		test1(null, null);
		test2(null, null);
		test3(null, null);
		test4(null, null, null, null);
		cast1(null, null, null, null);
	}

	private static void test1(Object a, A b) {
		System.out.println(a == b);
	}

	private static void test2(A a, B b) {
		System.out.println(a == b);
	}

	private static void test3(Integer a, String b) {
		System.out.println((Object)a == (Object)b);
	}

	private static void test4(A a, B b, C c, Object d) {
		System.out.println(a == b);
		System.out.println(a == c);
		System.out.println(a == d);
		System.out.println(b == c);
		System.out.println(b == d);
		System.out.println(c == d);
	}

	private static void cast1(A a, B b, C c, Object d) {
		System.out.println(a != null);
		System.out.println(b != null);
		System.out.println(c != null);
		System.out.println(d != null);
		System.out.println(d != null);
		System.out.println(d != null);
		System.out.println(d != null);
	}
}
