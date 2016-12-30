package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface GatheringByteChannel extends WritableByteChannel {
	public long write(ByteBuffer[] srcs, int offset, int length) throws IOException;

	public long write(ByteBuffer[] srcs) throws IOException;
}
