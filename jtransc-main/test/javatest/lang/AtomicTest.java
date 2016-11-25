package javatest.lang;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class AtomicTest {
	public volatile Integer value = 3;

	static public void main(String[] test) {
		System.out.println("AtomicTest.main:");
		testBool();
		testAtomicInteger();
		testAtomicReference();
		testAtomicReferenceFieldUpdater();
		System.out.println("/AtomicTest.main:");
	}

	private static void testAtomicInteger() {
		System.out.println("AtomicTest.testAtomicInteger:");
		AtomicInteger ref = new AtomicInteger(10);
		System.out.println(ref.get());
		System.out.println(ref.getAndSet(12));
		ref.set(14);
		System.out.println(ref.get());
		ref.compareAndSet(13, -13);
		System.out.println(ref.get());
		ref.compareAndSet(14, -14);
		System.out.println(ref.get());
	}

	private static void testAtomicReference() {
		System.out.println("AtomicTest.testAtomicReference:");
		AtomicReference<Integer> ref = new AtomicReference<>(10);
		System.out.println(ref.get());
		System.out.println(ref.getAndSet(12));
		ref.set(14);
		System.out.println(ref.get());
		ref.compareAndSet(13, -13);
		System.out.println(ref.get());
		ref.compareAndSet(14, -14);
		System.out.println(ref.get());
	}

	static private void testBool() {
		System.out.println("AtomicTest.testBool:");
		AtomicBoolean atomicBoolean = new AtomicBoolean();
		System.out.println(atomicBoolean.get());
		atomicBoolean.set(true);
		System.out.println(atomicBoolean.get());
	}

	static private void testAtomicReferenceFieldUpdater() {
		System.out.println("AtomicTest.testAtomicReferenceFieldUpdater:");
		AtomicTest obj = new AtomicTest();
		AtomicReferenceFieldUpdater<AtomicTest, Integer> updater = AtomicReferenceFieldUpdater.newUpdater(AtomicTest.class, Integer.class, "value");
		System.out.println(updater.get(obj));
		updater.set(obj, 5);
		System.out.println(updater.get(obj));
		updater.compareAndSet(obj, 4, -4);
		System.out.println(updater.get(obj));
		updater.compareAndSet(obj, 5, -5);
		System.out.println(updater.get(obj));
	}
}
