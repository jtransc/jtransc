package java.security;

public class InvalidAlgorithmParameterException extends GeneralSecurityException {
	public InvalidAlgorithmParameterException() {
		super();
	}

	public InvalidAlgorithmParameterException(String msg) {
		super(msg);
	}

	public InvalidAlgorithmParameterException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidAlgorithmParameterException(Throwable cause) {
		super(cause);
	}
}
