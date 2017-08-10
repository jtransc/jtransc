package jtransc.bug;

import com.jtransc.io.JTranscConsole;

public class JTranscBugClInitConflictInAsm {
	static public void main(String[] args) {
		test1();
		test2();
		test3();
		test4();
	}

	static private void test1() {
		JTranscConsole.log(new Test1() != null);
		JTranscConsole.log(Test1.test != null);
	}

	static private void test2() {
		System.out.println(new Test1() != null);
		System.out.println(Test1.test != null);
	}

	static private void test3() {
		System.out.println(true);
		System.out.println(false);
	}

	static private void test4() {
		System.out.println(new Test1() == null);
		System.out.println(Test1.test == null);
	}

	static class Test1 {
		static Test1 test = new Test1();

		static public void _clinit_() {
		}
	}
}
