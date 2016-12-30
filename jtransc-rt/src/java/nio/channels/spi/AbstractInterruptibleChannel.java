package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.Channel;
import java.nio.channels.InterruptibleChannel;

public abstract class AbstractInterruptibleChannel implements Channel, InterruptibleChannel {
	protected AbstractInterruptibleChannel() {
	}

	native public final void close() throws IOException;

	protected abstract void implCloseChannel() throws IOException;

	native public final boolean isOpen();

	native protected final void begin();

	native protected final void end(boolean completed) throws AsynchronousCloseException;
}
