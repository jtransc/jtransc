package java.nio.channels;

import com.jtransc.annotation.JTranscMethodBody;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

public class AsynchronousServerSocketChannel implements AsynchronousChannel, NetworkChannel {
	protected AsynchronousServerSocketChannel(AsynchronousChannelProvider provider) {
	}

	public final AsynchronousChannelProvider provider() {
		return null;
	}

	@JTranscMethodBody(target = "js", value = {
		"this.server = null;",
		"this.clients = [];",
		"this.handlers = [];",
		"this._opened = false;",
	})
	AsynchronousServerSocketChannel() {
		_init();
	}

	native private void _init();

	public static AsynchronousServerSocketChannel open(AsynchronousChannelGroup group) throws IOException {
		return new AsynchronousServerSocketChannel();
	}

	public static AsynchronousServerSocketChannel open() throws IOException {
		return open(null);
	}

	public final AsynchronousServerSocketChannel bind(SocketAddress local) throws IOException {
		return bind(local, 0);
	}

	@JTranscMethodBody(target = "js", value = {
		"var host = N.istr(p0), port = p1, backlog = p2;",
		"var net = require('net');",
		"var _this = this;",
		"this.server = net.createServer(function(socket) {",
		"	var client = {% CONSTRUCTOR java.nio.channels.AsynchronousSocketChannel:()V %}();",
		"	client.client = socket;",
		"	_this.clients.push(client);",
		"	if (_this.handlers.length != 0) { _this.handlers.shift()(_this.clients.shift()); }",
		"});",
		"this.server.on('error', function(err) { console.error(err); });",
		"this.server.listen(port, host, backlog, function() {",
		//"	console.log('listening!', _this.server.address());",
		"	_this.opened = true;",
		"});",
	})
	native private void _bind(String host, int port, int backlog);

	AsynchronousServerSocketChannel bind(SocketAddress local, int backlog) throws IOException {
		InetSocketAddress address = (InetSocketAddress) local;
		_bind(address.getHostName(), address.getPort(), backlog);
		return this;
	}

	public <T> AsynchronousServerSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
		// @TODO
		return this;
	}

	@Override
	native public <T> T getOption(SocketOption<T> name) throws IOException;

	@Override
	public Set<SocketOption<?>> supportedOptions() {
		return new HashSet<>();
	}

	@JTranscMethodBody(target = "js", value = {
		"var attachment = p0, handler = p1;",
		"var handlers = this.handlers;",
		"function fhandle(client) { handler{% IMETHOD java.nio.channels.CompletionHandler:completed %}(client, attachment); }",
		"if (this.clients.length != 0) { fhandle(this.clients.shift()); } else { this.handlers.push(fhandle); }"
	})
	public native <A> void accept(A attachment, CompletionHandler<AsynchronousSocketChannel, ? super A> handler);

	public native Future<AsynchronousSocketChannel> accept();

	@JTranscMethodBody(target = "js", value = "return N.str(this.server.address().address);")
	native private String _getLocalHost();

	@JTranscMethodBody(target = "js", value = "return this.server.address().port;")
	native private int _getLocalPort();

	@Override
	public SocketAddress getLocalAddress() throws IOException {
		return new InetSocketAddress(_getLocalHost(), _getLocalPort());
	}

	@Override
	@JTranscMethodBody(target = "js", value = "return this.opened;")
	native public boolean isOpen();

	@Override
	@JTranscMethodBody(target = "js", value = {
		"this.opened = false;",
		"if (this.server) this.server.close();",
	})
	native public void close() throws IOException;
}
