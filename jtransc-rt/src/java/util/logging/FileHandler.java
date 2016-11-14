package java.util.logging;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Hashtable;

public class FileHandler extends StreamHandler {
	private static final String LCK_EXT = ".lck";
	private static final int DEFAULT_COUNT = 1;
	private static final int DEFAULT_LIMIT = 0;
	private static final boolean DEFAULT_APPEND = false;
	private static final String DEFAULT_PATTERN = "%h/java%u.log";
	private static final Hashtable<String, FileLock> allLocks = new Hashtable<String, FileLock>();
	private int count;
	private int limit;
	private boolean append;
	private String pattern;
	private LogManager manager;
	private MeasureOutputStream output;
	private File[] files;
	FileLock lock = null;
	String fileName = null;
	int uniqueID = -1;

	public FileHandler() throws IOException {
		init(null, null, null, null);
	}

	// init properties
	private void init(String p, Boolean a, Integer l, Integer c)
		throws IOException {
		// check access
		manager = LogManager.getLogManager();
		manager.checkAccess();
		initProperties(p, a, l, c);
		initOutputFiles();
	}

	private void initOutputFiles() throws FileNotFoundException, IOException {
		while (true) {
			// try to find a unique file which is not locked by other process
			uniqueID++;
			// FIXME: improve performance here
			for (int generation = 0; generation < count; generation++) {
				// cache all file names for rotation use
				files[generation] = new File(parseFileName(generation));
			}
			fileName = files[0].getAbsolutePath();
			synchronized (allLocks) {
				/*
                 * if current process has held lock for this fileName continue
                 * to find next file
                 */
				if (allLocks.get(fileName) != null) {
					continue;
				}
				if (files[0].exists()
					&& (!append || files[0].length() >= limit)) {
					for (int i = count - 1; i > 0; i--) {
						if (files[i].exists()) {
							files[i].delete();
						}
						files[i - 1].renameTo(files[i]);
					}
				}
				FileOutputStream fileStream = new FileOutputStream(fileName + LCK_EXT);
                /*
                 * if lock is unsupported and IOException thrown, just let the
                 * IOException throws out and exit otherwise it will go into an
                 * undead cycle
                 */
				if (lock == null) {
					fileStream.close();
					continue;
				}
				allLocks.put(fileName, lock);
				break;
			}
		}
		output = new MeasureOutputStream(new BufferedOutputStream(
			new FileOutputStream(fileName, append)), files[0].length());
		setOutputStream(output);
	}

	private void initProperties(String p, Boolean a, Integer l, Integer c) {
		super.initProperties("ALL", null, "java.util.logging.XMLFormatter", null);
		String className = this.getClass().getName();
		pattern = (p == null) ? getStringProperty(className + ".pattern",
			DEFAULT_PATTERN) : p;
		if (pattern == null) {
			throw new NullPointerException("pattern == null");
		} else if (pattern.isEmpty()) {
			throw new NullPointerException("pattern.isEmpty()");
		}
		append = (a == null) ? getBooleanProperty(className + ".append", DEFAULT_APPEND) : a;
		count = (c == null) ? getIntProperty(className + ".count", DEFAULT_COUNT) : c;
		limit = (l == null) ? getIntProperty(className + ".limit", DEFAULT_LIMIT) : l;
		count = count < 1 ? DEFAULT_COUNT : count;
		limit = limit < 0 ? DEFAULT_LIMIT : limit;
		files = new File[count];
	}

	void findNextGeneration() {
		super.close();
		for (int i = count - 1; i > 0; i--) {
			if (files[i].exists()) {
				files[i].delete();
			}
			files[i - 1].renameTo(files[i]);
		}
		try {
			output = new MeasureOutputStream(new BufferedOutputStream(
				new FileOutputStream(files[0])));
		} catch (FileNotFoundException e1) {
			this.getErrorManager().error("Error opening log file", e1, ErrorManager.OPEN_FAILURE);
		}
		setOutputStream(output);
	}

