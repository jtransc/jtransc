package jtransc.rt.test;

import java.lang.annotation.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.List;

public class JTranscReflectionTest {
	static public void main(String[] args) {
		assignableTest();
		annotationTest();
		fieldTest();
		arrayTest();
		annotationsInConstructorTest();
		new TestDeprecatedExample().demo();
	}

	static private void annotationsInConstructorTest() {
		Annotation[][] parameterAnnotations = Test.class.getConstructors()[0].getParameterAnnotations();
		for (Annotation[] parameterAnnotation : parameterAnnotations) {
			System.out.println("::");
			for (Annotation annotation : parameterAnnotation) {
				System.out.println(":: " + annotation.toString());
			}
		}

	}

	static class Test {
		public Test(int a, @TestAnnotation1(10) @TestAnnotation2(20) int b) {
		}
	}

	static public void arrayTest() {
		int[] items = (int[]) Array.newInstance(Integer.TYPE, 10);
		for (int n = 0; n < 10; n++) items[n] = n * 10;
		System.out.println(items.length);
		System.out.println(items.getClass().getComponentType());
		for (int n = 0; n < 10; n++) System.out.println(Array.getInt(items, n));
		for (int n = 0; n < 10; n++) Array.setInt(items, n, n * 20);
		for (int n = 0; n < 10; n++) System.out.println(items[n]);
		for (int n = 0; n < 10; n++) Array.set(items, n, n * 40);
		for (int n = 0; n < 10; n++) System.out.println(items[n] + 10);

		System.out.println((new String[0]).getClass().getComponentType());
	}

	static public void assignableTest() {
		Class[] classes = {A.class, B.class, I.class};
		A.aCount = 0;
		B.bCount = 0;
		for (Class a : classes) {
			for (Class b : classes) {
				System.out.print(a.isAssignableFrom(b));
				System.out.print(",");
			}
			System.out.println();
		}
		System.out.println(A.aCount);
		System.out.println(B.bCount);
	}

	static public void annotationTest() {
		Singleton test1 = Test1.class.getAnnotation(Singleton.class);
		Singleton test2 = Test2.class.getAnnotation(Singleton.class);
		System.out.println(test1.declare());
		System.out.println(test1.store());
		System.out.println(test1.a());
		System.out.println(test1.b());
		System.out.println(test2.declare());
		System.out.println(test2.store());
		System.out.println(test2.a());
		System.out.println(test2.b());
		//System.out.println(test1);
		//System.out.println(test2);

		System.out.println(InjectStore.valueOf(EnumDemo.class, "BB").msg);

		try {
			System.out.println(MyDemo.class.getField("items").getGenericType());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		System.out.println(new ATest1<Integer, String>().new2().new3().getClass().getCanonicalName());
	}


	static private void fieldTest() {
		Class<FieldTestClass> clazz = FieldTestClass.class;
		FieldTestClass instance = new FieldTestClass();
		try {
			System.out.println(clazz.getField("_byte").getClass());
			System.out.println(clazz.getField("_byte").getDeclaringClass());
			System.out.println(clazz.getField("_byte").getType());
			clazz.getField("_byte").set(instance, (byte)7);
			clazz.getField("_Byte").set(instance, (byte)7);
			clazz.getField("_int").set(instance, 7);
			clazz.getField("_Integer").set(instance, 7);

			//clazz.getField("_byte").set(instance, (int)7);
			//clazz.getField("_Byte").set(instance, (int)7);

			System.out.println(clazz.getField("_byte").get(instance).getClass());
			System.out.println(clazz.getField("_int").get(instance).getClass());

			clazz.getField("_double").set(instance, 0.2);

			System.out.println(instance._byte);
			System.out.println(instance._Byte);
			System.out.println(instance._int);
			System.out.println(instance._Integer);

			System.out.println(instance._int * 2);
			System.out.println(instance._int + 1);
			System.out.println(instance._Integer * 2);
			System.out.println(instance._Integer + 1);
			System.out.println(instance._double + 2);

			clazz.getField("_byte").setByte(instance, (byte) 3);
			clazz.getField("_int").setInt(instance, 3);
			//clazz.getField("_Integer").setInt(instance, 3);

			System.out.println(instance._int);
			System.out.println(instance._Integer);
			System.out.println(instance._int + 1);

			System.out.println(clazz.getField("_int").getInt(instance) + 1);
			//System.out.println(clazz.getField("_Integer").getInt(instance));

			System.out.println(clazz.getField("_int").get(instance));
			System.out.println(clazz.getField("_Integer").get(instance));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Singleton static public class Test1 {}
	@Singleton(declare = InjectStore.DECLARE, store = InjectStore.REQUEST, b = "BB") static public class Test2 {}
}

class A extends B {
	static public int aCount = 0;

	public A() {
		aCount++;
	}
}

class B implements I {
	static public int bCount = 0;

	public B() {
		bCount++;
	}
}

interface I {

}

class FieldTestClass {
	public byte _byte;
	public Byte _Byte;
	public int _int;
	public Integer _Integer;
	public double _double;
	public Double _Double;
}

class MyDemo {
	public List<MyDemoItem> items;
}

class MyDemoItem {

}

class ATest1<A, B> {
	class ATest2<C> {
		class ATest3<D, E extends String> {

		}

		public ATest3<A, String> new3() {
			return new ATest3<>();
		}
	}
	public ATest2<B> new2() {
		return new ATest2<>();
	}
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Singleton {
	InjectStore declare() default InjectStore.REQUEST;

	InjectStore store() default InjectStore.DECLARE;

	String a() default "a";
	String b() default "b";
}

enum InjectStore {DECLARE, REQUEST, NONE}

enum InjectType {UNKNOWN, SINGLETON, PROTOTYPE}

enum EnumDemo {
	AA("mya"), BB("myb");

	static public String lol;

	public final String msg;

	EnumDemo(String msg) {
		this.msg = msg;
	}
}

class TestDeprecatedExample {
	@TestDeprecated(replaceWith = @TestReplaceWith(value = "test", imports = {"a"}))
	public void demo() {

	}
}

@interface TestReplaceWith {
	String value();
	String[] imports() default {};
}

@interface TestDeprecated {
	TestReplaceWith replaceWith();
}

@Retention(RetentionPolicy.RUNTIME)
@interface TestAnnotation1 {
	int value();
}

@Retention(RetentionPolicy.RUNTIME)
@interface TestAnnotation2 {
	int value();
}