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
import java.nio.channels.ServerSocketChannel;

public class ServerSocket implements java.io.Closeable {
	public ServerSocket() throws IOException {
	}

	public ServerSocket(int port) throws IOException {
		this(port, 50, null);
	}

	public ServerSocket(int port, int backlog) throws IOException {
		this(port, backlog, null);
	}

	public ServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
		throw new RuntimeException("Not implemented ServerSocket");
	}

	public void bind(SocketAddress endpoint) throws IOException {
		bind(endpoint, 50);
	}

	public void bind(SocketAddress endpoint, int backlog) throws IOException {
	}

	public InetAddress getInetAddress() {
		return null;
	}

	public int getLocalPort() {
		return -1;
	}

	public SocketAddress getLocalSocketAddress() {
		if (!isBound())
			return null;
		return new InetSocketAddress(getInetAddress(), getLocalPort());
	}

	public Socket accept() throws IOException {
		return null;
	}

	protected final void implAccept(Socket s) throws IOException {
	}

	public void close() throws IOException {
	}

	public ServerSocketChannel getChannel() {
		return null;
	}

	public boolean isBound() {
		return false;
	}

	public boolean isClosed() {
		return true;
	}

	public synchronized void setSoTimeout(int timeout) throws SocketException {
	}

	public synchronized int getSoTimeout() throws IOException {
		return 0;
	}

	public void setReuseAddress(boolean on) throws SocketException {
	}

	public boolean getReuseAddress() throws SocketException {
		return false;
	}

	public String toString() {
		return "ServerSocket[unbound]";
	}

	//public static synchronized void setSocketFactory(SocketImplFactory fac) throws IOException {
	//}

	public synchronized void setReceiveBufferSize(int size) throws SocketException {
	}

	public synchronized int getReceiveBufferSize() throws SocketException {
		return 1024;
	}

	public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
	}
}
