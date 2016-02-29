package jtransc.bug;

public class JTranscBugArrayGetClass {
	static public void main(String[] args) {
		System.out.println((new boolean[1]).getClass().getName());
		System.out.println((new byte[1]).getClass().getName());
		System.out.println((new char[1]).getClass().getName());
		System.out.println((new short[1]).getClass().getName());
		System.out.println((new int[] { 1, 2, 3 }).getClass().getName());
		System.out.println((new long[1]).getClass().getName());
		System.out.println((new float[1]).getClass().getName());
		System.out.println((new double[1]).getClass().getName());
		System.out.println((new Object[0]).getClass().getName());
		System.out.println((new String[0]).getClass().getName());
		System.out.println((new JTranscBugArrayGetClass[0]).getClass().getName());
		//System.out.println((new String[2][2]).getClass().getName());
	}
}
