package jtransc.jtransc.nativ;

import jtransc.jtransc.JTranscInternalNames;
import jtransc.jtransc.js.CustomJsRunTest;
import jtransc.rt.test.AssertionTests;

public class JTranscJsNativeMixedTest {
	static public void main(String[] args) {
		CustomJsRunTest.main(args);
		JTranscInternalNames.main(args);
		AssertionTests.main(args);
	}
}
