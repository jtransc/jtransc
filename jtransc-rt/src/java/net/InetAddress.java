/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.net;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * An Internet Protocol (IP) address. This can be either an IPv4 address or an IPv6 address, and
 * in practice you'll have an instance of either {@code Inet4Address} or {@code Inet6Address} (this
 * class cannot be instantiated directly). Most code does not need to distinguish between the two
 * families, and should use {@code InetAddress}.
 * <p>
 * <p>An {@code InetAddress} may have a hostname (accessible via {@code getHostName}), but may not,
 * depending on how the {@code InetAddress} was created.
 * <p>
 * <h4>IPv4 numeric address formats</h4>
 * <p>The {@code getAllByName} method accepts IPv4 addresses in the "decimal-dotted-quad" form only:
 * <ul>
 * <li>{@code "1.2.3.4"} - 1.2.3.4
 * </ul>
 * <p>
 * <h4>IPv6 numeric address formats</h4>
 * <p>The {@code getAllByName} method accepts IPv6 addresses in the following forms (this text
 * comes from <a href="http://www.ietf.org/rfc/rfc2373.txt">RFC 2373</a>, which you should consult
 * for full details of IPv6 addressing):
 * <ul>
 * <li><p>The preferred form is {@code x:x:x:x:x:x:x:x}, where the 'x's are the
 * hexadecimal values of the eight 16-bit pieces of the address.
 * Note that it is not necessary to write the leading zeros in an
 * individual field, but there must be at least one numeral in every
 * field (except for the case described in the next bullet).
 * Examples:
 * <pre>
 *     FEDC:BA98:7654:3210:FEDC:BA98:7654:3210
 *     1080:0:0:0:8:800:200C:417A</pre>
 * </li>
 * <li>Due to some methods of allocating certain styles of IPv6
 * addresses, it will be common for addresses to contain long strings
 * of zero bits.  In order to make writing addresses containing zero
 * bits easier a special syntax is available to compress the zeros.
 * The use of "::" indicates multiple groups of 16-bits of zeros.
 * The "::" can only appear once in an address.  The "::" can also be
 * used to compress the leading and/or trailing zeros in an address.
 * <p>
 * For example the following addresses:
 * <pre>
 *     1080:0:0:0:8:800:200C:417A  a unicast address
 *     FF01:0:0:0:0:0:0:101        a multicast address
 *     0:0:0:0:0:0:0:1             the loopback address
 *     0:0:0:0:0:0:0:0             the unspecified addresses</pre>
 * may be represented as:
 * <pre>
 *     1080::8:800:200C:417A       a unicast address
 *     FF01::101                   a multicast address
 *     ::1                         the loopback address
 *     ::                          the unspecified addresses</pre>
 * </li>
 * <li><p>An alternative form that is sometimes more convenient when dealing
 * with a mixed environment of IPv4 and IPv6 nodes is
 * {@code x:x:x:x:x:x:d.d.d.d}, where the 'x's are the hexadecimal values of
 * the six high-order 16-bit pieces of the address, and the 'd's are
 * the decimal values of the four low-order 8-bit pieces of the
 * address (standard IPv4 representation).  Examples:
 * <pre>
 *     0:0:0:0:0:0:13.1.68.3
 *     0:0:0:0:0:FFFF:129.144.52.38</pre>
 * or in compressed form:
 * <pre>
 *     ::13.1.68.3
 *     ::FFFF:129.144.52.38</pre>
 * </li>
 * </ul>
 * <p>Scopes are given using a trailing {@code %} followed by the scope id, as in
 * {@code 1080::8:800:200C:417A%2} or {@code 1080::8:800:200C:417A%en0}.
 * See <a href="https://www.ietf.org/rfc/rfc4007.txt">RFC 4007</a> for more on IPv6's scoped
 * address architecture.
 * <p>
 * <p>Additionally, for backwards compatibility, IPv6 addresses may be surrounded by square
 * brackets.
 * <p>
 * <h4>DNS caching</h4>
 * <p>In Android 4.0 (Ice Cream Sandwich) and earlier, DNS caching was performed both by
 * InetAddress and by the C library, which meant that DNS TTLs could not be honored correctly.
 * In later releases, caching is done solely by the C library and DNS TTLs are honored.
 *
 * @see Inet4Address
 */
