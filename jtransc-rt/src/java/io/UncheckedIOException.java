package java.io;

import java.util.Objects;

public class UncheckedIOException extends RuntimeException {
	public UncheckedIOException(String message, IOException cause) {
		super(message, Objects.requireNonNull(cause));
	}

	public UncheckedIOException(IOException cause) {
		super(Objects.requireNonNull(cause));
	}

	@Override
	public IOException getCause() {
		return (IOException) super.getCause();
	}
}
