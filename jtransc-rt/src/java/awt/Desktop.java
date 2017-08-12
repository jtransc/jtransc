package java.awt;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class Desktop {
	public enum Action {
		OPEN,
		EDIT,
		PRINT,
		MAIL,
		BROWSE
	}

	static private Desktop instance;

	private Desktop() {

	}

	public static synchronized Desktop getDesktop() {
		if (instance == null) {
			instance = new Desktop();
		}
		return instance;
	}

	native public static boolean isDesktopSupported();

	native public boolean isSupported(Action action);

	native public void open(File file) throws IOException;

	native public void edit(File file) throws IOException;

	native public void print(File file) throws IOException;

	native public void browse(URI uri) throws IOException;

	native public void mail() throws IOException;

	native public void mail(URI mailtoURI) throws IOException;
}
