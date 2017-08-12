package java.net;

public final class DatagramPacket {
	public DatagramPacket(byte buf[], int offset, int length) {
		throw new RuntimeException("Not implemented");
	}

	public DatagramPacket(byte buf[], int length) {
		this(buf, 0, length);
	}

	public DatagramPacket(byte buf[], int offset, int length, InetAddress address, int port) {
		throw new RuntimeException("Not implemented");
	}

	public DatagramPacket(byte buf[], int offset, int length, SocketAddress address) {
		setData(buf, offset, length);
		setSocketAddress(address);
	}

	public DatagramPacket(byte buf[], int length, InetAddress address, int port) {
		this(buf, 0, length, address, port);
	}

	public DatagramPacket(byte buf[], int length, SocketAddress address) {
		this(buf, 0, length, address);
	}

	native public synchronized InetAddress getAddress();

	native public synchronized int getPort();

	native public synchronized byte[] getData();

	native public synchronized int getOffset();

	native public synchronized int getLength();

	native public synchronized void setData(byte[] buf, int offset, int length);

	native public synchronized void setAddress(InetAddress iaddr);

	native public synchronized void setPort(int iport);

	native public synchronized void setSocketAddress(SocketAddress address);

	native public synchronized SocketAddress getSocketAddress();

	native public synchronized void setData(byte[] buf);

	native public synchronized void setLength(int length);
}
