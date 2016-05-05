package jtransc.jtransc;

import com.jtransc.*;

import java.util.Arrays;

public class FastMemoryTest {
	static public void main(String[] args) {
		testFastMemory();
		testMem();
		testCopyReinterpret();
		testViews();
	}

	private static void testFastMemory() {
		FastMemory mem = new FastMemory(1024);
		mem.setInt8(0, (byte) 255);
		mem.setInt8(1, (byte) 255);
		mem.setInt8(2, (byte) 255);
		mem.setInt8(3, (byte) 0);
		System.out.println(mem.getInt32(0));
	}

	private static void testMem() {
		FastMemory mem = new FastMemory(1024);
		mem.setInt8(0, (byte) 255);
		mem.setInt8(1, (byte) 255);
		mem.setInt8(2, (byte) 255);
		mem.setInt8(3, (byte) 0);
		Mem.select(mem);
		System.out.println(Mem.li32(0));
		System.out.println(Mem.li16(1));
		System.out.println(Mem.li8(3));
		System.out.println(Mem.sxi8(255));
		System.out.println(Mem.sxi8(0x80));
		System.out.println(Mem.sxi8(0x7F));
	}

	private static void testCopyReinterpret() {
		byte[] data = JTranscArrays.copyReinterpret(new int[] { 0x12345678, 0x33332222 });
		System.out.println(Arrays.toString(data));
	}

	private static void testViews() {
		FastMemory mem = new FastMemory(1024);
		FastMemory4Int ints = new FastMemory4Int(mem);
		FastMemory4Float floats = new FastMemory4Float(mem);
		floats.set(0, 1f);
		System.out.println(mem.getLength());
		System.out.println(ints.getLength());
		System.out.println(floats.getLength());
		System.out.println(ints.get(0));
	}
}
