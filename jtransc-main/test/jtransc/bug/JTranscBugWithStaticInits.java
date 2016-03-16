package jtransc.bug;

public class JTranscBugWithStaticInits {
	static public void main(String[] args) {
		new A();
	}

	static class A extends B {
		public A() {
			System.out.println(this.test1());
			System.out.println(super.test1());
		}

		@Override
		public String test1() {
			return "A";
		}
	}

	static class B extends C {
		public B() {
		}

		public String test1() {
			return "B";
		}
	}

	static class C {
		public String test1() {
			return "C";
		}
	}
}
