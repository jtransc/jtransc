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

public final class Inet4Address extends InetAddress {
	final static int INADDRSZ = 4;

	/*
	Inet4Address() {
	}

	Inet4Address(String hostName, byte addr[]) {
	}

	Inet4Address(String hostName, int address) {
	}
	*/

	native public boolean isMulticastAddress();

	native public boolean isAnyLocalAddress();

	native public boolean isLoopbackAddress();

	native public boolean isLinkLocalAddress();

	native public boolean isSiteLocalAddress();

	native public boolean isMCGlobal();

	native public boolean isMCNodeLocal();

	native public boolean isMCLinkLocal();

	native public boolean isMCSiteLocal();

	native public boolean isMCOrgLocal();

	native public byte[] getAddress();

	native public String getHostAddress();

	native public int hashCode();

	native public boolean equals(Object obj);

	static String numericToTextFormat(byte[] src) {
		return (src[0] & 0xff) + "." + (src[1] & 0xff) + "." + (src[2] & 0xff) + "." + (src[3] & 0xff);
	}
}
