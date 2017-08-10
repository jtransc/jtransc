package javatest;

public class StaticInit1 {
	static public void main(String[] args) {
		new A();
	}

	static public class A {
		static {
			System.out.println("A[1]");
			System.out.println(new B());
			System.out.println("A[2]");
			System.out.println(new B());
		}
	}

	static public class B {
		static {
			System.out.println("B[1]");
			System.out.println("B[2]");
			System.out.println(new A());
		}
	}
}
