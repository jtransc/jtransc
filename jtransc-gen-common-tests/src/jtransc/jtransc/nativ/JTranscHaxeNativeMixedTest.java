package jtransc.jtransc.nativ;

import jtransc.annotation.ClassMembersTest;
import jtransc.annotation.MethodBodyTest;
import jtransc.jtransc.*;

public class JTranscHaxeNativeMixedTest {

	static public void main(String[] args) {
		CustomBuildTest.main(args);
		HaxeNativeCallTest.main(args);
		ClassMembersTest.main(args);
		MethodBodyTest.main(args);
		JTranscSystemTest.main(args);
		UseMinitemplatesTest.main(args);
		JTranscInternalNamesTest.main(args);
		JTranscReinterpretArrays.main(args);
	}
}
