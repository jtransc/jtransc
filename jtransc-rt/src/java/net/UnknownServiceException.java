package java.net;

import java.io.IOException;

public class UnknownServiceException extends IOException {
	public UnknownServiceException() {
	}

	public UnknownServiceException(String detailMessage) {
		super(detailMessage);
	}

	public UnknownServiceException(String detailMessage, Throwable cause) {
		super(detailMessage, cause);
	}
}
