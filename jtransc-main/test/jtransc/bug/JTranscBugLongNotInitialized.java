package jtransc.bug;

public class JTranscBugLongNotInitialized {
	static public void main(String[] args) throws Throwable {
		test1();
		test2();
		test3();
	}

	static private void test1() {
		// WORKS
		System.out.println(new A().value < 100);
	}

	static private void test2() throws Throwable {
		// WORKS
		System.out.println(A.class.newInstance().value < 100);
	}

	static private void test3() throws Throwable {
		// FAILS
		System.out.println(((A) A.class.getDeclaredConstructors()[0].newInstance()).value < 100);
	}

	static class A {
		public long value;

		public A() {
		}
	}
}

