package jtransc.rt.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

public class JTranscReflection {
	static public void main(String[] args) {
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

	@Singleton static public class Test1 {}
	@Singleton(declare = InjectStore.DECLARE, store = InjectStore.DECLARE, b = "BB") static public class Test2 {}
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
	InjectStore declare() default InjectStore.DECLARE;

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
