package jtransc.rt.test;

import com.jtransc.JTranscBits;

public class JTranscArithmeticTest {
	static public void main(String[] args) {
		int[] values = {-1, 0, 1, 3123456, -12121212};
		System.out.println(false);
		System.out.println((byte)0);
		System.out.println((short)0);
		System.out.println((int)0);
		System.out.println('a');
		System.out.println(123456789123456789L);
		System.out.println(1f);
		System.out.println(1.0);
		for (int value : values) {
			System.out.println((long) value);
			System.out.println(shl15((byte) value));
			System.out.println(shl15((short) value));
			System.out.println(shl15((char) value));
			System.out.println(shl15(value));
			System.out.println(shl15((long) value));
			System.out.println(mul33333((byte) value));
			System.out.println(mul33333((short) value));
			System.out.println(mul33333((char) value));
			System.out.println(mul33333(value));
			System.out.println(mul33333((long) value));
		}
		System.out.println(JTranscBits.makeInt((byte) 1, (byte) 2, (byte) 3, (byte) 4));
		System.out.println(JTranscBits.makeInt((byte) 255, (byte) 255, (byte) 255, (byte) 255));
		System.out.println(JTranscBits.makeInt((byte) -127, (byte) 33, (byte) 1280, (byte) 232323));

		System.out.println("isInfinite:");
		System.out.println(java.lang.Double.isInfinite(1.0 / 0.0));
		System.out.println(java.lang.Double.isInfinite(-1.0 / 0.0));
		System.out.println(java.lang.Double.isInfinite(-1.0));
		System.out.println("Infinite:");
		System.out.println(1.0 / 0.0);
		System.out.println(-1.0 / 0.0);
		System.out.println(1.0);
		System.out.println(0.0d / 0.0);

		//System.out.println("Double literals:");
		//System.out.println(0x1.fffffffffffffP+1023);
		//System.out.println(0x1.0p-1022);

		//System.out.println(0x0.0000000000001P-1022);
		//System.out.println(5E-324);
		//System.out.println(5E-320);

		System.out.println(1023);
		System.out.println(-1022);
		System.out.println((double)-77);
		System.out.println((float)-77);
		testOperatorOrder();
	}

	static private void testOperatorOrder() {
		for (int n = 0; n < 10; n++) {
			if (((n * 3 + 2) % 5) != 1) {
				System.out.print("1");
			} else {
				System.out.print("0");
			}
			System.out.println((((n + 3 * 2) % 5) != 1) ? "1" : "0");
		}
		System.out.println();
	}

	static public int shl15(byte value) {
		return value << 15;
	}

	static public int shl15(short value) {
		return value << 15;
	}

	static public int shl15(char value) {
		return value << 15;
	}

	static public int shl15(int value) {
		return value << 15;
	}

	static public long shl15(long value) {
		return value << 15;
	}

	static public int mul33333(byte value) {
		return value * 33333;
	}

	static public int mul33333(short value) {
		return value * 33333;
	}

	static public int mul33333(char value) {
		return value * 33333;
	}

	static public int mul33333(int value) {
		return value * 33333;
	}

	static public long mul33333(long value) {
		return value * 33333;
	}
}