	private String parseFileName(int gen) {
		int cur = 0;
		int next = 0;
		boolean hasUniqueID = false;
		boolean hasGeneration = false;

		// TODO privilege code?

		String tempPath = System.getProperty("java.io.tmpdir");
		boolean tempPathHasSepEnd = (tempPath != null && tempPath.endsWith(File.separator));

		String homePath = System.getProperty("user.home");
		boolean homePathHasSepEnd = (homePath != null && homePath.endsWith(File.separator));

		StringBuilder sb = new StringBuilder();
		pattern = pattern.replace('/', File.separatorChar);

		char[] value = pattern.toCharArray();
		while ((next = pattern.indexOf('%', cur)) >= 0) {
			if (++next < pattern.length()) {
				switch (value[next]) {
					case 'g':
						sb.append(value, cur, next - cur - 1).append(gen);
						hasGeneration = true;
						break;
					case 'u':
						sb.append(value, cur, next - cur - 1).append(uniqueID);
						hasUniqueID = true;
						break;
					case 't':
						sb.append(value, cur, next - cur - 1).append(tempPath);
						if (!tempPathHasSepEnd) sb.append(File.separator);
						break;
					case 'h':
						sb.append(value, cur, next - cur - 1).append(homePath);
						if (!homePathHasSepEnd) sb.append(File.separator);
						break;
					case '%':
						sb.append(value, cur, next - cur - 1).append('%');
						break;
					default:
						sb.append(value, cur, next - cur);
				}
				cur = ++next;
			} else {
				// fail silently
			}
		}

		sb.append(value, cur, value.length - cur);

		if (!hasGeneration && count > 1) {
			sb.append(".").append(gen);
		}

		if (!hasUniqueID && uniqueID > 0) {
			sb.append(".").append(uniqueID);
		}

		return sb.toString();
	}

	private boolean getBooleanProperty(String key, boolean defaultValue) {
		String property = manager.getProperty(key);
		if (property == null) return defaultValue;
		if ("true".equalsIgnoreCase(property)) return true;
		if ("false".equalsIgnoreCase(property)) return false;
		return defaultValue;
	}

	private String getStringProperty(String key, String defaultValue) {
		String property = manager.getProperty(key);
		return property == null ? defaultValue : property;
	}

	// get int LogManager property, if invalid value got, using default value
	private int getIntProperty(String key, int defaultValue) {
		String property = manager.getProperty(key);
		int result = defaultValue;
		if (property != null) {
			try {
				result = Integer.parseInt(property);
			} catch (Exception e) {
				// ignore
			}
		}
		return result;
	}

	public FileHandler(String pattern) throws IOException {
		if (pattern.isEmpty()) {
			throw new IllegalArgumentException("Pattern cannot be empty");
		}
		init(pattern, null, DEFAULT_LIMIT, DEFAULT_COUNT);
	}

	public FileHandler(String pattern, boolean append) throws IOException {
		if (pattern.isEmpty()) throw new IllegalArgumentException("Pattern cannot be empty");
		init(pattern, append, DEFAULT_LIMIT, DEFAULT_COUNT);
	}

	public FileHandler(String pattern, int limit, int count) throws IOException {
		if (pattern.isEmpty()) throw new IllegalArgumentException("Pattern cannot be empty");
		if (limit < 0 || count < 1) throw new IllegalArgumentException("limit < 0 || count < 1");
		init(pattern, null, limit, count);
	}

	public FileHandler(String pattern, int limit, int count, boolean append) throws IOException {
		if (pattern.isEmpty()) throw new IllegalArgumentException("Pattern cannot be empty");
		if (limit < 0 || count < 1) throw new IllegalArgumentException("limit < 0 || count < 1");
		init(pattern, append, limit, count);
	}

	@Override
	public void close() {
		// release locks
		super.close();
		allLocks.remove(fileName);
		try {
			lock.release();
			File file = new File(fileName + LCK_EXT);
			file.delete();
		} catch (IOException e) {
			// ignore
		}
	}

	@Override
	public synchronized void publish(LogRecord record) {
		super.publish(record);
		flush();
		if (limit > 0 && output.getLength() >= limit) {
			findNextGeneration();
		}
	}

	@SuppressWarnings({"WeakerAccess", "NullableProblems"})
	static class MeasureOutputStream extends OutputStream {

		OutputStream wrapped;

		long length;

		public MeasureOutputStream(OutputStream stream, long currentLength) {
			wrapped = stream;
			length = currentLength;
		}

		public MeasureOutputStream(OutputStream stream) {
			this(stream, 0);
		}

		@Override
		public void write(int oneByte) throws IOException {
			wrapped.write(oneByte);
			length++;
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			wrapped.write(b, off, len);
			length += len;
		}

		@Override
		public void close() throws IOException {
			wrapped.close();
		}

		@Override
		public void flush() throws IOException {
			wrapped.flush();
		}

		public long getLength() {
			return length;
		}

		public void setLength(long newLength) {
			length = newLength;
		}
	}
}
