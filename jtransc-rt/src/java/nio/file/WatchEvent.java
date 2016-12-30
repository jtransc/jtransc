package java.nio.file;

public interface WatchEvent<T> {
	interface Kind<T> {
		String name();

		Class<T> type();
	}

	interface Modifier {
		String name();
	}

	Kind<T> kind();

	int count();

	T context();
}
