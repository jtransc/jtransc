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

import java.io.ObjectStreamException;

public final class Inet4Address extends InetAddress {
	final static int INADDRSZ = 4;

	Inet4Address() {
		super();
		holder().hostName = null;
		holder().address = 0;
		holder().family = IPv4;
	}

	Inet4Address(String hostName, byte addr[]) {
		holder().hostName = hostName;
		holder().family = IPv4;
		if (addr != null) {
			if (addr.length == INADDRSZ) {
				int address = addr[3] & 0xFF;
				address |= ((addr[2] << 8) & 0xFF00);
				address |= ((addr[1] << 16) & 0xFF0000);
				address |= ((addr[0] << 24) & 0xFF000000);
				holder().address = address;
			}
		}
	}

	Inet4Address(String hostName, int address) {
		holder().hostName = hostName;
		holder().family = IPv4;
		holder().address = address;
	}

	private Object writeReplace() throws ObjectStreamException {
		InetAddress inet = new InetAddress();
		inet.holder().hostName = holder().getHostName();
		inet.holder().address = holder().getAddress();
		inet.holder().family = 2;

		return inet;
	}

	public boolean isMulticastAddress() {
		return ((holder().getAddress() & 0xf0000000) == 0xe0000000);
	}

	public boolean isAnyLocalAddress() {
		return holder().getAddress() == 0;
	}

	public boolean isLoopbackAddress() {
		byte[] byteAddr = getAddress();
		return byteAddr[0] == 127;
	}

	public boolean isLinkLocalAddress() {
		int address = holder().getAddress();
		return (((address >>> 24) & 0xFF) == 169) && (((address >>> 16) & 0xFF) == 254);
	}

	public boolean isSiteLocalAddress() {
		int address = holder().getAddress();
		return (((address >>> 24) & 0xFF) == 10) || ((((address >>> 24) & 0xFF) == 172) && (((address >>> 16) & 0xF0) == 16)) || ((((address >>> 24) & 0xFF) == 192) && (((address >>> 16) & 0xFF) == 168));
	}

	public boolean isMCGlobal() {
		byte[] byteAddr = getAddress();
		return ((byteAddr[0] & 0xff) >= 224 && (byteAddr[0] & 0xff) <= 238) && !((byteAddr[0] & 0xff) == 224 && byteAddr[1] == 0 && byteAddr[2] == 0);
	}

	public boolean isMCNodeLocal() {
		return false;
	}

	public boolean isMCLinkLocal() {
		int address = holder().getAddress();
		return (((address >>> 24) & 0xFF) == 224) && (((address >>> 16) & 0xFF) == 0) && (((address >>> 8) & 0xFF) == 0);
	}

	public boolean isMCSiteLocal() {
		int address = holder().getAddress();
		return (((address >>> 24) & 0xFF) == 239) && (((address >>> 16) & 0xFF) == 255);
	}

	public boolean isMCOrgLocal() {
		int address = holder().getAddress();
		return (((address >>> 24) & 0xFF) == 239) && (((address >>> 16) & 0xFF) >= 192) && (((address >>> 16) & 0xFF) <= 195);
	}

	public byte[] getAddress() {
		int address = holder().getAddress();
		byte[] addr = new byte[INADDRSZ];

		addr[0] = (byte) ((address >>> 24) & 0xFF);
		addr[1] = (byte) ((address >>> 16) & 0xFF);
		addr[2] = (byte) ((address >>> 8) & 0xFF);
		addr[3] = (byte) (address & 0xFF);
		return addr;
	}

	public String getHostAddress() {
		return numericToTextFormat(getAddress());
	}

	public int hashCode() {
		return holder().getAddress();
	}

	public boolean equals(Object obj) {
		return (obj != null) && (obj instanceof Inet4Address) && (((InetAddress) obj).holder().getAddress() == holder().getAddress());
	}

	static String numericToTextFormat(byte[] src) {
		return (src[0] & 0xff) + "." + (src[1] & 0xff) + "." + (src[2] & 0xff) + "." + (src[3] & 0xff);
	}
}
