package javatest.utils;

import com.jtransc.io.JTranscConsole;

import java.util.Arrays;

public class CopyTest {
	static public void main(String[] args) {
		JTranscConsole.log("CopyTest.main()");
		testOverlappingInt(new int[] {0, 1, 2, 3, 4, 5, 6}, 3);
		testOverlappingByte(new byte[] {0, 1, 2, 3, 4, 5, 6}, 3);
		testOverlappingLong(new long[] {0, 1, 2, 3, 4, 5, 6}, 3);
		JTranscConsole.log("/CopyTest.main()");
	}

	private static void testOverlappingInt(int[] array1, int size) {
		JTranscConsole.log("CopyTest.testOverlappingInt");
		//System.out.printf("CopyTest.testOverlapping[%d]:%s\n", size, Arrays.toString(array1));

		int[] array2 = new int[array1.length];
		for (int a = 0; a < array1.length - size; a++) {
			for (int b = 0; b < array1.length - size; b++) {
				System.arraycopy(array1, 0, array2, 0, array1.length);
				System.arraycopy(array2, a, array2, b, size);
				System.out.println(Arrays.toString(array2));
				//JTranscConsole.log(Arrays.toString(array2));
			}
		}
	}

	private static void testOverlappingByte(byte[] array1, int size) {
		JTranscConsole.log("CopyTest.testOverlappingByte");
		byte[] array2 = new byte[array1.length];
		for (int a = 0; a < array1.length - size; a++) {
			for (int b = 0; b < array1.length - size; b++) {
				System.arraycopy(array1, 0, array2, 0, array1.length);
				System.arraycopy(array2, a, array2, b, size);
				System.out.println(Arrays.toString(array2));
				//JTranscConsole.log(Arrays.toString(array2));
			}
		}
	}

	private static void testOverlappingLong(long[] array1, int size) {
		JTranscConsole.log("CopyTest.testOverlappingLong");
		long[] array2 = new long[array1.length];
		for (int a = 0; a < array1.length - size; a++) {
			for (int b = 0; b < array1.length - size; b++) {
				System.arraycopy(array1, 0, array2, 0, array1.length);
				System.arraycopy(array2, a, array2, b, size);
				System.out.println(Arrays.toString(array2));
				//JTranscConsole.log(Arrays.toString(array2));
			}
		}
	}
}
