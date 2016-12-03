package com.jtransc.crypto;

import java.util.zip.CRC32;

public class JTranscHash {
	static public int crc32(byte[] data, int offset, int length) {
		CRC32 crc32 = new CRC32();
		crc32.reset();
		crc32.update(data, offset, length);
		return (int) crc32.getValue();
	}
}
