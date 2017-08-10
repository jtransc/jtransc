package jtransc.java8;

import java.util.function.Predicate;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Java8Test {
	static public void main(String[] args) {
		System.out.println("Java8Test.main:");
		//demo(i -> (i > 0));
		myrunnerVoid(Java8Test::exampleVoid);
		myrunnerBool(Java8Test::exampleBool);
		myrunnerByte(Java8Test::exampleByte);
		myrunnerShort(Java8Test::exampleShort);
		myrunnerChar(Java8Test::exampleChar);
		myrunnerInt(Java8Test::exampleInt);
		myrunnerInt(Java8Test::exampleIntClass);
		myrunnerLong(Java8Test::exampleLong);
		myrunnerFloat(Java8Test::exampleFloat);
		myrunnerDouble(Java8Test::exampleDouble);
		myrunnerInt(i -> i < 10);
		Java8Test2.main(args);

		//myrunnerDoubleDefaultNegate(Java8Test::exampleDouble);

		//MethodHandle method = Java8Test::example1;
	}

	static public void exampleVoid() {
		System.out.println("exampleVoid()");
	}

	static public boolean exampleBool(boolean b) {
		return !b;
	}

	static public boolean exampleByte(byte b) {
		return b > 0;
	}

	static public boolean exampleShort(short b) {
		return b > 0;
	}

	static public boolean exampleChar(char b) {
		return b > 0;
	}

	static public boolean exampleInt(int i) {
		return i > 0;
	}

	static public boolean exampleLong(long i) {
		return i > 0;
	}

	static public boolean exampleFloat(float i) {
		return i > 0f;
	}

	static public boolean exampleDouble(double i) {
		return i > 0.0;
	}

	static public boolean exampleIntClass(Integer i) {
		return i > 0;
	}

	static public void myrunnerVoid(Runnable run) {
		run.run();
	}

	static public void myrunnerBool(Predicate<Boolean> pred) {
		System.out.println("false: " + pred.test(false));
		System.out.println("true: " + pred.test(true));
	}

	static public void myrunnerByte(Predicate<Byte> pred) {
		System.out.println("0: " + pred.test((byte) 0));
		System.out.println("10: " + pred.test((byte) 10));
	}

	static public void myrunnerShort(Predicate<Short> pred) {
		System.out.println("0: " + pred.test((short) 0));
		System.out.println("10: " + pred.test((short) 10));
	}

	static public void myrunnerChar(Predicate<Character> pred) {
		System.out.println("0: " + pred.test((char) 0));
		System.out.println("10: " + pred.test((char) 10));
	}

	static public void myrunnerInt(Predicate<Integer> pred) {
		System.out.println("0: " + pred.test(0));
		System.out.println("10: " + pred.test(10));
	}

	static public void myrunnerLong(Predicate<Long> pred) {
		System.out.println("0: " + pred.test(0L));
		System.out.println("10: " + pred.test(10L));
	}

	static public void myrunnerFloat(Predicate<Float> pred) {
		System.out.println("0: " + pred.test(0f));
		System.out.println("10: " + pred.test(10f));
	}

	static public void myrunnerDouble(Predicate<Double> pred) {
		System.out.println("0: " + pred.test(0.0));
		System.out.println("10: " + pred.test(10.0));
	}

	static public void myrunnerDoubleDefaultNegate(Predicate<Double> pred) {
		System.out.println("0: " + pred.negate().test(0.0));
		System.out.println("10: " + pred.negate().test(10.0));
	}
}

/*
public class MetafactoryTest {

    public static void main(String[] args) throws Throwable {

        MethodHandles.Lookup caller = MethodHandles.lookup();
        MethodType methodType = MethodType.methodType(Object.class);
        MethodType actualMethodType = MethodType.methodType(String.class);
        MethodType invokedType = MethodType.methodType(Supplier.class);
        CallSite site = LambdaMetafactory.metafactory(
	        caller,
			"get",
			invokedType,
			methodType,
			caller.findStatic(MetafactoryTest.class, "print", actualMethodType),
			methodType
		);
        MethodHandle factory = site.getTarget();
        Supplier<String> r = (Supplier<String>) factory.invoke();
        System.out.println(r.get());
    }

    private static String print() {
        return "hello world";
    }
}
 */