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

import com.jtransc.annotation.JTranscMethodBody;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AsynchronousSocketChannel implements AsynchronousByteChannel, NetworkChannel {
	protected AsynchronousSocketChannel(AsynchronousChannelProvider provider) {
	}

	public final AsynchronousChannelProvider provider() {
		return null;
	}

	public static AsynchronousSocketChannel open(AsynchronousChannelGroup group) throws IOException {
		return new AsynchronousSocketChannel();
	}

	public static AsynchronousSocketChannel open()
		throws IOException {
		return open(null);
	}

	AsynchronousSocketChannel() {
	}

	@Override
	native public AsynchronousSocketChannel bind(SocketAddress local) throws IOException;

	@Override
	native public <T> AsynchronousSocketChannel setOption(SocketOption<T> name, T value) throws IOException;

	@Override
	native public <T> T getOption(SocketOption<T> name) throws IOException;

	@Override
	native public Set<SocketOption<?>> supportedOptions();

	native public AsynchronousSocketChannel shutdownInput() throws IOException;

	native public AsynchronousSocketChannel shutdownOutput() throws IOException;

	native public SocketAddress getRemoteAddress() throws IOException;

	@JTranscMethodBody(target = "js", value = {
		"var address = N.istr(p0), port = p1, attachment = p2, handler = p3;",
		"var net = require('net');",
		"this.client = new net.Socket();",
		"this.client.on('error', function() {",
		"	handler{% IMETHOD java.nio.channels.CompletionHandler:failed %}({{ JC_COMMA }}N.createRuntimeException({{ JC_COMMA }}'error'), attachment);",
		"});",
		"this.client.connect(port, address, function() {",
		"	handler{% IMETHOD java.nio.channels.CompletionHandler:completed %}({{ JC_COMMA }}null, attachment);",
		"});",
	})
	native private <A> void _connect(String address, int port, A attachment, CompletionHandler<Void, ? super A> handler);

	public <A> void connect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
		InetSocketAddress address = (InetSocketAddress) remote;
		_connect(address.getHostName(), address.getPort(), attachment, handler);
	}

	native public Future<Void> connect(SocketAddress remote);

	@Override
	public final <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
		read(dst, 0L, TimeUnit.MILLISECONDS, attachment, handler);
	}

	@Override
	native public Future<Integer> read(ByteBuffer dst);

	native private <A> void _read(byte[] data, int offset, int len, double timeout, A attachment, CompletionHandler<Integer, ? super A> handler);

	@JTranscMethodBody(target = "js", value = {
		"var data = p0.data, offset = p1, len = p2, timeout = p3, attachment = p4, handler = p5;",
		"this.client.write(new Buffer(new Int8Array(data.buffer, offset, len)), function() {",
		"	handler{% IMETHOD java.nio.channels.CompletionHandler:completed %}({{ JC_COMMA }}N.boxInt(len), attachment);",
		"});",
	})
	native private <A> void _write(byte[] data, int offset, int len, double timeout, A attachment, CompletionHandler<Integer, ? super A> handler);

	native public <A> void read(ByteBuffer[] dsts, int offset, int length, long timeout, TimeUnit unit, A attachment, CompletionHandler<Long, ? super A> handler);

	public <A> void read(ByteBuffer dst, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler) {
		_read(dst.array(), dst.arrayOffset(), dst.limit(), (double) unit.toMillis(timeout), attachment, handler);
	}

	public <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler) {
		_write(src.array(), src.arrayOffset(), src.limit(), (double) unit.toMillis(timeout), attachment, handler);
	}

	@Override
	public final <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
		write(src, 0L, TimeUnit.MILLISECONDS, attachment, handler);
	}

	@Override
	native public Future<Integer> write(ByteBuffer src);

	native public <A> void write(ByteBuffer[] srcs, int offset, int length, long timeout, TimeUnit unit, A attachment, CompletionHandler<Long, ? super A> handler);

	native public SocketAddress getLocalAddress() throws IOException;

	@Override
	native public boolean isOpen();

	@Override
	@JTranscMethodBody(target = "js", value = {
		"this.client.end()",
	})
	native public void close() throws IOException;
}
