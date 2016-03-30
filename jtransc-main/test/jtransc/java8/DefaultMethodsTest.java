package jtransc.java8;

public class DefaultMethodsTest {
	static public void main(String[] args) {
		test1();
	}

	static private void test1() {
		System.out.println(new MyImpl1().test());
		System.out.println(new MyImpl2().test());
	}

	static private class MyImpl1 implements MyInterface {
	}

	static private class MyImpl2 implements MyInterface {
		@Override
		public String test() {
			return "overriden";
		}
	}

	interface MyInterface {
		default String test() {
			return "default";
		}
	}
}
