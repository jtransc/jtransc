package jtransc.jtransc;

import com.jtransc.*;
import com.jtransc.io.JTranscConsole;
import com.jtransc.mem.BytesRead;

import java.util.Arrays;

public class FastMemoryTest {
	static public void main(String[] args) {
		testFastMemory();
		testMem();
		testCopyReinterpret();
		testViews();
		testRawMem();
		testRawMem();
		testBits();
		testWrap();
	}

	private static void testFastMemory() {
		JTranscConsole.log("testFastMemory:");
		FastMemory mem = FastMemory.alloc(1024);
		mem.setInt8(0, (byte) 255);
		mem.setInt8(1, (byte) 255);
		mem.setInt8(2, (byte) 255);
		mem.setInt8(3, (byte) 0);
		System.out.println(mem.getInt32(0));
	}

	private static void testMem() {
		JTranscConsole.log("testMem:");
		FastMemory mem = FastMemory.alloc(1024);
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
		JTranscConsole.log("testCopyReinterpret:");
		int[] array = {0x12345678, 0x33332222};
		byte[] data = JTranscArrays.copyReinterpret(array);
		System.out.println(Arrays.toString(array));
		System.out.println(Arrays.toString(data));
	}

	private static void testViews() {
		JTranscConsole.log("testViews:");
		FastMemory mem = FastMemory.alloc(1024);
		FastMemory4Int ints = new FastMemory4Int(mem);
		FastMemory4Float floats = new FastMemory4Float(mem);
		floats.set(0, 1f);
		System.out.println(mem.getLength());
		System.out.println(mem.getAllocatedLength());
		System.out.println(ints.getLength());
		System.out.println(floats.getLength());
		System.out.println(ints.get(0));
	}

	private static void testRawMem() {
		System.out.println("testRawMem:");
		Mem.select(FastMemory.alloc(1024));
		Mem.si32(0, 0x12345670);
		System.out.println(Mem.li8(0));
		System.out.println(Mem.li8(1));
		System.out.println(Mem.li8(2));
		System.out.println(Mem.li8(3));
		Mem.si8(0, (byte) 255);
		System.out.println(Mem.li8(0) < 0);
		testRawMem2();
	}

	private static void testRawMem2() {
		Mem.si8(0, (byte) 128);
		System.out.println(Mem.li8(0) < 0);
	}

	private static void testBits() {
		System.out.println("testBits (all true):");
		System.out.println(BytesRead.s32l(new byte[]{0, 1, 0, 0, 0}, 1) == 1);
		System.out.println(BytesRead.s32l(new byte[]{0, 2, 1, 0, 0}, 1) == 0x100 + 2);
		System.out.println(BytesRead.s32b(new byte[]{(byte) 0x71, (byte) 0x32, (byte) 0xE3, (byte) 0xF4}, 0) == 0x7132E3F4);
		System.out.println(BytesRead.s32l(new byte[]{(byte) 0x71, (byte) 0x32, (byte) 0xE3, (byte) 0xF4}, 0) == 0xF4E33271);
		System.out.println(BytesRead.u16b(new byte[]{(byte) 0xF1, (byte) 0x32}, 0) == 0xF132);
		System.out.println(BytesRead.u16l(new byte[]{(byte) 0xF1, (byte) 0x32}, 0) == 0x32F1);
		System.out.println(BytesRead.s16b(new byte[]{(byte) 0xF1, (byte) 0x32}, 0) == (int)(short)0xF132);
		System.out.println(BytesRead.s16l(new byte[]{(byte) 0xF1, (byte) 0x32}, 0) == (int)(short)0x32F1);
		System.out.println("testBits (values):");
		System.out.println(BytesRead.s32l(new byte[]{0, 1, 0, 0, 0}, 1));
		System.out.println(BytesRead.s32l(new byte[]{0, 2, 1, 0, 0}, 1));
		System.out.println(BytesRead.s32b(new byte[]{(byte) 0x71, (byte) 0x32, (byte) 0xE3, (byte) 0xF4}, 0));
		System.out.println(BytesRead.s32l(new byte[]{(byte) 0x71, (byte) 0x32, (byte) 0xE3, (byte) 0xF4}, 0));
		System.out.println(BytesRead.u16b(new byte[]{(byte) 0xF1, (byte) 0x32}, 0));
		System.out.println(BytesRead.u16l(new byte[]{(byte) 0xF1, (byte) 0x32}, 0));
		System.out.println(BytesRead.s16b(new byte[]{(byte) 0xF1, (byte) 0x32}, 0));
		System.out.println(BytesRead.s16l(new byte[]{(byte) 0xF1, (byte) 0x32}, 0));
	}

	private static void testWrap() {
		System.out.println("testWrap:");
		byte[] data = {1,2,3,4,5,6,7,8};
		FastMemory mem = FastMemory.wrap(data);
		System.out.println(mem.getInt32(0));
		System.out.println(mem.getInt8(1));
		mem.setInt8(0, 4);;
		System.out.println(mem.getInt32(0));
		for (int n = 0; n < data.length; n++) System.out.println(data[n]);
	}
}
