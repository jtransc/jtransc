package java.net;

public interface SocketOption<T> {
	String name();

	Class<T> type();
}
