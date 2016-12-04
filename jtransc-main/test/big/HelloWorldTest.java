package big;

import com.jtransc.reflection.JTranscReflection;
import javatest.utils.Base64Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class HelloWorldTest {
	static private final boolean FINAL_TRUE = true;
	static private final boolean FINAL_FALSE = false;
	static private boolean TRUE = true;
	//public HelloWorldTest() {
	//}

	static private boolean[] sbools = new boolean[1];
	private boolean[] ibools = new boolean[1];

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

		System.out.println(FINAL_TRUE);
		System.out.println(FINAL_FALSE);
		System.out.println(TRUE);

		sbools[0] = true;

		new Runnable() {
			@Override
			public void run() {
				System.out.println(sbools[0]);
			}
		}.run();
		System.out.println(sbools[0]);

		new HelloWorldTest().test();
	}

	private void test() {
		new InnerTest().testInner();
	}

	class InnerTest {
		private void testInner() {
			sbools[0] = true;
			ibools[0] = true;
			new Runnable() {
				@Override
				public void run() {
					System.out.println(sbools[0]);
					System.out.println(ibools[0]);
				}
			}.run();
			System.out.println(sbools[0]);
			System.out.println(ibools[0]);
		}
	}
}

enum EnumABC {
	A, B, C;
}