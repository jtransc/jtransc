package jtransc.jtransc;

public class CustomBuildTest {
	native private static boolean worked1();
	native private static boolean worked2();

	static class Demo {
		native private static boolean worked3();
	}

	native private static boolean worked_file1();
	native private static boolean worked_file2();
	native private static boolean worked_file3();

	static public void main(String[] args) {
		System.out.println(worked1());
		System.out.println(worked2());
		System.out.println(Demo.worked3());
		System.out.println(worked_file1());
		System.out.println(worked_file2());
		System.out.println(worked_file3());
	}
}
