package jtransc.java8;

public class DefaultMethodsTest {
	static public void main(String[] args) {
		System.out.println("DefaultMethodsTest.main:");
		test1();
	}

	static private void test1() {
		System.out.println("DefaultMethodsTest.test1:");
		System.out.println(new MyImpl1().test());
		System.out.println(new MyImpl2().test());
		System.out.println(new MyImpl1b().test());
		System.out.println(new MyImpl2b().test());
		System.out.println(new MyImpl3b().test());

		MyInterface minterface = new MyImpl3b();
		System.out.println(minterface.test());
	}

	static private class MyImpl1 implements MyInterface {
	}

	static private class MyImpl2 implements MyInterface {
		@Override
		public String test() {
			return "overriden";
		}
	}

	static private abstract class MyImpl3 implements MyInterface {
	}

	static private class MyImpl1b extends MyImpl1 {
	}

	static private class MyImpl2b extends MyImpl2 {
	}

	static private class MyImpl3b extends MyImpl3 {
	}

	interface MyInterface {
		default String test() {
			return "default";
		}
	}
}
