package jtransc.rt.test;

import com.jtransc.annotation.JTranscKeep;
import javatest.JacocoFilters;

import java.lang.annotation.*;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unchecked", "ImplicitArrayToString", "ConstantConditions"})
public class JTranscReflectionTest {
	static public void main(String[] args) throws Throwable {
		testInvokeConstructor();
		testInvokeMethodStatic();
		testArrayType();
		annotationTest();
		assignableTest();
		annotationTypesTest();
		invokeTest();
		fieldTest();
		arrayTest();
		annotationsInConstructorTest();
		new TestDeprecatedExample().demo();
		nullArgs();
		getInterfacesTest();
		testEmptyAnnotations();
		checkClassNotFound();
		toStringArray();
		getAnnotationType();
		classNewInstance();
		testReflectFields();
		classInAnnotationTest();
	}

	interface ClassAnInt {
		int getV();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface ClassAn {
		Class<? extends ClassAnInt> value();
	}

	@ClassAn(Class2.class)
	static public class Class1 {
	}

	static public class Class2 implements ClassAnInt {
		public Class2() {
		}

		@Override
		public int getV() {
			return -99;
		}
	}

	private static void classInAnnotationTest() throws Throwable {
		System.out.println("classInAnnotationTest:");
		ClassAn an = Class1.class.getAnnotation(ClassAn.class);
		System.out.println(an.value().newInstance().getV());
	}

	@JTranscKeep
	@Retention(RetentionPolicy.RUNTIME)
	@interface TestAn {
	}

	@SuppressWarnings("unused")
	@JTranscKeep
	class RcBase {
		public int a;
		private int ap;
	}

	@SuppressWarnings("unused")
	@JTranscKeep
	class Rc1 extends RcBase {
		@TestAn public String b;
		@TestAn private String bp;
	}

	private static void testReflectFields() throws Throwable {
		System.out.println("testReflectFields:");

		List<Field> declaredFields = Arrays.asList(JacocoFilters.filter(Rc1.class.getDeclaredFields()));
		List<Field> fields = Arrays.asList(JacocoFilters.filter(Rc1.class.getFields()));

		Comparator<Field> c = new Comparator<Field>() {
			@Override
			public int compare(Field o1, Field o2) {
				return o1.getName().compareTo(o2.getName());
			}
		};
		Collections.sort(declaredFields, c);
		Collections.sort(fields, c);

		for (Field f : declaredFields) {
			System.out.println(" - getDeclaredFields: " + f.getName());
		}
		for (Field f : fields) {
			System.out.println(" - getFields: " + f.getName());
			for (Annotation annotation : f.getDeclaredAnnotations()) {
				System.out.println("   - annotation: " + annotation.annotationType().getName());
			}
		}
		System.out.println(" - annotation[a]: " + (Rc1.class.getField("a").getAnnotation(TestAn.class) != null));
		System.out.println(" - annotation[b]: " + (Rc1.class.getField("b").getAnnotation(TestAn.class) != null));

		System.out.println(" - getField: " + Rc1.class.getField("a").getName());
		System.out.println(" - getField: " + Rc1.class.getField("b").getName());
		//System.out.println(" - getField: " + RcBase.class.getField("ap").getName());
		//System.out.println(" - getField: " + Rc1.class.getField("bp").getName());
	}

	private static void getAnnotationType() {
		System.out.println("getAnnotationType:");
		Annotation[] annotations = Test2.class.getAnnotations();
		if (annotations != null && annotations.length >= 1) {
			if (annotations[0] != null && annotations[0].annotationType() != null) {
				System.out.println(annotations[0].annotationType().getName());
			}
		}
	}

