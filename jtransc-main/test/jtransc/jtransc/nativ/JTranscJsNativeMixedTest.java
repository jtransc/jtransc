package jtransc.jtransc.nativ;

import com.jtransc.target.Js;
import jtransc.jtransc.JTranscInternalNames;
import jtransc.jtransc.js.CustomJsRunTest;
import jtransc.rt.test.AssertionTests;

public class JTranscJsNativeMixedTest {
	static public void main(String[] args) {
		CustomJsRunTest.main(args);
		JTranscInternalNames.main(args);
		AssertionTests.main(args);
		rawTest();
	}

	static private void rawTest() {
		System.out.println(Js.i_raw("10 + 7"));
		Js.v_raw("console.log(-333);");
	}
}
