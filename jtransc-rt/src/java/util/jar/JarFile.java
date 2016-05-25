package java.util.jar;

import java.io.IOException;
import java.util.zip.ZipFile;

public class JarFile extends ZipFile {
	public JarFile(String name) throws IOException {
		super(name);
	}
}
