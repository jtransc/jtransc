package jtransc.jtransc.nativ;

import com.jtransc.annotation.JTranscCallSiteBody;
import com.jtransc.annotation.JTranscLiteralParam;
import com.jtransc.annotation.JTranscNativeName;
import com.jtransc.annotation.JTranscUnboxParam;
import com.jtransc.target.Js;
import com.jtransc.target.js.JsDynamic;
import jtransc.jtransc.JTranscInternalNamesTest;
import jtransc.jtransc.js.CustomJsRunTest;
import jtransc.jtransc.js.MixedJsKotlin;
import jtransc.ref.MethodBodyReferencesTest;
import jtransc.rt.test.AssertionTests;
import testservice.ITestService;

import java.util.ServiceLoader;

public class JTranscJsNativeMixedTest {
	static public void main(String[] args) {
		rawTest();
		servicesTest();
		CustomJsRunTest.main(args);
		JTranscInternalNamesTest.main(args);
		AssertionTests.main(args);
		JTranscReinterpretArrays.main(args);
		MethodBodyReferencesTest.main(args);
		customAnnotationTest();
		MixedJsKotlin.main(args);
		call(access(global("console"), "log"), 1);
		JsDynamic.global("console").call("log", 2);
		int res = JsDynamic.global("console").get("log").toInt();
		System.out.println(res);
		System.out.println(JsDynamic.global("Math").call("max", -4, -3).toInt() == -3);
		System.out.println(JsDynamic.global().get("Date").newInstance(1234567).call("getTime").toInt());

		Global.global.console.log(1);
		Global.global.console.log("hello");

		JsDynamic obj = JsDynamic.newEmptyObject();
		obj.set("test", 777);
		Global.global.console.log(obj.get("test"));

		JsDynamic array = JsDynamic.newEmptyArray();
		array.set(0, 999);
		Global.global.console.log(array.get(0));

		Global.global.console.log(JsDynamic.raw("Math.max(1, 7 * 3)"));
		Console.logHelloWorldStatic(1, "demo");
		Global.global.console.logHelloWorld(2, "test");
	}

	@JTranscNativeName("global")
	public abstract static class JsGlobal {
		public Console console;
		public String name;

		//public native void alert(String s);
		//public native void alert(int s);
	}

	@JTranscNativeName("global")
	public static class Global {
		public static JsGlobal global;
	}

	@JTranscNativeName("Console")
	public abstract static class Console {
		native public void log(@JTranscUnboxParam Object a);

		static public void logHelloWorldStatic(int v, String s) {
			Global.global.console.log("HELLO WORLD" + v + s);
		}

		public void logHelloWorld(int v, String s) {
			log("HELLO WORLD" + v + s);
		}
	}

	@JTranscCallSiteBody(target = "js", value = "global#.0")
	static native private Object global(@JTranscLiteralParam String name);

	@JTranscCallSiteBody(target = "js", value = "#0#.1")
	static native private Object access(Object obj, @JTranscLiteralParam String name);

	@JTranscCallSiteBody(target = "js", value = "#0(#1)")
	static native private Object call(Object obj, @JTranscUnboxParam Object v);

	private static void servicesTest() {
		ServiceLoader<ITestService> load = ServiceLoader.load(ITestService.class);
		System.out.println("Services:");
		for (ITestService testService : load) {
			System.out.println(testService.test());
		}
		System.out.println("/Services:");
	}

	static private void rawTest() {
		System.out.println(Js.i_raw("10 + 7"));
		Js.v_raw("console.log(-333);");
	}

	static private void customAnnotationTest() {
		System.out.println(getCustomAnnotationTest() ? "OK!" : "ERROR!");
	}

	@JsMethodBody("return true;")
	static private boolean getCustomAnnotationTest() {
		return false;
	}
}

@interface JsMethodBody {
	String value();
}
