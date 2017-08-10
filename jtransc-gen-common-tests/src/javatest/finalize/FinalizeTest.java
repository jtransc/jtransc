package javatest.finalize;

public class FinalizeTest {
	static public void main(String[] args) {
		System.out.println("FinalizeTest.main");

		test();

		for (int n = 0; n < 10; n++) {
			System.gc();
			System.runFinalization();
			if (A.ranFinalizers()) break;
			try {
				Thread.sleep(200L * (n + 1));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (A.ranFinalizers()) {
			System.out.println("Called finalizers");
		} else {
			System.out.println("NOT Called finalizers");
		}

		System.out.println("/FinalizeTest.main");
	}

	static private void test() {
		new A();
	}
}

class A {
	public B b = new B();

	static public StringBuffer out = new StringBuffer();

	static public boolean ranFinalizers() {
		String str = out.toString();
		return str.contains("A") && str.contains("B");
	}

	@Override
	protected void finalize() throws Throwable {
		out.append("A");
	}
}

class B {
	@Override
	protected void finalize() throws Throwable {
		A.out.append("B");
	}
}
