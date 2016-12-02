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

		int[] ints = {0, 1, 12, 123, 1234, 12345, 123456, 1234567, 12345678};
		Locale[] locales = {Locale.ENGLISH, Locale.UK, Locale.US, Locale.FRENCH,Locale.forLanguageTag("es"),Locale.forLanguageTag("ja")};

		for (int i : ints) {
			for (Locale locale : locales) {
				String s = NumberFormat.getIntegerInstance(locale).format(i);
				System.out.println(locale.getLanguage() + ":" + s);
				if (s.length() == 5) {
					System.out.println((int) s.charAt(1));
				}
			}
		}

		String[] strings = {"", "1", "12", "123", "1234", "12345", "123456", "1234567"};
		for (String s : strings) System.out.println(JTranscStrings.join(JTranscStrings.splitInChunks(s, 3), "-"));
		for (String s : strings) System.out.println(JTranscStrings.join(JTranscStrings.splitInChunksRightToLeft(s, 3), "-"));

		/*
		//LinkedHashMap<String, Integer> stringIntegerLinkedHashMap = new LinkedHashMap<>();
		//stringIntegerLinkedHashMap.put("a", 10);
		//stringIntegerLinkedHashMap.put("b", 20);
		//stringIntegerLinkedHashMap.put("a", 11);
		//System.out.println("Hello World! : " + stringIntegerLinkedHashMap.get("a"));
		System.out.println("Hello World!");
		try {
			Thread.sleep(10000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Hello World!");
		*/
		//System.out.println(new File("c:/temp/2.bin").length());
		//JTranscConsole.log("Hello World!");

		//ProgramReflection.dynamicInvoke(0, null, null);
		//System.out.println("HelloWorldTest.class.field[10]: " + HelloWorldTest.class.getDeclaredField("a").get(null));
		//HelloWorldTest.class.getDeclaredField("a").set(null, 20);
		//System.out.println("HelloWorldTest.class.field[20]: " + HelloWorldTest.class.getDeclaredField("a").get(null));
		//System.out.println("HelloWorldTest.class.method: " + HelloWorldTest.class.getDeclaredMethod("hello").invoke(null));
		//
		//System.out.println(HelloWorldTest.class.getConstructor().newInstance().demo);
		//System.out.println(HelloWorldTest.class.getConstructor().newInstance());
		//System.out.println("####");
	}

	//public String demo = "demo";
	//
	//static public int a = 10;
	//
	//@JTranscKeep
	//static public void hello() {
	//	System.out.println("hello!");
	//}
}

enum EnumABC {
	A, B, C;
}