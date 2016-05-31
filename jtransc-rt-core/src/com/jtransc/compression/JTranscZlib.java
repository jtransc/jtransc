package com.jtransc.compression;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.compression.jzlib.InflaterInputStream;
import com.jtransc.compression.jzlib.ZInputStream;
import com.jtransc.io.JTranscConsolePrintStream;
import com.jtransc.io.JTranscIoTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;

public class JTranscZlib {
	@JTranscInline
	static private boolean hasNativeInflate() {
		return JTranscSystem.usingJTransc() && JTranscSystem.isSys();
	}

	@HaxeMethodBody(target = "sys", value = "" +
		"var u = new haxe.zip.Uncompress(-15);\n" +
		"var src = p0.getBytes();\n" +
		"var dst = haxe.io.Bytes.alloc(p1);\n" +
		"u.execute(src, 0, dst, 0);\n" +
		"u.close();\n" +
		"return HaxeByteArray.fromBytes(dst);\n"
	)
	@HaxeMethodBody("return null;")
	native static private byte[] nativeInflate(byte[] data, int outputSize);

	static public byte[] inflate(byte[] data, int outputSize) {
		try {
			if (hasNativeInflate()) {
				return nativeInflate(data, outputSize);
			} else {
				return JTranscIoTools.copy(new InflaterInputStream(new ByteArrayInputStream(data)), new ByteArrayOutputStream(outputSize)).toByteArray();
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
