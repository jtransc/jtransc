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

package java.nio.channels;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

public abstract class DatagramChannel extends AbstractSelectableChannel implements ByteChannel, ScatteringByteChannel, GatheringByteChannel, MulticastChannel {

	protected DatagramChannel(SelectorProvider provider) {
		super(provider);
	}

	public static DatagramChannel open() throws IOException {
		return SelectorProvider.provider().openDatagramChannel();
	}

	public static DatagramChannel open(ProtocolFamily family) throws IOException {
		return SelectorProvider.provider().openDatagramChannel(family);
	}

	public final int validOps() {
		return SelectionKey.OP_READ | SelectionKey.OP_WRITE;
	}

	public abstract DatagramChannel bind(SocketAddress local) throws IOException;

	public abstract <T> DatagramChannel setOption(SocketOption<T> name, T value) throws IOException;

	public abstract DatagramSocket socket();

	public abstract boolean isConnected();

	public abstract DatagramChannel connect(SocketAddress remote) throws IOException;

	public abstract DatagramChannel disconnect() throws IOException;

	public abstract SocketAddress getRemoteAddress() throws IOException;

	public abstract SocketAddress receive(ByteBuffer dst) throws IOException;

	public abstract int send(ByteBuffer src, SocketAddress target) throws IOException;

	public abstract int read(ByteBuffer dst) throws IOException;

	public abstract long read(ByteBuffer[] dsts, int offset, int length) throws IOException;

	public final long read(ByteBuffer[] dsts) throws IOException {
		return read(dsts, 0, dsts.length);
	}

	public abstract int write(ByteBuffer src) throws IOException;

	public abstract long write(ByteBuffer[] srcs, int offset, int length) throws IOException;

	public final long write(ByteBuffer[] srcs) throws IOException {
		return write(srcs, 0, srcs.length);
	}

	@Override
	public abstract SocketAddress getLocalAddress() throws IOException;

}
