package jtransc.rt.test;

public class AssertionTests {
	static public void main(String[] args) {
		testAssert(10);
		testAssert(9);
	}

	static public void testAssert(int value) {
		try {
			assert value < 10 : "Error !(" + value + " < " + 10 + ")";
			System.out.println("ok");
		}catch (Throwable t) {
			System.out.println(t.getMessage());
		}
	}
}
