package com.jtransc.util;

import com.jtransc.FastMemory;

public class JTranscMemAllocator {
	public final int totalSize;
	private int offset = 0;

	public JTranscMemAllocator(int totalSize) {
		this.totalSize = totalSize;
	}

	public JTranscMemAllocator(FastMemory mem) {
		this.totalSize = mem.getLength();
	}

	public int malloc(int size) {
		if (offset + size > totalSize) throw new RuntimeException("No enough space!");
		try {
			return offset;
		} finally {
			offset += size;
		}
	}

	// @TODO: No free! So this is much easier to implement. To create simple layouts along with Mem.
	//public void free(int offset) {
	//}
}
