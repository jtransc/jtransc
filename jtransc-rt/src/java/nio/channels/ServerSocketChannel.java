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
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

public abstract class ServerSocketChannel extends AbstractSelectableChannel implements NetworkChannel {
	protected ServerSocketChannel(SelectorProvider provider) {
		super(provider);
	}

	public static ServerSocketChannel open() throws IOException {
		return SelectorProvider.provider().openServerSocketChannel();
	}

	public final int validOps() {
		return SelectionKey.OP_ACCEPT;
	}

	public final ServerSocketChannel bind(SocketAddress local) throws IOException {
		return bind(local, 0);
	}

	public abstract ServerSocketChannel bind(SocketAddress local, int backlog) throws IOException;

	public abstract <T> ServerSocketChannel setOption(SocketOption<T> name, T value) throws IOException;

	public abstract ServerSocket socket();

	public abstract SocketChannel accept() throws IOException;

	@Override
	public abstract SocketAddress getLocalAddress() throws IOException;
}
