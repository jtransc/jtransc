package jtransc.bug;

public class JTranscBugClInitConflictInAsm {
	static public void main(String[] args) {
		System.out.println(new Test1() != null);
		System.out.println(Test1.test != null);
	}

	static class Test1 {
		static Test1 test = new Test1();

		static public void _clinit_() {
		}
	}
}
