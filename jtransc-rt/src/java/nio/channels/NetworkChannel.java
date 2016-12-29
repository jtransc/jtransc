package java.nio.channels;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.util.Set;

public interface NetworkChannel extends Channel {
	NetworkChannel bind(SocketAddress local) throws IOException;

	SocketAddress getLocalAddress() throws IOException;

	<T> NetworkChannel setOption(SocketOption<T> name, T value) throws IOException;

	<T> T getOption(SocketOption<T> name) throws IOException;

	Set<SocketOption<?>> supportedOptions();
}
