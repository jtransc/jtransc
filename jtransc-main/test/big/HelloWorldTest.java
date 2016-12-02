package big;

import com.jtransc.annotation.JTranscKeep;
import com.jtransc.reflection.JTranscReflection;
import com.jtransc.util.JTranscStrings;
import j.ClassInfo;
import j.ProgramReflection;
import javatest.utils.Base64Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class HelloWorldTest {
	//public HelloWorldTest() {
	//}

	static public void main(String[] args) throws Throwable {
		//for (ClassInfo item : ProgramReflection.getAllClasses()) System.out.println(item.name + " : " + item.modifiers);

		//System.out.println("HelloWorldTest.class.getModifiers(): " + HelloWorldTest.class.getModifiers());
		System.out.println("Hello World!");
		System.out.println("Hello World!".getClass());
		Class<? extends Integer[]> intArrayClass = new Integer[0].getClass();
		System.out.println(intArrayClass);
		//System.out.println("Java " + System.getProperty("java.version"));

		ArrayList<Integer> items = new ArrayList<>();
		items.add(10);
		items.add(20);
		items.add(30);
		items.addAll(Arrays.asList(40, 50, 60));

		for (Integer item : items) {
			System.out.println(item);
		}

		HashMap<String, Object> map = new HashMap<>();
		map.put("hello", "world");
		System.out.println(map.size());

		Base64Test.main(args);

		System.out.println(JTranscReflection.getClassByName("java.lang.Object"));
		System.out.println(intArrayClass.getComponentType());
		System.out.println(JTranscReflection.getClassByName("class.that.doesnt.exist"));

		System.out.println(Arrays.asList(EnumABC.class.getEnumConstants()));
	}
}

enum EnumABC {
	A, B, C;
}