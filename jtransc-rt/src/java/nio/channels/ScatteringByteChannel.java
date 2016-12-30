package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ScatteringByteChannel extends ReadableByteChannel {
	long read(ByteBuffer[] dsts, int offset, int length) throws IOException;

	long read(ByteBuffer[] dsts) throws IOException;
}
