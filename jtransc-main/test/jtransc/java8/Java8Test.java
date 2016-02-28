package jtransc.java8;

import java.lang.invoke.MethodHandle;
import java.util.function.Function;
import java.util.function.Predicate;

public class Java8Test {
	static public void main(String[] args) {
		//demo(i -> (i > 0));
		myrunner(Java8Test::example1);

		//MethodHandle method = Java8Test::example1;
	}

	static public boolean example1(int i) {
		return i > 0;
	}

	static public void myrunner(Predicate<Integer> pred) {
		System.out.println("0: " + pred.test(0));
		System.out.println("10: " + pred.test(10));
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