package java.util.logging;

import com.jtransc.io.JTranscConsole;

public class ErrorManager {
	public static final int GENERIC_FAILURE = 0;
	public static final int WRITE_FAILURE = 1;
	public static final int FLUSH_FAILURE = 2;
	public static final int CLOSE_FAILURE = 3;
	public static final int OPEN_FAILURE = 4;
	public static final int FORMAT_FAILURE = 5;

	private static final String[] FAILURES = new String[]{
		"GENERIC_FAILURE",
		"WRITE_FAILURE", "FLUSH_FAILURE", "CLOSE_FAILURE", "OPEN_FAILURE",
		"FORMAT_FAILURE"
	};

	private boolean called;

	public ErrorManager() {
	}

	public void error(String message, Exception exception, int errorCode) {
		synchronized (this) {
			if (called) {
				return;
			}
			called = true;
		}
		JTranscConsole.error(this.getClass().getName() + ": " + FAILURES[errorCode]);
		if (message != null) {
			JTranscConsole.error("Error message - " + message);
		}
		if (exception != null) {
			JTranscConsole.error("Exception - " + exception);
		}
	}
}