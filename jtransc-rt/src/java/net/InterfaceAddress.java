package java.net;

public class InterfaceAddress {
	native public InetAddress getAddress();

	native public InetAddress getBroadcast();

	native public short getNetworkPrefixLength();

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
