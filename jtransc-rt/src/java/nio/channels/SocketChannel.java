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
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

public abstract class SocketChannel
	extends AbstractSelectableChannel
	implements ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel
{

	/**
	 * Initializes a new instance of this class.
	 *
	 * @param  provider
	 *         The provider that created this channel
	 */
	protected SocketChannel(SelectorProvider provider) {
		super(provider);
	}

	/**
	 * Opens a socket channel.
	 *
	 * <p> The new channel is created by invoking the {@link
	 * java.nio.channels.spi.SelectorProvider#openSocketChannel
	 * openSocketChannel} method of the system-wide default {@link
	 * java.nio.channels.spi.SelectorProvider} object.  </p>
	 *
	 * @return  A new socket channel
	 *
	 * @throws IOException
	 *          If an I/O error occurs
	 */
	public static SocketChannel open() throws IOException {
		return SelectorProvider.provider().openSocketChannel();
	}

	/**
	 * Opens a socket channel and connects it to a remote address.
	 *
	 * <p> This convenience method works as if by invoking the {@link #open()}
	 * method, invoking the {@link #connect(SocketAddress) connect} method upon
	 * the resulting socket channel, passing it <tt>remote</tt>, and then
	 * returning that channel.  </p>
	 *
	 * @param  remote
	 *         The remote address to which the new channel is to be connected
	 *
	 * @return  A new, and connected, socket channel
	 *
	 * @throws  AsynchronousCloseException
	 *          If another thread closes this channel
	 *          while the connect operation is in progress
	 *
	 * @throws  ClosedByInterruptException
	 *          If another thread interrupts the current thread
	 *          while the connect operation is in progress, thereby
	 *          closing the channel and setting the current thread's
	 *          interrupt status
	 *
	 * @throws  UnresolvedAddressException
	 *          If the given remote address is not fully resolved
	 *
	 * @throws  UnsupportedAddressTypeException
	 *          If the type of the given remote address is not supported
	 *
	 * @throws  SecurityException
	 *          If a security manager has been installed
	 *          and it does not permit access to the given remote endpoint
	 *
	 * @throws  IOException
	 *          If some other I/O error occurs
	 */
	public static SocketChannel open(SocketAddress remote)
		throws IOException
	{
		SocketChannel sc = open();
		try {
			sc.connect(remote);
		} catch (Throwable x) {
			try {
				sc.close();
			} catch (Throwable suppressed) {
				x.addSuppressed(suppressed);
			}
			throw x;
		}
		assert sc.isConnected();
		return sc;
	}

	public final int validOps() {
		return SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT;
	}

	@Override
	public abstract SocketChannel bind(SocketAddress local)
		throws IOException;

	@Override
	public abstract <T> SocketChannel setOption(SocketOption<T> name, T value)
		throws IOException;

	public abstract SocketChannel shutdownInput() throws IOException;

	public abstract SocketChannel shutdownOutput() throws IOException;

	public abstract Socket socket();

	public abstract boolean isConnected();

	public abstract boolean isConnectionPending();

	public abstract boolean connect(SocketAddress remote) throws IOException;

	public abstract boolean finishConnect() throws IOException;

	public abstract SocketAddress getRemoteAddress() throws IOException;

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
