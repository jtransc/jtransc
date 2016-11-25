package big;

import com.jtransc.annotation.JTranscKeep;
import com.jtransc.reflection.JTranscReflection;
import j.ClassInfo;
import j.ProgramReflection;
import javatest.utils.Base64Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class HelloWorldTest {
	//public HelloWorldTest() {
	//}

	static public void main(String[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InstantiationException {
		//for (ClassInfo item : ProgramReflection.getAllClasses()) System.out.println(item.name + " : " + item.modifiers);

		//System.out.println("HelloWorldTest.class.getModifiers(): " + HelloWorldTest.class.getModifiers());
		System.out.println("Hello World!");
		System.out.println("Hello World!".getClass());
		Class<? extends Integer[]> intArrayClass = new Integer[0].getClass();
		System.out.println(JTranscReflection.getClassByName("java.lang.Object"));
		System.out.println(JTranscReflection.getClassByName("class.that.doesnt.exist"));
		System.out.println(intArrayClass);
		System.out.println(intArrayClass.getComponentType());
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
