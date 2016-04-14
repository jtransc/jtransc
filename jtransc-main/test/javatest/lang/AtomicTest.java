package javatest.lang;

import java.util.concurrent.atomic.AtomicBoolean;

public class AtomicTest {
	static public void main(String[] test) {
		testBool();
	}

	static private void testBool() {
		AtomicBoolean atomicBoolean = new AtomicBoolean();
		System.out.println(atomicBoolean.get());
		atomicBoolean.set(true);
		System.out.println(atomicBoolean.get());
	}
}
