package jtransc.rt.test;

import jtransc.JTranscBits;

import java.nio.ByteBuffer;

public class JTranscArithmeticTest {
	static public void main(String[] args) {
		int[] values = {-1, 0, 1, 3123456, -12121212};
		for (int value : values) {
			System.out.println(shl15((byte)value));
			System.out.println(shl15((short)value));
			System.out.println(shl15((int)value));
			System.out.println(shl15((long)value));
			System.out.println(mul33333((byte)value));
			System.out.println(mul33333((short)value));
			System.out.println(mul33333((int)value));
			System.out.println(mul33333((long)value));
		}
		System.out.println(JTranscBits.makeInt((byte)1, (byte)2, (byte)3, (byte)4));
		System.out.println(JTranscBits.makeInt((byte)255, (byte)255, (byte)255, (byte)255));
		System.out.println(JTranscBits.makeInt((byte)-127, (byte)33, (byte)1280, (byte)232323));
	}

	static public int shl15(byte value) { return value << 15; }
	static public int shl15(short value) { return value << 15; }
	static public int shl15(int value) { return value << 15; }
	static public long shl15(long value) { return value << 15; }

	static public int mul33333(byte value) { return value * 33333; }
	static public int mul33333(short value) { return value * 33333; }
	static public int mul33333(int value) { return value * 33333; }
	static public long mul33333(long value) { return value * 33333; }
}
