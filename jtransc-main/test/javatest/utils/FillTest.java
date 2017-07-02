package javatest.utils;

import com.jtransc.JTranscArrays;

import java.util.Arrays;

@SuppressWarnings("RedundantCast")
public class FillTest {
	static public void main(String[] args) {
		System.out.println("FillTest:");
		new FillTest().demo();
	}

	final int size = 32;
	final boolean[] arrayBool = new boolean[size];
	final byte[] arrayByte = new byte[size];
	final char[] arrayChar = new char[size];
	final short[] arrayShort = new short[size];
	final int[] arrayInt = new int[size];
	final long[] arrayLong = new long[size];
	final float[] arrayFloat = new float[size];
	final double[] arrayDouble = new double[size];
	final Object[] arrayObject = new Object[size];
	final Demo[] demos = new Demo[size];

	public FillTest() {
		for (int n = 0; n < size; n++) demos[n] = new Demo(n);
		//System.out.println(demos instanceof Object[]);
		//System.out.println(demos instanceof Demo[]);
		//System.out.println(demos instanceof Runnable[]); // This fails!
		System.out.println(JTranscArrays.toStringCharsAsInts(demos));

		dumpAll();
	}

	public void demo() {
		fillFull();
		fillSize(1, 0);
		fillSize(2, 0);
		fillSize(16, 77);
		fillSize(32, 999);
	}

	private void fillFull() {
		Arrays.fill(arrayBool, true);
		Arrays.fill(arrayByte, (byte) 1);
		Arrays.fill(arrayChar, (char) 1);
		Arrays.fill(arrayShort, (short) -1);
		Arrays.fill(arrayInt, (int) -1);
		Arrays.fill(arrayLong, (long) -1);
		Arrays.fill(arrayFloat, (float) -1);
		Arrays.fill(arrayDouble, (double) -1);
		Arrays.fill(arrayObject, demos[1]);
		dumpAll();
	}

	private void fillSize(int count, int add) {
		for (int n = size - count; n >= 0; n--) {
			Arrays.fill(arrayBool, n, n + count, ((n+ add) % 2) == 0);
			Arrays.fill(arrayByte, n, n + count, (byte) ((n + add) * count));
			Arrays.fill(arrayChar, n, n + count, (char) ((n + add) * count));
			Arrays.fill(arrayShort, n, n + count, (short) ((n + add) * count));
			Arrays.fill(arrayInt, n, n + count, (int) ((n + add) * count));
			Arrays.fill(arrayLong, n, n + count, (long) ((n + add) * count));
			Arrays.fill(arrayFloat, n, n + count, (float) ((n + add) * count));
			Arrays.fill(arrayDouble, n, n + count, (double) ((n + add) * count));
			Arrays.fill(arrayObject, demos[((n + add) * count) % demos.length]);
		}
		dumpAll();
	}

	private void dumpAll() {
		System.out.println("DUMP:");
		System.out.print("[Z: ");
		System.out.println(JTranscArrays.toStringCharsAsInts(arrayBool));
		System.out.print("[B: ");
		System.out.println(JTranscArrays.toStringCharsAsInts(arrayByte));
		System.out.print("[C: ");
		System.out.println(JTranscArrays.toStringCharsAsInts(arrayChar));
		System.out.print("[S: ");
		System.out.println(JTranscArrays.toStringCharsAsInts(arrayShort));
		System.out.print("[I: ");
		System.out.println(JTranscArrays.toStringCharsAsInts(arrayInt));
		System.out.print("[J: ");
		System.out.println(JTranscArrays.toStringCharsAsInts(arrayLong));
		System.out.print("[F: ");
		System.out.println(JTranscArrays.toStringCharsAsInts(arrayFloat));
		System.out.print("[D: ");
		System.out.println(JTranscArrays.toStringCharsAsInts(arrayDouble));
		System.out.print("[L: ");
		System.out.println(JTranscArrays.toStringCharsAsInts(arrayObject));
	}

	static private class Demo {
		public final int id;

		public Demo(int id) {
			this.id = id;
		}

		public String toString() {
			return "demo" + id;
		}
	}
}
