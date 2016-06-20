package java.net;

import java.io.InterruptedIOException;

public class SocketTimeoutException extends InterruptedIOException {
	public SocketTimeoutException() {
	}

	public SocketTimeoutException(String detailMessage) {
		super(detailMessage);
	}

	public SocketTimeoutException(Throwable cause) {
		super(null, cause);
	}

	public SocketTimeoutException(String detailMessage, Throwable cause) {
		super(detailMessage, cause);
	}
}
