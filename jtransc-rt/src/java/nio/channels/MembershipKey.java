package java.nio.channels;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;

public abstract class MembershipKey {
	protected MembershipKey() {
	}

	public abstract boolean isValid();

	public abstract void drop();

	public abstract MembershipKey block(InetAddress source) throws IOException;

	public abstract MembershipKey unblock(InetAddress source);

	public abstract MulticastChannel channel();

	public abstract InetAddress group();

	public abstract NetworkInterface networkInterface();

	public abstract InetAddress sourceAddress();
}
