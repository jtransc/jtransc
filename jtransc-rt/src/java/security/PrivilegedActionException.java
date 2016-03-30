package java.security;

public class PrivilegedActionException extends Exception {
	public PrivilegedActionException(Exception exception) {
		super(exception);
	}
}
