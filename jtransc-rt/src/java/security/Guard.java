package java.security;

public interface Guard {
	void checkGuard(Object object) throws SecurityException;
}
