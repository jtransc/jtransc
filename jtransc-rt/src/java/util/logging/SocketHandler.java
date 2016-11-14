package java.util.logging;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketHandler extends StreamHandler {

	// default level
	private static final String DEFAULT_LEVEL = "ALL";

	// default formatter
	private static final String DEFAULT_FORMATTER = "java.util.logging.XMLFormatter";

	// the socket connection
	private Socket socket;

	public SocketHandler() throws IOException {
		super(DEFAULT_LEVEL, null, DEFAULT_FORMATTER, null);
		initSocket(LogManager.getLogManager().getProperty(
			"java.util.logging.SocketHandler.host"), LogManager
			.getLogManager().getProperty("java.util.logging.SocketHandler.port")
		);
	}

	public SocketHandler(String host, int port) throws IOException {
		super(DEFAULT_LEVEL, null, DEFAULT_FORMATTER, null);
		initSocket(host, String.valueOf(port));
	}

	// Initialize the socket connection and prepare the output stream
	private void initSocket(String host, String port) throws IOException {
		// check the validity of the host name
		if (host == null || host.isEmpty()) throw new IllegalArgumentException("host == null || host.isEmpty()");
		// check the validity of the port number
		int p = 0;
		try {
			p = Integer.parseInt(port);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Illegal port argument");
		}
		if (p <= 0) {
			throw new IllegalArgumentException("Illegal port argument");
		}
		// establish the network connection
		try {
			this.socket = new Socket(host, p);
		} catch (IOException e) {
			getErrorManager().error("Failed to establish the network connection", e,
				ErrorManager.OPEN_FAILURE);
			throw e;
		}
		super.internalSetOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
	}

	@Override
	public void close() {
		try {
			super.close();
			if (this.socket != null) {
				this.socket.close();
				this.socket = null;
			}
		} catch (Exception e) {
			getErrorManager().error("Exception occurred when closing the socket handler", e,
				ErrorManager.CLOSE_FAILURE);
		}
	}

	@Override
	public void publish(LogRecord record) {
		super.publish(record);
		super.flush();
	}
}