package java.net;

import java.util.Objects;

public class InetSocketAddress extends SocketAddress {
	private String hostname;
	private int port;

	public InetSocketAddress(int port) {
		this("0.0.0.0", port);
	}

	public InetSocketAddress(InetAddress addr, int port) {
		this(addr.getHostName(), port);
	}

	public InetSocketAddress(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}

	public static InetSocketAddress createUnresolved(String host, int port) {
		return new InetSocketAddress(host, port);
	}

	public final int getPort() {
		return port;
	}

	public final InetAddress getAddress() {
		try {
			return Inet4Address.getByName(hostname);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	public final String getHostName() {
		return hostname;
	}

	public final String getHostString() {
		return hostname;
	}

	public final boolean isUnresolved() {
		return true;
	}

	@Override
	public String toString() {
		return "InetSocketAddress(" + hostname + "," + port + ")";
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == null || !(obj instanceof InetSocketAddress)) return false;
		InetSocketAddress that = (InetSocketAddress) obj;
		return Objects.equals(this.hostname, that.getHostName()) && this.port == that.getPort();
	}

	/**
	 * Returns a hashcode for this socket address.
	 *
	 * @return a hash code value for this socket address.
	 */
	@Override
	public final int hashCode() {
		return Objects.hashCode(hostname) + port;
	}
}
