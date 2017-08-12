package java.nio.file;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public interface DirectoryStream<T> extends Closeable, Iterable<T> {
	@FunctionalInterface
	interface Filter<T> {
		boolean accept(T entry) throws IOException;
	}

	@Override
	Iterator<T> iterator();
}