//@SuppressWarnings("all")
public class InetAddress implements Serializable {
	public static final int AF_INET = 0;
	public static final int AF_INET6 = 1;
	public static final int AF_UNIX = 2;
	public static final int AF_UNSPEC = 3;

	/**
	 * Our Java-side DNS cache.
	 */
	private int family;

	byte[] ipaddress;

	String hostName;

	/**
	 * Used by the DatagramSocket.disconnect implementation.
	 *
	 * @hide internal use only
	 */
	public static final InetAddress UNSPECIFIED = new InetAddress(AF_UNSPEC, null, null);

	/**
	 * Constructs an {@code InetAddress}.
	 * <p>
	 * Note: this constructor is for subclasses only.
	 */
	InetAddress(int family, byte[] ipaddress, String hostName) {
		this.family = family;
		this.ipaddress = ipaddress;
		this.hostName = hostName;
	}

	/**
	 * Compares this {@code InetAddress} instance against the specified address
	 * in {@code obj}. Two addresses are equal if their address byte arrays have
	 * the same length and if the bytes in the arrays are equal.
	 *
	 * @param obj the object to be tested for equality.
	 * @return {@code true} if both objects are equal, {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof InetAddress)) {
			return false;
		}
		return Arrays.equals(this.ipaddress, ((InetAddress) obj).ipaddress);
	}

	/**
	 * Returns the IP address represented by this {@code InetAddress} instance
	 * as a byte array. The elements are in network order (the highest order
	 * address byte is in the zeroth element).
	 *
	 * @return the address in form of a byte array.
	 */
	public byte[] getAddress() {
		return ipaddress.clone();
	}

	/**
	 * Converts an array of byte arrays representing raw IP addresses of a host
	 * to an array of InetAddress objects.
	 *
	 * @param rawAddresses the raw addresses to convert.
	 * @param hostName     the hostname corresponding to the IP address.
	 * @return the corresponding InetAddresses, appropriately sorted.
	 */
	//private static InetAddress[] bytesToInetAddresses(byte[][] rawAddresses, String hostName)
	//	throws UnknownHostException {
	//	// Convert the byte arrays to InetAddresses.
	//	InetAddress[] returnedAddresses = new InetAddress[rawAddresses.length];
	//	for (int i = 0; i < rawAddresses.length; i++) {
	//		returnedAddresses[i] = makeInetAddress(rawAddresses[i], hostName);
	//	}
	//	return returnedAddresses;
	//}

	/**
	 * Gets all IP addresses associated with the given {@code host} identified
	 * by name or literal IP address. The IP address is resolved by the
	 * configured name service. If the host name is empty or {@code null} an
	 * {@code UnknownHostException} is thrown. If the host name is a literal IP
	 * address string an array with the corresponding single {@code InetAddress}
	 * is returned.
	 *
	 * @param host the hostname or literal IP string to be resolved.
	 * @return the array of addresses associated with the specified host.
	 * @throws UnknownHostException if the address lookup fails.
	 */
	public static InetAddress[] getAllByName(String host) throws UnknownHostException {
		//return getAllByNameImpl(host).clone();
		try {
			return new InetAddress[]{parseNumericAddress(host)};
		} catch (Throwable t) {
			throw new UnknownHostException(host);
		}
	}

	/**
	 * Returns the InetAddresses for {@code host}. The returned array is shared
	 * and must be cloned before it is returned to application code.
	 */
	//private static InetAddress[] getAllByNameImpl(String host) throws UnknownHostException {
	//	if (host == null || host.isEmpty()) {
	//		return loopbackAddresses();
	//	}
//
	//	// Is it a numeric address?
	//	InetAddress result = parseNumericAddressNoThrow(host);
	//	if (result != null) {
	//		result = disallowDeprecatedFormats(host, result);
	//		if (result == null) {
	//			throw new UnknownHostException("Deprecated IPv4 address format: " + host);
	//		}
	//		return new InetAddress[] { result };
	//	}
//
	//	return lookupHostByName(host).clone();
	//}

