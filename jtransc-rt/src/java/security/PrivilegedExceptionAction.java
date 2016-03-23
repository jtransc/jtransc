package java.security;

public interface PrivilegedExceptionAction<T> {
	T run() throws Exception;
}
