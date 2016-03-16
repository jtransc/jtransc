package jtransc.rt.test;

public class JTranscCloneTest {
	static public void main(String[] args) throws CloneNotSupportedException {
		MyClass mc = new MyClass();
		mc.bb = true;
		mc.b = -127;
		mc.c = 'C';
		mc.s = 32000;
		mc.i = -7;
		mc.l = 1111111111111111111L;
		mc.f = 0.3f;
		mc.d = 0.125;
		mc.ref = new Object();
		MyClass mc2 = (MyClass) mc.clone();
		System.out.println(mc.bb == mc2.bb);
		System.out.println(mc.b == mc2.b);
		System.out.println(mc.c == mc2.c);
		System.out.println(mc.s == mc2.s);
		System.out.println(mc.i == mc2.i);
		System.out.println(mc.l == mc2.l);
		System.out.println(mc.f == mc2.f);
		System.out.println(mc.d == mc2.d);
		System.out.println(mc.ref == mc2.ref);
	}

	static class MyClass implements Cloneable {
		private boolean bb;
		private byte b;
		private char c;
		private short s;
		public int i;
		public long l;
		public float f;
		public double d;
		public Object ref;

		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}
	}
}
