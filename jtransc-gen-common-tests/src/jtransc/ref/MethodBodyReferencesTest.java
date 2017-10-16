package jtransc.ref;

import com.jtransc.annotation.JTranscMethodBody;

public class MethodBodyReferencesTest {
	static public void main(String[] args) {
		demo();
		System.out.println("MethodBodyReferencesTestJs:" + exists("jtransc.ref.MethodBodyReferencesTestJs"));
		System.out.println("MethodBodyReferencesTestCpp:" + exists("jtransc.ref.MethodBodyReferencesTestCpp"));
		System.out.println("MethodBodyReferencesTestJvm:" + exists("jtransc.ref.MethodBodyReferencesTestJvm"));
	}

	static private boolean exists(String fqname) {
		try {
			Class.forName(fqname);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	@JTranscMethodBody(target = "js", value = "{% SMETHOD jtransc.ref.MethodBodyReferencesTestJs:test %}({{ JC }});")
	@JTranscMethodBody(target = "cpp", value = "{% SMETHOD jtransc.ref.MethodBodyReferencesTestCpp:test %}();")
	static private void demo() {
		MethodBodyReferencesTestJvm.test();
	}
}

class MethodBodyReferencesTestJs {
	static public void test() {
	}
}

class MethodBodyReferencesTestCpp {
	static public void test() {
	}
}

class MethodBodyReferencesTestJvm {
	static public void test() {
	}
}
