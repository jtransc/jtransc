package javatest.utils;

import java.util.Arrays;

public class CopyTest {
	static public void main(String[] args) {
		testOverlapping(new int[] {0, 1, 2, 3, 4, 5, 6}, 3);
	}

	private static void testOverlapping(int[] ints, int size) {
		System.out.printf("CopyTest.testOverlapping[%d]:%s\n", size, Arrays.toString(ints));
		int[] ints2 = new int[ints.length];
		for (int a = 0; a < ints.length - size; a++) {
			for (int b = 0; b < ints.length - size; b++) {
				System.arraycopy(ints, 0, ints2, 0, ints.length);
				System.arraycopy(ints2, a, ints2, b, size);
				System.out.println(Arrays.toString(ints2));
			}
		}
	}
}
