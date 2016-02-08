/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.net;

import java.io.IOException;
import java.util.HashMap;

public class InetAddress implements java.io.Serializable {
	static final int IPv4 = 1;
	static final int IPv6 = 2;

	private transient String canonicalHostName = null;

	InetAddress() {

	}

	public boolean isMulticastAddress() {
		return false;
	}

	public boolean isAnyLocalAddress() {
		return false;
	}

	public boolean isLoopbackAddress() {
		return false;
	}

	public boolean isLinkLocalAddress() {
		return false;
	}

	public boolean isSiteLocalAddress() {
		return false;
	}

	public boolean isMCGlobal() {
		return false;
	}

	public boolean isMCNodeLocal() {
		return false;
	}

	public boolean isMCLinkLocal() {
		return false;
	}

	public boolean isMCSiteLocal() {
		return false;
	}

	public boolean isMCOrgLocal() {
		return false;
	}

	native public boolean isReachable(int timeout) throws IOException;

	public String getHostName() {
		return getHostName(true);
	}

	native String getHostName(boolean check);

	public String getCanonicalHostName() {
		if (canonicalHostName == null) canonicalHostName = InetAddress.getHostFromNameService(this, true);
		return canonicalHostName;
	}

	native private static String getHostFromNameService(InetAddress addr, boolean check);

	public byte[] getAddress() {
		return null;
	}

	public String getHostAddress() {
		return null;
	}

	public int hashCode() {
		return -1;
	}

	public boolean equals(Object obj) {
		return false;
	}

	native public String toString();

	private static boolean addressCacheInit = false;
	static InetAddress[] unknown_array; // put THIS in cache
	private static final HashMap<String, Void> lookupTable = new HashMap<String, Void>();

	native public static InetAddress getByAddress(String host, byte[] addr) throws UnknownHostException;

	public static InetAddress getByName(String host) throws UnknownHostException {
		return InetAddress.getAllByName(host)[0];
	}

	native public static InetAddress[] getAllByName(String host) throws UnknownHostException;

	native public static InetAddress getLoopbackAddress();

	public static InetAddress getByAddress(byte[] addr) throws UnknownHostException {
		return getByAddress(null, addr);
	}

	private static InetAddress cachedLocalHost = null;
	private static long cacheTime = 0;
	private static final long maxCacheTime = 5000L;
	private static final Object cacheLock = new Object();

	native public static InetAddress getLocalHost() throws UnknownHostException;
}

