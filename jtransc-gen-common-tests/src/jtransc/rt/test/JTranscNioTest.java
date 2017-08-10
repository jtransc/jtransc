package jtransc.rt.test;

import com.jtransc.JTranscBits;
import com.jtransc.util.JTranscHex;

import java.nio.*;

public class JTranscNioTest {
	static public void main(String[] args) {
		test1();
		test2();
		test2b();
		test3();
		testViewsPutIndex();
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
		for (ByteOrder byteOrder : new ByteOrder[]{ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN, ByteOrder.nativeOrder()}) {
			ByteBuffer buffer = ByteBuffer.allocate(16).order(byteOrder);
			for (int n = 0; n < 2; n++) {
				buffer.putInt(n * 4, Float.floatToRawIntBits(1f));
				FloatBuffer floatBuffer = buffer.asFloatBuffer();
				IntBuffer intBuffer = buffer.asIntBuffer();
				LongBuffer longBuffer = buffer.asLongBuffer();
				ShortBuffer shortBuffer = buffer.asShortBuffer();
				System.out.println("Endian:" + byteOrder);
				System.out.println("FLOAT:" + floatBuffer.get(n));
				System.out.println("INT:" + intBuffer.get(n));
				System.out.println("LONG:" + longBuffer.get(n));
				System.out.println("SHORT[0]:" + shortBuffer.get(n * 2 + 0));
				System.out.println("SHORT[1]:" + shortBuffer.get(n * 2 + 1));
			}
		}
	}

	static private void test2b() {
		System.out.println("JTranscNioTest.test2:");
		for (ByteOrder byteOrder : new ByteOrder[]{ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN, ByteOrder.nativeOrder()}) {
			for (int n = 0; n < 2; n++) {
				ByteBuffer buffer = ByteBuffer.allocate(16).order(byteOrder);
				buffer.putLong(n * 8, Double.doubleToRawLongBits(1.0));
				IntBuffer intBuffer = buffer.asIntBuffer();
				LongBuffer longBuffer = buffer.asLongBuffer();
				DoubleBuffer doubleBuffer = buffer.asDoubleBuffer();
				System.out.println("Endian:" + byteOrder);
				System.out.println("LONG:" + longBuffer.get(n));
				System.out.println("DOUBLE:" + doubleBuffer.get(n));
				System.out.println("INT[0]:" + intBuffer.get(n * 2 + 0));
				System.out.println("INT[1]:" + intBuffer.get(n * 2 + 1));
			}
		}
	}

	static private void testViewsPutIndex() {
		System.out.println("JTranscNioTest.test2:");
		for (ByteOrder byteOrder : new ByteOrder[]{ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN, ByteOrder.nativeOrder()}) {
			for (int n = 0; n < 2; n++) {
				final int len = 64;
				ByteBuffer buffer = ByteBuffer.allocate(len).order(byteOrder);
				ShortBuffer shortBuffer = buffer.asShortBuffer();
				CharBuffer charBuffer = buffer.asCharBuffer();
				IntBuffer intBuffer = buffer.asIntBuffer();
				LongBuffer longBuffer = buffer.asLongBuffer();
				FloatBuffer floatBuffer = buffer.asFloatBuffer();
				DoubleBuffer doubleBuffer = buffer.asDoubleBuffer();
				buffer.put(1, (byte)0x77);
				shortBuffer.put(1, (short)0x12345);
				charBuffer.put(2, (char)0x12345);
				intBuffer.put(3, 0x12345789);
				floatBuffer.put(4, (float)1.0);
				longBuffer.put(5, 0x12345789ABCDEF1L);
				doubleBuffer.put(6, 1.0);
				byte[] out = new byte[len];
				buffer.position(0);
				buffer.get(out, 0, len);
				System.out.println(byteOrder + ":" + JTranscHex.encode(out));
			}
		}
	}

	static private void test3() {
		System.out.println("JTranscNioTest.test3:");
		for (ByteOrder byteOrder : new ByteOrder[]{ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN, ByteOrder.nativeOrder()}) {
			System.out.println("JTranscNioTest.test3(" + byteOrder + "):");
			ByteBuffer buffer = ByteBuffer.allocate(512).order(byteOrder);
			ShortBuffer shortBuffer = buffer.asShortBuffer();
			IntBuffer intBuffer = buffer.asIntBuffer();
			FloatBuffer floatBuffer = buffer.asFloatBuffer();
			buffer.put(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			buffer.put(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			buffer.put(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			System.out.println("ByteBuffer");
			System.out.println(buffer.position());
			System.out.println(buffer.limit());
			System.out.println(buffer.capacity());
			for (int n = 0; n < buffer.limit(); n++) {
				System.out.print(buffer.get(n));
				System.out.print(',');
			}
			System.out.println();
			shortBuffer.put(new short[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			shortBuffer.put(new short[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			shortBuffer.put(new short[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			System.out.println("asShortBuffer");
			System.out.println(buffer.position());
			System.out.println(buffer.limit());
			System.out.println(buffer.capacity());
			for (int n = 0; n < buffer.limit(); n++) {
				System.out.print(buffer.get(n));
				System.out.print(',');
			}
			System.out.println();
			intBuffer.put(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			intBuffer.put(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			intBuffer.put(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			System.out.println("asIntBuffer");
			System.out.println(buffer.position());
			System.out.println(buffer.limit());
			System.out.println(buffer.capacity());
			for (int n = 0; n < buffer.limit(); n++) {
				System.out.print(buffer.get(n));
				System.out.print(',');
			}
			System.out.println();
			floatBuffer.put(new float[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			floatBuffer.put(new float[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			floatBuffer.put(new float[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 3, 5);
			System.out.println("asFloatBuffer");
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