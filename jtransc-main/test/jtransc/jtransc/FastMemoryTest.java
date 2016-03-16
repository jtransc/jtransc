package jtransc.jtransc;

import jtransc.FastMemory;

public class FastMemoryTest {
	static public void main(String[] args) {
		FastMemory mem = new FastMemory(1024);
		mem.setInt8(0, (byte) 255);
		mem.setInt8(1, (byte) 255);
		mem.setInt8(2, (byte) 255);
		mem.setInt8(3, (byte) 0);
		System.out.println(mem.getInt32(0));
	}
}
