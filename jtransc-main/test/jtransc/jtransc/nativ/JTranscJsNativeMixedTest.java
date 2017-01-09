package jtransc.jtransc.nativ;

import com.jtransc.annotation.JTranscCallSiteBody;
import com.jtransc.annotation.JTranscLiteralParam;
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
		JsDynamic.global("console").get("log").call(2);
		int res = JsDynamic.global("console").get("log").toInt();
		System.out.println(res);
		System.out.println(JsDynamic.global("Math").get("max").call(-4, -3).toInt() == -3);
	}

	@JTranscCallSiteBody(target = "js", value = "global[#'0]")
	static native private Object global(@JTranscLiteralParam String name);

	@JTranscCallSiteBody(target = "js", value = "#0[#'1]")
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