	//private static InetAddress makeInetAddress(byte[] bytes, String hostName) throws UnknownHostException {
	//	if (bytes.length == 4) {
	//		return new Inet4Address(bytes, hostName);
	//	} else if (bytes.length == 16) {
	//		return new Inet6Address(bytes, hostName, 0);
	//	} else {
	//		throw badAddressLength(bytes);
	//	}
	//}

	//private static InetAddress disallowDeprecatedFormats(String address, InetAddress inetAddress) {
	//	// Only IPv4 addresses are problematic.
	//	if (!(inetAddress instanceof Inet4Address) || address.indexOf(':') != -1) {
	//		return inetAddress;
	//	}
	//	// If inet_pton(3) can't parse it, it must have been a deprecated format.
	//	// We need to return inet_pton(3)'s result to ensure that numbers assumed to be octal
	//	// by getaddrinfo(3) are reinterpreted by inet_pton(3) as decimal.
	//	return Libcore.os.inet_pton(AF_INET, address);
	//}

	//private static InetAddress parseNumericAddressNoThrow(String address) {
	//	// Accept IPv6 addresses (only) in square brackets for compatibility.
	//	if (address.startsWith("[") && address.endsWith("]") && address.indexOf(':') != -1) {
	//		address = address.substring(1, address.length() - 1);
	//	}
	//	StructAddrinfo hints = new StructAddrinfo();
	//	hints.ai_flags = AI_NUMERICHOST;
	//	InetAddress[] addresses = null;
	//	try {
	//		addresses = Libcore.os.getaddrinfo(address, hints);
	//	} catch (GaiException ignored) {
	//	}
	//	return (addresses != null) ? addresses[0] : null;
	//}

	/**
	 * Returns the address of a host according to the given host string name
	 * {@code host}. The host string may be either a machine name or a dotted
	 * string IP address. If the latter, the {@code hostName} field is
	 * determined upon demand. {@code host} can be {@code null} which means that
	 * an address of the loopback interface is returned.
	 *
	 * @param host the hostName to be resolved to an address or {@code null}.
	 * @return the {@code InetAddress} instance representing the host.
	 * @throws UnknownHostException if the address lookup fails.
	 */
	public static InetAddress getByName(String host) throws UnknownHostException {
		//return getAllByNameImpl(host)[0];
		throw new RuntimeException("Not implemented");
	}

	/**
	 * Returns the numeric representation of this IP address (such as "127.0.0.1").
	 */
	public String getHostAddress() {
		//return Libcore.os.getnameinfo(this, NI_NUMERICHOST); // Can't throw.
		return String.format("%d.%d.%d.%d", ipaddress[0], ipaddress[1], ipaddress[2], ipaddress[3]);
	}

	/**
	 * Returns the host name corresponding to this IP address. This may or may not be a
	 * fully-qualified name. If the IP address could not be resolved, the numeric representation
	 * is returned instead (see {@link #getHostAddress}).
	 */
	public String getHostName() {
		//if (hostName == null) {
		//	try {
		//		hostName = getHostByAddrImpl(this).hostName;
		//	} catch (UnknownHostException ex) {
		//		hostName = getHostAddress();
		//	}
		//}
		//return hostName;
		return getHostAddress();
	}

	/**
	 * Returns the fully qualified hostname corresponding to this IP address.
	 */
	public String getCanonicalHostName() {
		//try {
		//	return getHostByAddrImpl(this).hostName;
		//} catch (UnknownHostException ex) {
		//	return getHostAddress();
		//}
		return getHostAddress();
	}

	public static InetAddress getLocalHost() throws UnknownHostException {
		//String host = Libcore.os.uname().nodename;
		//return lookupHostByName(host)[0];
		return Inet4Address.LOOPBACK;
	}

