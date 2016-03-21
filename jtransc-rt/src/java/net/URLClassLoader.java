package java.net;

public class URLClassLoader extends ClassLoader {
	private URL[] urls;

	public URLClassLoader(URL[] urls, ClassLoader parent) {
		super(parent);
		this.urls = urls;
	}

	public URLClassLoader(URL[] urls) {
		super();
		this.urls = urls;
	}

	public URL[] getURLs() {
		return urls;
	}

	public static URLClassLoader newInstance(final URL[] urls, final ClassLoader parent) {
		return new URLClassLoader(urls, parent);
	}

	public static URLClassLoader newInstance(final URL[] urls) {
		return new URLClassLoader(urls);
	}
}
