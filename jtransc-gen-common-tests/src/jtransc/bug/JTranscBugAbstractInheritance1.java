package jtransc.bug;

public class JTranscBugAbstractInheritance1 {
	static public void main(String[] args) {
		System.out.println(new End().test());
	}

	static public class End extends Mid {
	}

	static public abstract class Mid extends Base implements I1 {
	}

	static public abstract class Base implements I1 {
		public int test() {
			return 7;
		}
	}

	public interface I1 {
		int test();
	}
}
