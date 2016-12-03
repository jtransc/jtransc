package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;

public class Socket implements java.io.Closeable {
	public Socket() {

	}

	public Socket(Proxy proxy) {
	}

	//protected Socket(SocketImpl impl) throws SocketException {
	//}

	public Socket(String host, int port) throws UnknownHostException, IOException {
	}

	public Socket(InetAddress address, int port) throws IOException {
	}

	public Socket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
	}

	public Socket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
	}

	public Socket(String host, int port, boolean stream) throws IOException {
	}

	public Socket(InetAddress host, int port, boolean stream) throws IOException {
	}

	native public void connect(SocketAddress endpoint) throws IOException;

	native public void connect(SocketAddress endpoint, int timeout) throws IOException;

	native public void bind(SocketAddress bindpoint) throws IOException;

	native public InetAddress getInetAddress();

	native public InetAddress getLocalAddress();

	native public int getPort();

	native public int getLocalPort();

	native public SocketAddress getRemoteSocketAddress();

	native public SocketAddress getLocalSocketAddress();

	native public SocketChannel getChannel();

	native public InputStream getInputStream() throws IOException;

	native public OutputStream getOutputStream() throws IOException;

	native public void setTcpNoDelay(boolean on) throws SocketException;

	native public boolean getTcpNoDelay() throws SocketException;

	native public void setSoLinger(boolean on, int linger) throws SocketException;

	native public int getSoLinger() throws SocketException;

	native public void sendUrgentData(int data) throws IOException;

	native public void setOOBInline(boolean on) throws SocketException;

	native public boolean getOOBInline() throws SocketException;

	native public synchronized void setSoTimeout(int timeout) throws SocketException;

	native public synchronized int getSoTimeout() throws SocketException;

	native public synchronized void setSendBufferSize(int size) throws SocketException;

	native public synchronized int getSendBufferSize() throws SocketException;

	native public synchronized void setReceiveBufferSize(int size) throws SocketException;

	native public synchronized int getReceiveBufferSize() throws SocketException;

	native public void setKeepAlive(boolean on) throws SocketException;

	native public boolean getKeepAlive() throws SocketException;

	native public void setTrafficClass(int tc) throws SocketException;

	native public int getTrafficClass() throws SocketException;

	native public void setReuseAddress(boolean on) throws SocketException;

	native public boolean getReuseAddress() throws SocketException;

	native public synchronized void close() throws IOException;

	native public void shutdownInput() throws IOException;

	native public void shutdownOutput() throws IOException;

	native public boolean isConnected();

	native public boolean isBound();

	native public boolean isClosed();

	native public boolean isInputShutdown();

	native public boolean isOutputShutdown();

	//public static synchronized void setSocketImplFactory(SocketImplFactory fac) throws IOException {
	//}

	public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
	}
}
