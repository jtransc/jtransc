package jtransc.rt.test;

import jtransc.JTranscBits;

import java.nio.*;

public class JTranscNioTest {
	static public void main(String[] args) {
		test1();
		test2();
	}

	static private void test1() {
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
}