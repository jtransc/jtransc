package java.nio.channels;

import java.io.IOException;

public interface InterruptibleChannel extends Channel {
	void close() throws IOException;
}
