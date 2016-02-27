package jtransc.bug;

// ./jtransc/bug/JTranscRegression3Test_A_.hx:7:
// characters 8-62 : Redefinition of variable this_0 in subclass is not allowed.
// Previously declared at jtransc.bug.JTranscRegression3Test_B_
public class JTranscRegression3Test {
	char a = 10;

	static public void main(String[] args) {
		new JTranscRegression3Test().main2(args);
	}

	public void main2(String[] args) {
		A a = new A();
		a.test();
		System.out.println(this.a);
		System.out.println(a.a);
		System.out.println(a.getA());
	}

	class A extends B {
		public String a = "one";
		public void test() {
			JTranscRegression3Test.this.a = 'A';
		}
	}

	class B {
		public int a = 1;
		public int getA() { return a; }
		public void test() {
			JTranscRegression3Test.this.a = 'B';
		}
	}
}