	static private void testInvokeConstructor() {
		System.out.println("testInvokeConstructor:");
		try {
			ConstructorTest instance = ConstructorTest.class.getConstructor(Long.TYPE, Long.class, Double.TYPE).newInstance(1L, 2L, 3.3);
			System.out.println(instance.a);
			System.out.println(instance.b);
			System.out.println(instance.c);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	static private class ConstructorTest {
		public long a = 3L;
		public Long b = 4L;
		public double c = 10.0;

		@JTranscKeep
		public ConstructorTest(long a, Long b, double c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}
	}

	static private void testInvokeMethodStatic() {
		try {
			Method method2 = JTranscReflectionTest.class.getDeclaredMethod("testInvokeMethod2", Integer.TYPE, Boolean.TYPE);
			System.out.println(method2);
			//System.out.println(method2.slot);
			System.out.println(method2.invoke(null, 7, true));
			Method method3 = JTranscReflectionTest.class.getDeclaredMethod("testInvokeMethod3", Integer.TYPE, Boolean.TYPE, Double.TYPE);
			System.out.println(method3);
			System.out.println(method3.invoke(null, 7, false, 0.5));
			Method method4 = JTranscReflectionTest.class.getDeclaredMethod("testInvokeMethod4", Object.class, byte[].class, Object.class);
			//System.out.println(method4);
			System.out.println(Arrays.toString((short[]) method4.invoke(
				null, "hello world", new byte[]{1, 2, 3, 4}, new long[]{Long.MIN_VALUE, Long.MAX_VALUE}
			)));

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	@JTranscKeep
	static public void testInvokeMethod2(int a, boolean b) {
		System.out.println("testInvokeMethod2:" + a + ":" + b);
	}

	@SuppressWarnings("unused")
	@JTranscKeep
	static public long testInvokeMethod3(int a, boolean b, double c) {
		System.out.println("testInvokeMethodInt3:" + a + ":" + b + ":" + c);
		return Long.MAX_VALUE;
	}

	@SuppressWarnings("unused")
	@JTranscKeep
	static public short[] testInvokeMethod4(Object a, byte[] data, Object data2) {
		System.out.println("testInvokeMethod4:" + a + ":" + Arrays.toString(data) + ":" + Arrays.toString((long[]) data2));
		return new short[]{3, 4, 5, 6};
	}

	@SuppressWarnings("unused")
	@JTranscKeep
	native static public byte[] testArrayTypeMethod();

	static private void testArrayType() {
		System.out.println("testArrayType:");
		try {
			System.out.println(JTranscReflectionTest.class.getMethod("testArrayTypeMethod").getReturnType().isArray());
			//System.out.println(JTranscReflectionTest.class.getMethod("testArrayTypeMethod"));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	static private void checkClassNotFound() {
		try {
			System.out.println(Class.forName("com.unexistant.UnexistantClass"));
		} catch (ClassNotFoundException e) {
			System.out.println("Can't find class");
		}
	}

	static private void testEmptyAnnotations() {
		System.out.println("annotationsInConstructorTest2:");
		dumpClass(TestEmptyAnnotationsClass.class);
		dumpClass(Test.class);
	}

	static private void dumpAnnotations(String prefix, Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			System.out.println(prefix + ": " + annotation);
		}
	}

	static private void dumpAnnotations(String prefix, Annotation[][] annotationsList) {
		for (Annotation[] annotations : annotationsList) {
			dumpAnnotations(prefix + ".Annotation", annotations);
		}
	}

	static private void dumpClass(Class<?> clazz) {
		System.out.println("Dumping class..." + clazz);
		dumpAnnotations("Class.Annotation", clazz.getDeclaredAnnotations());
		for (Field field : JacocoFilters.filter(clazz.getDeclaredFields())) {
			System.out.println("Field: " + field);
			dumpAnnotations("Field.Annotation", field.getDeclaredAnnotations());
		}
		for (Constructor<?> constructor : JacocoFilters.filter(clazz.getDeclaredConstructors())) {
			System.out.println("Constructor: " + constructor);
			dumpAnnotations("Constructor.Annotation", constructor.getDeclaredAnnotations());
			dumpAnnotations("Constructor.Annotation", constructor.getParameterAnnotations());
		}
		for (Method method : JacocoFilters.filter(clazz.getDeclaredMethods())) {
			System.out.println("Method: " + method);
			dumpAnnotations("Method.Annotation", method.getDeclaredAnnotations());
			dumpAnnotations("Method.Annotation", method.getParameterAnnotations());
		}
	}

	@SuppressWarnings("unused")
	static class TestEmptyAnnotationsClass {
		@JTranscKeep
		public int a;

		@JTranscKeep
		public TestEmptyAnnotationsClass(int z) {
		}

		@JTranscKeep
		public void methodWithoutAnnotations(String a, int b) {
		}
	}

	static private void getInterfacesTest() {
		System.out.println("getInterfacesTest:");
		System.out.println(float.class.getSuperclass() != null);
		System.out.println(float.class.getInterfaces().length);
		System.out.println(float.class.getAnnotations().length);
	}

	@JTranscKeep
	static public char getC() {
		return 'C';
	}

	static private void invokeTest() throws Throwable {
		System.out.println("invokeTest:");
		System.out.println(JTranscReflectionTest.class.getDeclaredMethod("getC").invoke(null));
		System.out.println(JTranscReflectionTest.class.getDeclaredMethod("getC").invoke(null).getClass());
	}

	static private void annotationTypesTest() {
		System.out.println("annotationTypesTest:");
		Annotation[] declaredAnnotations = TestAnnotationTypesClass.class.getDeclaredAnnotations();
		System.out.println(declaredAnnotations != null);
		if (declaredAnnotations != null) {
			System.out.println(declaredAnnotations.length);
			if (declaredAnnotations.length >= 1) {
				System.out.println(declaredAnnotations[0] != null);
				System.out.println(TestAnnotationTypes.class.isAssignableFrom(Annotation.class));
				System.out.println(Annotation.class.isAssignableFrom(TestAnnotationTypes.class));
				System.out.println(declaredAnnotations[0] instanceof Annotation);
				System.out.println(declaredAnnotations[0] instanceof TestAnnotationTypes);
			}
		}
		System.out.println("-");
		//System.out.println(((TestAnnotationTypes)declaredAnnotations[0]).b());
		//JTranscConsole.dump(declaredAnnotations[0]);

		TestAnnotationTypes a = TestAnnotationTypesClass.class.getAnnotation(TestAnnotationTypes.class);
		System.out.println(a != null);
		if (a != null) {
			System.out.println(a.b());
			System.out.println(a.c());
			System.out.println((int) a.c());
			System.out.println(a.d());
			System.out.println(a.f());
			System.out.println(a.i());
			System.out.println(a.s());
			System.out.println(a.z());
		}
	}

	static private void classNewInstance() throws Throwable {
		System.out.println("classNewInstance:");
		MyDemoItem instance = MyDemoItem.class.newInstance();
		System.out.println(instance.a);
	}

	static private void nullArgs() throws Throwable {
		System.out.println("nullArgs:");
		System.out.println("[1]");
		Constructor<MyDemoItem> constructor = MyDemoItem.class.getConstructor((Class[]) null);
		System.out.println("[2]");
		MyDemoItem myDemoItem = constructor.newInstance((Object[]) null);
		System.out.println("[3]");
		System.out.println(myDemoItem.a);
		System.out.println("[4]");
	}

	static private void annotationsInConstructorTest() {
		System.out.println("annotationsInConstructorTest:");
		Annotation[][] parameterAnnotations = Test.class.getConstructors()[0].getParameterAnnotations();
		if (parameterAnnotations != null) {
			for (Annotation[] parameterAnnotation : parameterAnnotations) {
				if (parameterAnnotation != null) {
					System.out.println(":: " + parameterAnnotation.length);
					//JTranscConsole.dump(parameterAnnotation);
					for (Annotation annotation : parameterAnnotation) {
						System.out.println(":: " + (annotation != null));
						System.out.println(":: " + annotation.toString());
					}
				}
			}
		}

		System.out.println(Test.class.getConstructors()[0].getParameterAnnotations().length);
		System.out.println(B.class.getConstructors()[0].getParameterAnnotations().length);
		System.out.println(C.class.getConstructors()[0].getParameterAnnotations().length);
	}

	@SuppressWarnings("UnusedParameters")
	static class Test {
		@JTranscKeep
		public Test(int a, @TestAnnotation1(10) @TestAnnotation2(20) int b) {
		}
	}

	// @TODO: Test all types!
	static public void arrayTest() {
		System.out.println("arrayTest:");
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

	static public void toStringArray() {
		System.out.println("toStringArray:");
		System.out.println(new boolean[0].toString() != null);
		System.out.println(new byte[0].toString() != null);
		System.out.println(new char[0].toString() != null);
		System.out.println(new short[0].toString() != null);
		System.out.println(new int[0].toString() != null);
		System.out.println(new long[0].toString() != null);
		System.out.println(new float[0].toString() != null);
		System.out.println(new double[0].toString() != null);
		System.out.println(new String[0].toString() != null);
		System.out.println(new Class[0].toString() != null);
	}

	static public void assignableTest() {
		System.out.println("assignableTest:");
		Class[] classes = {A.class, B.class, I.class};
		A.aCount = 0;
		B.bCount = 0;
		for (Class a : classes) {
			for (Class b : classes) {
				System.out.print(a.isAssignableFrom(b));
				System.out.print(",");
				System.out.print(b.isAssignableFrom(a));
				System.out.print(",");
			}
			System.out.println();
		}
		System.out.println(A.aCount);
		System.out.println(B.bCount);
	}

	static public void annotationTest() {
		System.out.println("annotationTest:");
		Singleton test1 = Test1.class.getAnnotation(Singleton.class);
		Singleton test2 = Test2.class.getAnnotation(Singleton.class);
		System.out.println("[1]");
		System.out.println(test1 != null);
		if (test1 != null) {
			System.out.println(test1.declare());
			System.out.println(test1.store());
			System.out.println(test1.a());
			System.out.println(test1.b());
		}
		System.out.println("[2]");
		System.out.println(test2 != null);
		if (test2 != null) {
			System.out.println(test2.declare());
			System.out.println(test2.store());
			System.out.println(test2.a());
			System.out.println(test2.b());
		}
		System.out.println("[3]");
		//System.out.println(test1);
		//System.out.println(test2);

		{
			InjectStore[] enumValues = InjectStore.values();
			System.out.println(enumValues.length);
			for (InjectStore enumValue : enumValues) {
				System.out.println(enumValue.name());
			}
		}
		{
			//EnumDemo[] enumValues = EnumDemo.values();
			//System.out.println(enumValues.length);
			//for (EnumDemo enumValue : enumValues) {
			//	System.out.println(enumValue.name());
			//}
		}

		System.out.println("[4]");

		System.out.println(Enum.valueOf(EnumDemo.class, "BB").msg);
		System.out.println(InjectStore.valueOf(EnumDemo.class, "BB").msg);

		System.out.println("[5]");

		try {
			System.out.println(MyDemo.class.getField("items").getGenericType());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		System.out.println(new ATest1<Integer, String>().new2().new3().getClass().getCanonicalName());
	}


	static private void fieldTest() {
		System.out.println("fieldTest:");
		Class<FieldTestClass> clazz = FieldTestClass.class;
		FieldTestClass instance = new FieldTestClass();
		try {
			System.out.println(clazz.getField("_byte").getClass());
			System.out.println(clazz.getField("_byte").getDeclaringClass());
			System.out.println(clazz.getField("_byte").getType());
			clazz.getField("_byte").set(instance, (byte) 7);
			clazz.getField("_Byte").set(instance, (byte) 7);
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

	@Singleton
	static public class Test1 {
	}

	@Singleton(declare = InjectStore.DECLARE, store = InjectStore.REQUEST, b = "BB")
	static public class Test2 {
	}
}

@SuppressWarnings("WeakerAccess")
class A extends B {
	static public int aCount = 0;

	@JTranscKeep
	public A() {
		aCount++;
	}
}

@SuppressWarnings("WeakerAccess")
class B implements I {
	static public int bCount = 0;

	@JTranscKeep
	public B() {
		bCount++;
	}
}

@SuppressWarnings("UnusedParameters")
class C extends B {
	@JTranscKeep
	public C(int a, int b, int c) {
	}
}

interface I {

}

@SuppressWarnings({"WeakerAccess", "unused"})
class FieldTestClass {
	public byte _byte;
	public Byte _Byte;
	public int _int;
	public Integer _Integer;
	public double _double;
	@JTranscKeep
	public Double _Double;
}

@SuppressWarnings({"WeakerAccess", "unused"})
class MyDemo {
	@JTranscKeep
	public List<MyDemoItem> items;
}

class MyDemoItem {
	public int a = 3;

	@JTranscKeep
	public MyDemoItem() {
	}
}

@SuppressWarnings("ALL")
class ATest1<A, B> {
	@SuppressWarnings("TypeParameterHidesVisibleType")
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

@SuppressWarnings({"WeakerAccess", "unused"})
enum InjectType {
	UNKNOWN, SINGLETON, PROTOTYPE
}

@SuppressWarnings({"WeakerAccess", "unused"})
enum EnumDemo {
	AA("mya"), BB("myb");

	@JTranscKeep
	static public String lol;

	public final String msg;

	EnumDemo(String msg) {
		this.msg = msg;
	}
}

class TestDeprecatedExample {
	@TestDeprecated(replaceWith = @TestReplaceWith(value = "test", imports = {"a"}))
	public void demo() {
		System.out.println("new TestDeprecatedExample().demo():");
	}
}

@SuppressWarnings("unused")
@interface TestReplaceWith {
	String value();

	String[] imports() default {};
}

@SuppressWarnings("unused")
@interface TestDeprecated {
	TestReplaceWith replaceWith();
}

@Retention(RetentionPolicy.RUNTIME)
@interface TestAnnotation1 {
	@JTranscKeep
	int value();
}

@Retention(RetentionPolicy.RUNTIME)
@interface TestAnnotation2 {
	@JTranscKeep
	int value();
}

@Retention(RetentionPolicy.RUNTIME)
@interface TestAnnotationTypes {
	@JTranscKeep
	boolean z();

	@JTranscKeep
	byte b();

	@JTranscKeep
	short s();

	@JTranscKeep
	char c();

	@JTranscKeep
	int i();

	@JTranscKeep
	float f();

	@JTranscKeep
	double d();
}

@TestAnnotationTypes(
	z = true,
	b = 1,
	s = 2,
	c = 'a',
	i = 3,
	f = 1f,
	d = 2.2
)
class TestAnnotationTypesClass {

}