	/**
	 * Gets the hashcode of the represented IP address.
	 *
	 * @return the appropriate hashcode value.
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(ipaddress);
	}

	/**
	 * Resolves a hostname to its IP addresses using a cache.
	 *
	 * @param host the hostname to resolve.
	 * @return the IP addresses of the host.
	 */
	//private static InetAddress[] lookupHostByName(String host) throws UnknownHostException {
	//	BlockGuard.getThreadPolicy().onNetwork();
	//	// Do we have a result cached?
	//	Object cachedResult = addressCache.get(host);
	//	if (cachedResult != null) {
	//		if (cachedResult instanceof InetAddress[]) {
	//			// A cached positive result.
	//			return (InetAddress[]) cachedResult;
	//		} else {
	//			// A cached negative result.
	//			throw new UnknownHostException((String) cachedResult);
	//		}
	//	}
	//	try {
	//		StructAddrinfo hints = new StructAddrinfo();
	//		hints.ai_flags = AI_ADDRCONFIG;
	//		hints.ai_family = AF_UNSPEC;
	//		// If we don't specify a socket type, every address will appear twice, once
	//		// for SOCK_STREAM and one for SOCK_DGRAM. Since we do not return the family
	//		// anyway, just pick one.
	//		hints.ai_socktype = SOCK_STREAM;
	//		InetAddress[] addresses = Libcore.os.getaddrinfo(host, hints);
	//		// TODO: should getaddrinfo set the hostname of the InetAddresses it returns?
	//		for (InetAddress address : addresses) {
	//			address.hostName = host;
	//		}
	//		addressCache.put(host, addresses);
	//		return addresses;
	//	} catch (GaiException gaiException) {
	//		// If the failure appears to have been a lack of INTERNET permission, throw a clear
	//		// SecurityException to aid in debugging this common mistake.
	//		// http://code.google.com/p/android/issues/detail?id=15722
	//		if (gaiException.getCause() instanceof ErrnoException) {
	//			if (((ErrnoException) gaiException.getCause()).errno == EACCES) {
	//				throw new SecurityException("Permission denied (missing INTERNET permission?)", gaiException);
	//			}
	//		}
	//		// Otherwise, throw an UnknownHostException.
	//		String detailMessage = "Unable to resolve host \"" + host + "\": " + Libcore.os.gai_strerror(gaiException.error);
	//		addressCache.putUnknownHost(host, detailMessage);
	//		throw gaiException.rethrowAsUnknownHostException(detailMessage);
	//	}
	//}


	//private static InetAddress getHostByAddrImpl(InetAddress address) throws UnknownHostException {
	//	BlockGuard.getThreadPolicy().onNetwork();
	//	try {
	//		String hostname = Libcore.os.getnameinfo(address, NI_NAMEREQD);
	//		return makeInetAddress(address.ipaddress.clone(), hostname);
	//	} catch (GaiException gaiException) {
	//		throw gaiException.rethrowAsUnknownHostException();
	//	}
	//}

	/**
	 * Returns a string containing the host name (if available) and host address.
	 * For example: {@code "www.google.com/74.125.224.115"} or {@code "/127.0.0.1"}.
	 * <p>
	 * <p>IPv6 addresses may additionally include an interface name or scope id.
	 * For example: {@code "www.google.com/2001:4860:4001:803::1013%eth0"} or
	 * {@code "/2001:4860:4001:803::1013%2"}.
	 */
	@Override
	public String toString() {
		return (hostName == null ? "" : hostName) + "/" + getHostAddress();
	}

