package java.nio.internal;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class BufferInternalUtils {
	static public ByteBuffer getByteBuffer(Buffer buffer) {
		if (buffer instanceof ByteBufferAs) return ((ByteBufferAs)buffer).getByteBuffer();
		if (buffer instanceof ByteBuffer) return (ByteBuffer)buffer;
		throw new RuntimeException("Buffer not uses a ByteBuffer internally");
	}

	static public byte[] getByteBufferByteArray(Buffer buffer) {
		return getByteBuffer(buffer).array();
	}

	public static void reference() {
	}
}
