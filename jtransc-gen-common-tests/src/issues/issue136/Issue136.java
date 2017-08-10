package issues.issue136;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Issue136 {
	static public void main(String[] args) throws Throwable {
		basic();
		extended();
		extExtended();
	}

	static public void basic() throws Throwable {
		System.out.println("basic()");
		for (Field field : SomeBasicClass.class.getDeclaredFields()) {
			//System.out.println(field.getName());
			field.getGenericType();
		}
	}

	static public void extended() throws Throwable {
		System.out.println("extended()");
		// To keep class from optimization, probably it could be replaced by annotation
		SomeExtClass<String, Float> f = new SomeExtClass<>();
		f.point = 0.3f;
		f.map = new HashMap<>();
		f.mass = new String[]{"dd", "d"};

		for (Field field : SomeExtClass.class.getDeclaredFields()) {
			//System.out.println(field.getName());
			field.getGenericType();
		}
	}

	static public void extExtended() throws Throwable {
		System.out.println("extExtended()");
		for (Field field : SomeExtExtClass.class.getDeclaredFields()) {
			//System.out.println(field.getName());
			field.getGenericType();
		}
	}
}
