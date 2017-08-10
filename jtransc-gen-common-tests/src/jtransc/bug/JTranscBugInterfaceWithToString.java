package jtransc.bug;

public class JTranscBugInterfaceWithToString {
	static public void main(String[] args) {
		System.out.println(A.class != null);
		System.out.println(B.class != null);
		System.out.println(C.class != null);
		System.out.println(D.class != null);
		System.out.println(E.class != null);
		System.out.println(F.class != null);
	}

	interface A { String toString(); }
	interface B { int hashCode(); }
	interface C { boolean equals(Object that); }
	interface D { Object clone(); }
	interface E { void finalize(); }
	interface F { void other(); }
	//interface E { void notify(); }
}