	/**
	 * Returns true if the string is a valid numeric IPv4 or IPv6 address (such as "192.168.0.1").
	 * This copes with all forms of address that Java supports, detailed in the {@link InetAddress}
	 * class documentation.
	 *
	 * @hide used by frameworks/base to ensure that a getAllByName won't cause a DNS lookup.
	 */
	public static boolean isNumeric(String address) {
		//InetAddress inetAddress = parseNumericAddressNoThrow(address);
		//return inetAddress != null && disallowDeprecatedFormats(address, inetAddress) != null;
		try {
			parseNumericAddress(address);
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * Returns an InetAddress corresponding to the given numeric address (such
	 * as {@code "192.168.0.1"} or {@code "2001:4860:800d::68"}).
	 * This method will never do a DNS lookup. Non-numeric addresses are errors.
	 *
	 * @throws IllegalArgumentException if {@code numericAddress} is not a numeric address
	 * @hide used by frameworks/base's NetworkUtils.numericToInetAddress
	 */
	public static InetAddress parseNumericAddress(String numericAddress) {
		if (numericAddress == null || numericAddress.isEmpty()) return getLoopbackAddress();

		if (numericAddress.contains(".")) {
			String[] parts = numericAddress.split(".");
			byte[] bytes = new byte[parts.length];
			return new Inet4Address(bytes, numericAddress);
		} else {
			throw new RuntimeException("Don't know how to handle address");
		}

		//InetAddress result = parseNumericAddressNoThrow(numericAddress);
		//result = disallowDeprecatedFormats(numericAddress, result);
		//if (result == null) {
		//	throw new IllegalArgumentException("Not a numeric address: " + numericAddress);
		//}
		//return result;
	}

	private static InetAddress[] loopbackAddresses() {
		//return new InetAddress[] { Inet6Address.LOOPBACK, Inet4Address.LOOPBACK };
		return new InetAddress[]{Inet4Address.LOOPBACK};
	}

	public static InetAddress getLoopbackAddress() {
		//return Inet6Address.LOOPBACK;
		return Inet4Address.LOOPBACK;
	}

	public boolean isAnyLocalAddress() {
		return false;
	}

	public boolean isLinkLocalAddress() {
		return false;
	}

	public boolean isLoopbackAddress() {
		return false;
	}

	public boolean isMCGlobal() {
		return false;
	}

	public boolean isMCLinkLocal() {
		return false;
	}

	public boolean isMCNodeLocal() {
		return false;
	}

	public boolean isMCOrgLocal() {
		return false;
	}

	public boolean isMCSiteLocal() {
		return false;
	}

	public boolean isMulticastAddress() {
		return false;
	}

	public boolean isSiteLocalAddress() {
		return false;
	}

	public boolean isReachable(int timeout) throws IOException {
		//return isReachable(null, 0, timeout);
		return true;
	}

	//public boolean isReachable(NetworkInterface networkInterface, final int ttl, final int timeout) throws IOException {
	//	if (ttl < 0 || timeout < 0) {
	//		throw new IllegalArgumentException("ttl < 0 || timeout < 0");
	//	}
//
	//	// The simple case.
	//	if (networkInterface == null) {
	//		return isReachable(this, null, timeout);
	//	}
//
	//	// Try each NetworkInterface in parallel.
	//	// Use a thread pool Executor?
	//	List<InetAddress> sourceAddresses = Collections.list(networkInterface.getInetAddresses());
	//	if (sourceAddresses.isEmpty()) {
	//		return false;
	//	}
	//	final InetAddress destinationAddress = this;
	//	final CountDownLatch latch = new CountDownLatch(sourceAddresses.size());
	//	final AtomicBoolean isReachable = new AtomicBoolean(false);
	//	for (final InetAddress sourceAddress : sourceAddresses) {
	//		new Thread() {
	//			@Override public void run() {
	//				try {
	//					if (isReachable(destinationAddress, sourceAddress, timeout)) {
	//						isReachable.set(true);
	//						// Wake the main thread so it can return success without
	//						// waiting for any other threads to time out.
	//						while (latch.getCount() > 0) {
	//							latch.countDown();
	//						}
	//					}
	//				} catch (IOException ignored) {
	//				}
	//				latch.countDown();
	//			}
	//		}.start();
	//	}
	//	try {
	//		latch.await();
	//	} catch (InterruptedException ignored) {
	//		Thread.currentThread().interrupt(); // Leave the interrupted bit set.
	//	}
	//	return isReachable.get();
	//}

	private boolean isReachable(InetAddress destination, InetAddress source, int timeout) throws IOException {
		//// TODO: try ICMP first (http://code.google.com/p/android/issues/detail?id=20106)
		//FileDescriptor fd = IoBridge.socket(true);
		//boolean reached = false;
		//try {
		//	if (source != null) {
		//		IoBridge.bind(fd, source, 0);
		//	}
		//	IoBridge.connect(fd, destination, 7, timeout);
		//	reached = true;
		//} catch (IOException e) {
		//	if (e.getCause() instanceof ErrnoException) {
		//		// "Connection refused" means the IP address was reachable.
		//		reached = (((ErrnoException) e.getCause()).errno == ECONNREFUSED);
		//	}
		//}
//
		//IoBridge.closeSocket(fd);
//
		//return reached;
		return true;
	}

	/**
	 * Equivalent to {@code getByAddress(null, ipAddress)}. Handy for addresses with
	 * no associated hostname.
	 */
	public static InetAddress getByAddress(byte[] ipAddress) throws UnknownHostException {
		return getByAddress(null, ipAddress, 0);
	}

	/**
	 * Returns an {@code InetAddress} corresponding to the given network-order
	 * bytes {@code ipAddress} and {@code scopeId}.
	 * <p>
	 * <p>For an IPv4 address, the byte array must be of length 4.
	 * For IPv6, the byte array must be of length 16. Any other length will cause an {@code
	 * UnknownHostException}.
	 * <p>
	 * <p>No reverse lookup is performed. The given {@code hostName} (which may be null) is
	 * associated with the new {@code InetAddress} with no validation done.
	 * <p>
	 * <p>(Note that numeric addresses such as {@code "127.0.0.1"} are names for the
	 * purposes of this API. Most callers probably want {@link #getAllByName} instead.)
	 *
	 * @throws UnknownHostException if {@code ipAddress} is null or the wrong length.
	 */
	public static InetAddress getByAddress(String hostName, byte[] ipAddress) throws UnknownHostException {
		return getByAddress(hostName, ipAddress, 0);
	}

	private static InetAddress getByAddress(String hostName, byte[] ipAddress, int scopeId) throws UnknownHostException {
		if (ipAddress == null) {
			throw new UnknownHostException("ipAddress == null");
		}
		if (ipAddress.length == 4) {
			return new Inet4Address(ipAddress.clone(), hostName);
		}
		//else if (ipAddress.length == 16) {
		//	// First check to see if the address is an IPv6-mapped
		//	// IPv4 address. If it is, then we can make it a IPv4
		//	// address, otherwise, we'll create an IPv6 address.
		//	if (isIPv4MappedAddress(ipAddress)) {
		//		return new Inet4Address(ipv4MappedToIPv4(ipAddress), hostName);
		//	} else {
		//		return new Inet6Address(ipAddress.clone(), hostName, scopeId);
		//	}
		//}
		else {
			throw badAddressLength(ipAddress);
		}
	}

	private static UnknownHostException badAddressLength(byte[] bytes) throws UnknownHostException {
		throw new UnknownHostException("Address is neither 4 or 16 bytes: " + Arrays.toString(bytes));
	}

	private static boolean isIPv4MappedAddress(byte[] ipAddress) {
		// Check if the address matches ::FFFF:d.d.d.d
		// The first 10 bytes are 0. The next to are -1 (FF).
		// The last 4 bytes are varied.
		if (ipAddress == null || ipAddress.length != 16) {
			return false;
		}
		for (int i = 0; i < 10; i++) {
			if (ipAddress[i] != 0) {
				return false;
			}
		}
		if (ipAddress[10] != -1 || ipAddress[11] != -1) {
			return false;
		}
		return true;
	}

	private static byte[] ipv4MappedToIPv4(byte[] mappedAddress) {
		byte[] ipv4Address = new byte[4];
		for (int i = 0; i < 4; i++) {
			ipv4Address[i] = mappedAddress[12 + i];
		}
		return ipv4Address;
	}

	//private static final ObjectStreamField[] serialPersistentFields = {
	//	new ObjectStreamField("address", int.class),
	//	new ObjectStreamField("family", int.class),
	//	new ObjectStreamField("hostName", String.class),
	//};

	//private void writeObject(ObjectOutputStream stream) throws IOException {
	//	ObjectOutputStream.PutField fields = stream.putFields();
	//	if (ipaddress == null) {
	//		fields.put("address", 0);
	//	} else {
	//		fields.put("address", Memory.peekInt(ipaddress, 0, ByteOrder.BIG_ENDIAN));
	//	}
	//	fields.put("family", family);
	//	fields.put("hostName", hostName);
//
	//	stream.writeFields();
	//}

	//private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
	//	ObjectInputStream.GetField fields = stream.readFields();
	//	int addr = fields.get("address", 0);
	//	ipaddress = new byte[4];
	//	Memory.pokeInt(ipaddress, 0, addr, ByteOrder.BIG_ENDIAN);
	//	hostName = (String) fields.get("hostName", null);
	//	family = fields.get("family", 2);
	//}
//
	///*
	// * The spec requires that if we encounter a generic InetAddress in
	// * serialized form then we should interpret it as an Inet4Address.
	// */
	//private Object readResolve() throws ObjectStreamException {
	//	return new Inet4Address(ipaddress, hostName);
	//}
}
