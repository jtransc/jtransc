package jtransc.bug;

public class JTranscBugAbstractInheritance2 {
	static public void main(String[] args) {
		System.out.println(new End() != null);
	}

	static private class End extends Mid {
	}

	static private abstract class Mid {
	}
}
