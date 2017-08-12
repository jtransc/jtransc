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
