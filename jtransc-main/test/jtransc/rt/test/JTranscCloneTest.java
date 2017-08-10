package jtransc.rt.test;

import com.jtransc.annotation.JTranscKeep;
import javatest.JacocoFilters;

import java.lang.reflect.Field;
import java.util.Arrays;

public class JTranscCloneTest {
	static public void main(String[] args) throws CloneNotSupportedException {
		System.out.println("JTranscCloneTest.main:");
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
		System.out.println("bb,b,c,s,i,l:");
		System.out.println(mc2.bb);
		System.out.println(mc2.b);
		System.out.println(mc2.c);
		System.out.println(mc2.s);
		System.out.println(mc2.i);
		System.out.println(mc2.l);
		//System.out.println(mc2.f);
		//System.out.println(mc2.d);
		System.out.println("bb,b,c,s,i,l,f,d,ref:");
		System.out.println(mc.bb == mc2.bb);
		System.out.println(mc.b == mc2.b);
		System.out.println(mc.c == mc2.c);
		System.out.println(mc.s == mc2.s);
		System.out.println(mc.i == mc2.i);
		System.out.println(mc.l == mc2.l);
		System.out.println(mc.f == mc2.f);
		System.out.println(mc.d == mc2.d);
		System.out.println(mc.ref == mc2.ref);
		System.out.println("arrays clone");
		System.out.println(Arrays.toString(new boolean[] { true, false }.clone()));
		System.out.println(Arrays.toString(new byte[] { 1, 2, 3 }.clone()));
		System.out.println(Arrays.toString(new char[] { 'a', 'b', 'c' }.clone()));
		System.out.println(Arrays.toString(new short[] { 1, 2, 3 }.clone()));
		System.out.println(Arrays.toString(new int[] { 1, 2, 3 }.clone()));
		System.out.println(Arrays.toString(new long[] { 1, 2, 3 }.clone()));
		System.out.println(Arrays.toString(new float[] { 1, 2, 3 }.clone()));
		System.out.println(Arrays.toString(new double[] { 1, 2, 3 }.clone()));
	}

	@JTranscKeep
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
			System.out.println("MyClass.clone():");
			for (Field field : JacocoFilters.filter(getClass().getDeclaredFields())) {
				//field.getDeclaringClass().isPrimitive()
				System.out.println(field.getName());
			}
			return super.clone();
		}
	}
}
