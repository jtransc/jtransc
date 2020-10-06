package com.jtransc.compression;

import com.jtransc.annotation.JTranscInvisible;


import com.jtransc.compression.jzlib.InflaterInputStream;
import com.jtransc.io.JTranscIoTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.Inflater;

@JTranscInvisible
public class JTranscZlib {
	static private boolean hasNativeInflate() {
		//return JTranscSystem.usingJTransc() && JTranscSystem.isSys();
		return false;
	}

	native static private byte[] nativeInflate(byte[] data, int outputSize);

	static public byte[] inflate(byte[] data, int outputSize) {
		try {
			if (hasNativeInflate()) {
				return nativeInflate(data, outputSize);
			} else {
				//return JTranscIoTools.copy(new java.util.zip.InflaterInputStream(new ByteArrayInputStream(data)), new ByteArrayOutputStream(outputSize)).toByteArray();
				return JTranscIoTools.copy(new InflaterInputStream(new ByteArrayInputStream(data), true), new ByteArrayOutputStream(outputSize)).toByteArray();
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	static public byte[] uncompress(byte[] data, int outputSize) {
		try {
			if (hasNativeInflate()) {
				return nativeInflate(data, outputSize);
			} else {
				Inflater inflater = new Inflater(false);
				inflater.setInput(data);
				byte[] out = new byte[outputSize];
				int result = inflater.inflate(out);
				return out;
				//return JTranscIoTools.copy(new java.util.zip.InflaterInputStream(new ByteArrayInputStream(data), false), new ByteArrayOutputStream(outputSize)).toByteArray();
				//return JTranscIoTools.copy(new InflaterInputStream(new ByteArrayInputStream(data), false), new ByteArrayOutputStream(outputSize)).toByteArray();
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
