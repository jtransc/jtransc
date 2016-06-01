package jtransc.bug;

public class JTranscBug41Test {
	public static void main(String[] args) {
		int a = 777;
		a = 0x80000000 - a;
		a = 0x7FFFFFFF - a;
		a = 0xFFFFFFFF - a;
		System.out.println(a);
	}
}
