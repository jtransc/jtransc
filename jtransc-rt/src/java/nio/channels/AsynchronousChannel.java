package java.nio.channels;

import java.io.IOException;

public interface AsynchronousChannel extends Channel {
	@Override
	void close() throws IOException;
}
