package java.nio.file;

import java.net.URI;

public final class Paths {
	private Paths() {
	}

	native public static Path get(String first, String... more);

	native public static Path get(URI uri);
}
