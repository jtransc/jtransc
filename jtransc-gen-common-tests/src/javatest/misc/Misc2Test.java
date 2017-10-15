package javatest.misc;

import java.lang.reflect.Field;

public class Misc2Test {
	static public void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
		new Misc2Test().fieldReflection();
	}

	static private int SFIELD = 20;
	private int FIELD = 10;

	private void fieldReflection() throws NoSuchFieldException, IllegalAccessException {
		SFIELD = 20;
		Field sfield = Misc2Test.class.getDeclaredField("SFIELD");
		System.out.println("fieldReflection:static:20:");
		System.out.println("::" + sfield.getName() + "," + sfield.get(null));

		Field field = Misc2Test.class.getDeclaredField("FIELD");
		System.out.println("fieldReflection:10:");
		System.out.println("::" + field.getName() + "," + field.get(this));
	}
}
