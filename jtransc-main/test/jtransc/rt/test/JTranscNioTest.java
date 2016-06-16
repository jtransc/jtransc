package jtransc.rt.test;

import com.jtransc.JTranscBits;

import java.nio.*;

public class JTranscNioTest {
	static public void main(String[] args) {
		test1();
		test2();
		test3();
	}

	static private void test1() {
		System.out.println("JTranscNioTest.test1:");
		ByteBuffer allocate = ByteBuffer.allocate(16);
		allocate.put(0, (byte) 4);
		allocate.put(1, (byte) 63);
		allocate.put(2, (byte) -128);
		allocate.put(3, (byte) 0);
		allocate.put(4, (byte) 0);

		System.out.println(JTranscBits.makeInt(allocate.get(1), allocate.get(2), allocate.get(3), allocate.get(4)));
		System.out.println(JTranscBits.makeInt(allocate.get(4), allocate.get(3), allocate.get(2), allocate.get(1)));

		System.out.println(allocate.getInt(1));
		System.out.println(allocate.getInt(0));
		System.out.println(allocate.get(0));
		System.out.println(allocate.get(1));
		System.out.println(allocate.get(2));
		System.out.println(allocate.get(3));
		System.out.println(allocate.get(4));
		System.out.println(allocate.getFloat(1));
		allocate.putFloat(1, 2f);
		System.out.println(allocate.get(0));
		System.out.println(allocate.get(1));
		System.out.println(allocate.get(2));
		System.out.println(allocate.get(3));
		System.out.println(allocate.get(4));
	}

	static private void test2() {
		System.out.println("JTranscNioTest.test2:");
		ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(0, 0x3F800000);
		FloatBuffer floatBuffer = buffer.asFloatBuffer();
		IntBuffer intBuffer = buffer.asIntBuffer();
		ShortBuffer shortBuffer = buffer.asShortBuffer();
		System.out.println(floatBuffer.get(0));
		System.out.println(intBuffer.get(0));
		System.out.println(shortBuffer.get(0));
		System.out.println(shortBuffer.get(1));
	}

	static private void test3() {
		System.out.println("JTranscNioTest.test3:");
		for (ByteOrder byteOrder : new ByteOrder[]{ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN, ByteOrder.nativeOrder()}) {
			ByteBuffer buffer = ByteBuffer.allocate(512).order(byteOrder);
			ShortBuffer shortBuffer = buffer.asShortBuffer();
			IntBuffer intBuffer = buffer.asIntBuffer();
			FloatBuffer floatBuffer = buffer.asFloatBuffer();
			buffer.put(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			buffer.put(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			buffer.put(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			System.out.println(buffer.position());
			System.out.println(buffer.limit());
			System.out.println(buffer.capacity());
			for (int n = 0; n < buffer.limit(); n++) {
				System.out.print(buffer.get(n));
				System.out.print(',');
			}
			System.out.println();
			shortBuffer.put(new short[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			shortBuffer.put(new short[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			shortBuffer.put(new short[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			System.out.println(buffer.position());
			System.out.println(buffer.limit());
			System.out.println(buffer.capacity());
			for (int n = 0; n < buffer.limit(); n++) {
				System.out.print(buffer.get(n));
				System.out.print(',');
			}
			System.out.println();
			intBuffer.put(new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			intBuffer.put(new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			intBuffer.put(new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			System.out.println(buffer.position());
			System.out.println(buffer.limit());
			System.out.println(buffer.capacity());
			for (int n = 0; n < buffer.limit(); n++) {
				System.out.print(buffer.get(n));
				System.out.print(',');
			}
			System.out.println();
			floatBuffer.put(new float[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			floatBuffer.put(new float[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			floatBuffer.put(new float[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			System.out.println(buffer.position());
			System.out.println(buffer.limit());
			System.out.println(buffer.capacity());
			for (int n = 0; n < buffer.limit(); n++) {
				System.out.print(buffer.get(n));
				System.out.print(',');
			}
			System.out.println();
		}
	}
}