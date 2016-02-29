package jtransc.bug;

public class JTranscBugAbstractInheritance1 {
	static public void main(String[] args) {
		System.out.println(new End().test());
	}

	static private class End extends Mid {
	}

	static private abstract class Mid extends Base implements I1 {
	}

	static private abstract class Base implements I1 {
		public int test() {
			return 7;
		}
	}

	interface I1 {
		int test();
	}
}
