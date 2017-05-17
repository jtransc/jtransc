package jtransc.jtransc;

import com.jtransc.JTranscArrays;
import com.jtransc.simd.MutableFloat32x4;
import com.jtransc.simd.MutableMatrixFloat32x4x4;

import java.util.Arrays;

public class SimdTest {
	static public void main(String[] args) {
		testByteArray();
		testMutableFloat32x4();
		testMatrix();
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

	static private void testMutableFloat32x4() {
		MutableFloat32x4 r = MutableFloat32x4.create();
		MutableFloat32x4 v1 = MutableFloat32x4.create(1f, 2f, 3f, 4f);
		MutableFloat32x4 v2 = MutableFloat32x4.create(-10f, -20f, -30f, -40f);
		r.setToAdd(v1, v2);
		r.setToAbs(r);
		System.out.println(r.toString());
	}

	static private void testMatrix() {
		MutableMatrixFloat32x4x4 a = MutableMatrixFloat32x4x4.create();
		a.setTo(
			1f, 9f, 1f, 7f,
			3f, 2f, 4f, 5f,
			3f, 7f, 3f, 3f,
			3f, 8f, 4f, 4f
		);
		MutableMatrixFloat32x4x4 b = MutableMatrixFloat32x4x4.create();
		b.setTo(
			2f, 3f, 4f, 5f,
			2f, 3f, 4f, 5f,
			2f, 3f, 4f, 5f,
			2f, 3f, 4f, 5f
		);

		for (int n = 0; n < 100; n++) {
			a.setToMul44(a, b);
		}
	}
}
