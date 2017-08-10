package jtransc.jtransc;

import com.jtransc.annotation.haxe.HaxeMethodBody;

public class UseMinitemplatesTest {
	static public void main(String[] args) {
		callStaticMethod();
		callMethod1(new Class2());
		callMethod2(new Class2());
	}

	@HaxeMethodBody("{% SMETHOD jtransc.jtransc.UseMinitemplatesTest:methodToExecute1 %}(1);")
	native static private void callStaticMethod();

	@HaxeMethodBody("p0{% IMETHOD jtransc.jtransc.UseMinitemplatesTest$Class2:method1 %}();")
	native static private void callMethod1(Class2 clazz);

	@HaxeMethodBody("p0{% IMETHOD jtransc.jtransc.UseMinitemplatesTest$Class2:method2 %}();")
	native static private void callMethod2(Class2 clazz);

	//@JTranscKeep
	private static void methodToExecute1(int value) {
		System.out.println("methodToExecute1:" + value);
	}

	static private class Class1 {
		//@JTranscKeep
		public void method1() {
			System.out.println("Class1.method1");
		}
	}

	static private class Class2 extends Class1 {
		//@JTranscKeep
		public void method2() {
			System.out.println("Class1.method2");
		}
	}
}
