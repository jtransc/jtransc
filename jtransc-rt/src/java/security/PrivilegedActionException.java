package java.security;

public class PrivilegedActionException extends Exception {
	private Exception exception;

	public PrivilegedActionException(Exception exception) {
		super((Throwable) null);  // Disallow initCause
		this.exception = exception;
	}

	public Exception getException() {
		return exception;
	}

	public Throwable getCause() {
		return exception;
	}

	public String toString() {
		String s = getClass().getName();
		return (exception != null) ? (s + ": " + exception.toString()) : s;
	}
}
