package com.jtransc;

import org.junit.Test;

import static org.junit.Assert.*;

public class FastMemoryTest {
	@Test
	public void name() throws Exception {
		FastMemory alloc = FastMemory.alloc(1024);
		alloc.setInt8(0, 1);
		alloc.setInt8(1, 2);
		alloc.setInt8(2, 3);
		alloc.setInt8(4, 4);
		assertEquals(197121, alloc.getInt32(0));
	}
}