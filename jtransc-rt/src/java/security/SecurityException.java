package java.security;

public class SecurityException extends RuntimeException {
	public SecurityException() {
		super();
	}

	public SecurityException(String s) {
		super(s);
	}

	public SecurityException(String message, Throwable cause) {
		super(message, cause);
	}

	public SecurityException(Throwable cause) {
		super(cause);
	}
}
