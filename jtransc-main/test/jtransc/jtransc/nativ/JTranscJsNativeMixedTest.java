package jtransc.jtransc.nativ;

import com.jtransc.target.Js;
import jtransc.jtransc.JTranscInternalNamesTest;
import jtransc.jtransc.js.CustomJsRunTest;
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
	}

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
}
