package java.net;

import java.util.Enumeration;

public final class NetworkInterface {
	native public String getName();

	native public Enumeration<InetAddress> getInetAddresses();

	native public java.util.List<InterfaceAddress> getInterfaceAddresses();

	native public Enumeration<NetworkInterface> getSubInterfaces();

	native public NetworkInterface getParent();

	native public int getIndex();

	native public String getDisplayName();

	native public static NetworkInterface getByName(String name) throws SocketException;

	native public static NetworkInterface getByIndex(int index) throws SocketException;

	native public static NetworkInterface getByInetAddress(InetAddress addr) throws SocketException;

	native public static Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException;

	native public boolean isUp() throws SocketException;

	native public boolean isLoopback() throws SocketException;

	native public boolean isPointToPoint() throws SocketException;

	native public boolean supportsMulticast() throws SocketException;

	native public byte[] getHardwareAddress() throws SocketException;

	native public int getMTU() throws SocketException;

	native public boolean isVirtual();

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
