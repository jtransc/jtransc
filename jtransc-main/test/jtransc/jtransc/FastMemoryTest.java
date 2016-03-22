package jtransc.jtransc;

import jtransc.FastMemory;
import jtransc.Mem;

public class FastMemoryTest {
	static public void main(String[] args) {
		testFastMemory();
		testMem();
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
}
