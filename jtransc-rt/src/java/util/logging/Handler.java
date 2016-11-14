package java.util.logging;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public abstract class Handler {
	private static final Level DEFAULT_LEVEL = Level.ALL;
	private ErrorManager errorMan;
	private String encoding;
	private Level level;
	private Formatter formatter;
	private Filter filter;
	private String prefix;

	protected Handler() {
		this.errorMan = new ErrorManager();
		this.level = DEFAULT_LEVEL;
		this.encoding = null;
		this.filter = null;
		this.formatter = null;
		this.prefix = this.getClass().getName();
	}

	private Object getDefaultInstance(String className) {
		Object result = null;
		if (className == null) {
			return result;
		}
		try {
			result = Class.forName(className).newInstance();
		} catch (Exception e) {
			// ignore
		}
		return result;
	}

	private Object getCustomizeInstance(final String className) throws Exception {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			loader = ClassLoader.getSystemClassLoader();
		}
		Class<?> c = loader.loadClass(className);
		return c.newInstance();
	}

	void printInvalidPropMessage(String key, String value, Exception e) {
		String msg = "Invalid property value for " + prefix + ":" + key + "/" + value;
		errorMan.error(msg, e, ErrorManager.GENERIC_FAILURE);
	}

	void initProperties(String defaultLevel, String defaultFilter, String defaultFormatter, String defaultEncoding) {
		LogManager manager = LogManager.getLogManager();

		// set filter
		final String filterName = manager.getProperty(prefix + ".filter");
		if (filterName != null) {
			try {
				filter = (Filter) getCustomizeInstance(filterName);
			} catch (Exception e1) {
				printInvalidPropMessage("filter", filterName, e1);
				filter = (Filter) getDefaultInstance(defaultFilter);
			}
		} else {
			filter = (Filter) getDefaultInstance(defaultFilter);
		}

		// set level
		String levelName = manager.getProperty(prefix + ".level");
		if (levelName != null) {
			try {
				level = Level.parse(levelName);
			} catch (Exception e) {
				printInvalidPropMessage("level", levelName, e);
				level = Level.parse(defaultLevel);
			}
		} else {
			level = Level.parse(defaultLevel);
		}

		// set formatter
		final String formatterName = manager.getProperty(prefix + ".formatter");
		if (formatterName != null) {
			try {
				formatter = (Formatter) getCustomizeInstance(formatterName);
			} catch (Exception e) {
				printInvalidPropMessage("formatter", formatterName, e);
				formatter = (Formatter) getDefaultInstance(defaultFormatter);
			}
		} else {
			formatter = (Formatter) getDefaultInstance(defaultFormatter);
		}

		// set encoding
		final String encodingName = manager.getProperty(prefix + ".encoding");
		try {
			internalSetEncoding(encodingName);
		} catch (UnsupportedEncodingException e) {
			printInvalidPropMessage("encoding", encodingName, e);
		}
	}

	public abstract void close();

	public abstract void flush();

	public abstract void publish(LogRecord record);

	public String getEncoding() {
		return this.encoding;
	}

	public ErrorManager getErrorManager() {
		LogManager.getLogManager().checkAccess();
		return this.errorMan;
	}

	public Filter getFilter() {
		return this.filter;
	}
	public Formatter getFormatter() {
		return this.formatter;
	}
	public Level getLevel() {
		return this.level;
	}
	@SuppressWarnings("SimplifiableIfStatement")
	public boolean isLoggable(LogRecord record) {
		if (record == null) throw new NullPointerException("record == null");
		if (this.level.intValue() == Level.OFF.intValue()) return false;
		if (record.getLevel().intValue() >= this.level.intValue()) return this.filter == null || this.filter.isLoggable(record);
		return false;
	}

	protected void reportError(String msg, Exception ex, int code) {
		this.errorMan.error(msg, ex, code);
	}

	void internalSetEncoding(String newEncoding) throws UnsupportedEncodingException {
		// accepts "null" because it indicates using default encoding
		if (newEncoding == null) {
			this.encoding = null;
		} else {
			if (Charset.isSupported(newEncoding)) {
				this.encoding = newEncoding;
			} else {
				throw new UnsupportedEncodingException(newEncoding);
			}
		}
	}

	public void setEncoding(String charsetName) throws UnsupportedEncodingException {
		LogManager.getLogManager().checkAccess();
		internalSetEncoding(charsetName);
	}

	public void setErrorManager(ErrorManager newErrorManager) {
		LogManager.getLogManager().checkAccess();
		if (newErrorManager == null) throw new NullPointerException("newErrorManager == null");
		this.errorMan = newErrorManager;
	}

	public void setFilter(Filter newFilter) {
		LogManager.getLogManager().checkAccess();
		this.filter = newFilter;
	}

	void internalSetFormatter(Formatter newFormatter) {
		if (newFormatter == null) throw new NullPointerException("newFormatter == null");
		this.formatter = newFormatter;
	}

	public void setFormatter(Formatter newFormatter) {
		LogManager.getLogManager().checkAccess();
		internalSetFormatter(newFormatter);
	}

	public void setLevel(Level newLevel) {
		if (newLevel == null) throw new NullPointerException("newLevel == null");
		LogManager.getLogManager().checkAccess();
		this.level = newLevel;
	}
}