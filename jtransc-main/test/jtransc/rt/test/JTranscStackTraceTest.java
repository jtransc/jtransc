package jtransc.rt.test;

public class JTranscStackTraceTest {
	static public void main(String[] args) {
		test();
	}

	static public void test() {
		StackTraceElement[] items = Thread.currentThread().getStackTrace();
		System.out.println(items.length >= 3);
		System.out.println(items[1].getMethodName() != null); // .contains("test")
		System.out.println(items[2].getMethodName() != null); // .contains("main")
		//for (StackTraceElement i : items) System.out.println(i);
	}
}
