package java.lang;

public class ExceptionInInitializerError extends LinkageError {
	private Throwable exception;

	public ExceptionInInitializerError() {
		initCause(null);  // Disallow subsequent initCause
	}

	public ExceptionInInitializerError(Throwable thrown) {
		initCause(null);  // Disallow subsequent initCause
		this.exception = thrown;
	}

	public ExceptionInInitializerError(String s) {
		super(s);
		initCause(null);  // Disallow subsequent initCause
	}

	public Throwable getException() {
		return exception;
	}

	public Throwable getCause() {
		return exception;
	}
}
