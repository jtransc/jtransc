package java.security.cert;

import java.security.GeneralSecurityException;

public class CertificateException extends GeneralSecurityException {
	public CertificateException() {
		super();
	}

	public CertificateException(String msg) {
		super(msg);
	}

	public CertificateException(String message, Throwable cause) {
		super(message, cause);
	}

	public CertificateException(Throwable cause) {
		super(cause);
	}
}
