package jtransc.jtransc;

import com.jtransc.JTranscArrays;

import java.util.Arrays;

public class SimdTest {
	static public void main(String[] args) {
		testByteArray();
	}

	static private void testByteArray() {
		byte[] result = new byte[6];
		byte[] a = {0, 1, 2, 3, 4, 127};
		byte[] b = {-1, -1, 3, -1, 7, 77};
		JTranscArrays.add(result.length, result, 0, a, 0, b, 0);
		System.out.println(Arrays.toString(result));
		JTranscArrays.sub(result.length, result, 0, a, 0, b, 0);
		System.out.println(Arrays.toString(result));
		JTranscArrays.mixUnsigned(result.length, result, 0, a, 0, b, 0, 0.5);
		System.out.println(Arrays.toString(result));
		JTranscArrays.addUnsignedClamped(result.length, result, 0, a, 0, b, 0);
		System.out.println(Arrays.toString(result));
	}
}
