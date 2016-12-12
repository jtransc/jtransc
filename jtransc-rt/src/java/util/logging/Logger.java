package java.util.logging;

import com.jtransc.io.JTranscConsole;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Logger {
	public static final String GLOBAL_LOGGER_NAME = "global";
	@Deprecated
	public static final Logger global = new Logger(GLOBAL_LOGGER_NAME, null);
	private static final Handler[] EMPTY_HANDLERS_ARRAY = new Handler[0];
	private volatile String name;
	Logger parent;
	volatile Level levelObjVal;
	volatile int levelIntVal = Level.INFO.intValue();
	private Filter filter;
	private volatile String resourceBundleName;
	private volatile ResourceBundle resourceBundle;
	private final List<Handler> handlers = new CopyOnWriteArrayList<Handler>();
	private boolean notifyParentHandlers = true;
	private boolean isNamed = true;
	final List<Logger> children = new ArrayList<Logger>();

	void updateVmLogHandler() {
	}

	protected Logger(String name, String resourceBundleName) {
		this.name = name;
		initResourceBundle(resourceBundleName);
		updateVmLogHandler();
	}

	static ResourceBundle loadResourceBundle(String resourceBundleName) {
		// try context class loader to load the resource
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl != null) {
			try {
				return ResourceBundle.getBundle(resourceBundleName, Locale.getDefault(), cl);
			} catch (MissingResourceException ignored) {
				// Failed to load using context class loader, ignore
			}
		}
		// try system class loader to load the resource
		cl = ClassLoader.getSystemClassLoader();
		if (cl != null) {
			try {
				return ResourceBundle.getBundle(resourceBundleName, Locale.getDefault(), cl);
			} catch (MissingResourceException ignored) {
				// Failed to load using system class loader, ignore
			}
		}
		throw new MissingResourceException("Failed to load the specified resource bundle \"" +
			resourceBundleName + "\"", resourceBundleName, null);
	}

	public static Logger getAnonymousLogger() {
		return getAnonymousLogger(null);
	}

	public static Logger getAnonymousLogger(String resourceBundleName) {
		Logger result = new Logger(null, resourceBundleName);
		result.isNamed = false;
		LogManager logManager = LogManager.getLogManager();
		logManager.setParent(result, logManager.getLogger(""));
		return result;
	}

	private synchronized void initResourceBundle(String resourceBundleName) {
		String current = this.resourceBundleName;

		if (current != null) {
			if (current.equals(resourceBundleName)) {
				return;
			} else {
				throw new IllegalArgumentException("Resource bundle name '" + resourceBundleName + "' is inconsistent with the existing '" + current + "'");
			}
		}

		if (resourceBundleName != null) {
			this.resourceBundle = loadResourceBundle(resourceBundleName);
			this.resourceBundleName = resourceBundleName;
		}
	}

	public static Logger getLogger(String name) {
		return LogManager.getLogManager().getOrCreate(name, null);
	}

	public static Logger getLogger(String name, String resourceBundleName) {
		Logger result = LogManager.getLogManager().getOrCreate(name, resourceBundleName);
		result.initResourceBundle(resourceBundleName);
		return result;
	}

	public static Logger getGlobal() {
		return global;
	}

	public void addHandler(Handler handler) {
		if (handler == null) throw new NullPointerException("handler == null");
		// Anonymous loggers can always add handlers
		if (this.isNamed) LogManager.getLogManager().checkAccess();
		this.handlers.add(handler);
		updateVmLogHandler();
	}

	void setManager(LogManager manager) {
		String levelProperty = manager.getProperty(name + ".level");
		if (levelProperty != null) {
			try {
				manager.setLevelRecursively(Logger.this, Level.parse(levelProperty));
			} catch (IllegalArgumentException invalidLevel) {
				invalidLevel.printStackTrace();
			}
		}

		String handlersPropertyName = name.isEmpty() ? "handlers" : name + ".handlers";
		String handlersProperty = manager.getProperty(handlersPropertyName);
		if (handlersProperty != null) {
			for (String handlerName : handlersProperty.split(",|\\s")) {
				if (handlerName.isEmpty()) {
					continue;
				}

				final Handler handler;
				try {
					handler = (Handler) LogManager.getInstanceByClass(handlerName);
				} catch (Exception invalidHandlerName) {
					invalidHandlerName.printStackTrace();
					continue;
				}

				try {
					String level = manager.getProperty(handlerName + ".level");
					if (level != null) {
						handler.setLevel(Level.parse(level));
					}
				} catch (Exception invalidLevel) {
					invalidLevel.printStackTrace();
				}

				handlers.add(handler);
			}
		}

		updateVmLogHandler();
	}

	public Handler[] getHandlers() {
		return handlers.toArray(EMPTY_HANDLERS_ARRAY);
	}

	public void removeHandler(Handler handler) {
		// Anonymous loggers can always remove handlers
		if (this.isNamed) LogManager.getLogManager().checkAccess();
		if (handler == null) return;
		this.handlers.remove(handler);
		updateVmLogHandler();
	}

	public Filter getFilter() {
		return this.filter;
	}

	public void setFilter(Filter newFilter) {
		// Anonymous loggers can always set the filter
		if (this.isNamed) {
			LogManager.getLogManager().checkAccess();
		}
		filter = newFilter;
	}

	public Level getLevel() {
		return levelObjVal;
	}

	public void setLevel(Level newLevel) {
		// Anonymous loggers can always set the level
		LogManager logManager = LogManager.getLogManager();
		if (this.isNamed) {
			logManager.checkAccess();
		}
		logManager.setLevelRecursively(this, newLevel);
	}

	public boolean getUseParentHandlers() {
		return this.notifyParentHandlers;
	}

	public void setUseParentHandlers(boolean notifyParentHandlers) {
		// Anonymous loggers can always set the useParentHandlers flag
		if (this.isNamed) {
			LogManager.getLogManager().checkAccess();
		}
		this.notifyParentHandlers = notifyParentHandlers;
		updateVmLogHandler();
	}

	public Logger getParent() {
		return parent;
	}

	public void setParent(Logger parent) {
		if (parent == null) throw new NullPointerException("parent == null");
		// even anonymous loggers are checked
		LogManager logManager = LogManager.getLogManager();
		logManager.checkAccess();
		logManager.setParent(this, parent);
	}

	public String getName() {
		return this.name;
	}

	public ResourceBundle getResourceBundle() {
		return this.resourceBundle;
	}

	public String getResourceBundleName() {
		return this.resourceBundleName;
	}

	private boolean internalIsLoggable(Level l) {
		int effectiveLevel = levelIntVal;
		if (effectiveLevel == Level.OFF.intValue()) {
			// always return false if the effective level is off
			return false;
		}
		return l.intValue() >= effectiveLevel;
	}

	public boolean isLoggable(Level l) {
		return internalIsLoggable(l);
	}

	private void setResourceBundle(LogRecord record) {
		for (Logger p = this; p != null; p = p.parent) {
			String resourceBundleName = p.resourceBundleName;
			if (resourceBundleName != null) {
				record.setResourceBundle(p.resourceBundle);
				record.setResourceBundleName(resourceBundleName);
				return;
			}
		}
	}

	public void entering(String sourceClass, String sourceMethod) {
		if (!internalIsLoggable(Level.FINER)) return;
		LogRecord record = new LogRecord(Level.FINER, "ENTRY");
		record.setLoggerName(this.name);
		record.setSourceClassName(sourceClass);
		record.setSourceMethodName(sourceMethod);
		setResourceBundle(record);
		log(record);
	}

	public void entering(String sourceClass, String sourceMethod, Object param) {
		if (!internalIsLoggable(Level.FINER)) return;
		LogRecord record = new LogRecord(Level.FINER, "ENTRY" + " {0}");
		record.setLoggerName(this.name);
		record.setSourceClassName(sourceClass);
		record.setSourceMethodName(sourceMethod);
		record.setParameters(new Object[] { param });
		setResourceBundle(record);
		log(record);
	}

	public void entering(String sourceClass, String sourceMethod, Object[] params) {
		if (!internalIsLoggable(Level.FINER)) return;
		String msg = "ENTRY";
		if (params != null) {
			StringBuilder msgBuffer = new StringBuilder("ENTRY");
			for (int i = 0; i < params.length; i++) {
				msgBuffer.append(" {").append(i).append("}");
			}
			msg = msgBuffer.toString();
		}
		LogRecord record = new LogRecord(Level.FINER, msg);
		record.setLoggerName(this.name);
		record.setSourceClassName(sourceClass);
		record.setSourceMethodName(sourceMethod);
		record.setParameters(params);
		setResourceBundle(record);
		log(record);
	}

	public void exiting(String sourceClass, String sourceMethod) {
		if (!internalIsLoggable(Level.FINER)) return;

		LogRecord record = new LogRecord(Level.FINER, "RETURN");
		record.setLoggerName(this.name);
		record.setSourceClassName(sourceClass);
		record.setSourceMethodName(sourceMethod);
		setResourceBundle(record);
		log(record);
	}

	public void exiting(String sourceClass, String sourceMethod, Object result) {
		if (!internalIsLoggable(Level.FINER)) return;

		LogRecord record = new LogRecord(Level.FINER, "RETURN" + " {0}");
		record.setLoggerName(this.name);
		record.setSourceClassName(sourceClass);
		record.setSourceMethodName(sourceMethod);
		record.setParameters(new Object[] { result });
		setResourceBundle(record);
		log(record);
	}

	public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
		if (!internalIsLoggable(Level.FINER)) return;
		LogRecord record = new LogRecord(Level.FINER, "THROW");
		record.setLoggerName(this.name);
		record.setSourceClassName(sourceClass);
		record.setSourceMethodName(sourceMethod);
		record.setThrown(thrown);
		setResourceBundle(record);
		log(record);
	}

	public void severe(String msg) {
		log(Level.SEVERE, msg);
	}

	public void warning(String msg) {
		log(Level.WARNING, msg);
	}

	public void info(String msg) {
		log(Level.INFO, msg);
	}

	public void config(String msg) {
		log(Level.CONFIG, msg);
	}

	public void fine(String msg) {
		log(Level.FINE, msg);
	}

	public void finer(String msg) {
		log(Level.FINER, msg);
	}

	public void finest(String msg) {
		log(Level.FINEST, msg);
	}

	public void log(Level logLevel, String msg) {
		if (!internalIsLoggable(logLevel)) return;
		JTranscConsole.error("Logger.log : " + logLevel + " : " + msg);
	}

	public void log(Level logLevel, String msg, Object param) {
		if (!internalIsLoggable(logLevel)) return;
		LogRecord record = new LogRecord(logLevel, msg);
		record.setLoggerName(this.name);
		record.setParameters(new Object[] { param });
		setResourceBundle(record);
		log(record);
	}

	public void log(Level logLevel, String msg, Object[] params) {
		if (!internalIsLoggable(logLevel)) return;
		LogRecord record = new LogRecord(logLevel, msg);
		record.setLoggerName(this.name);
		record.setParameters(params);
		setResourceBundle(record);
		log(record);
	}

	public void log(Level logLevel, String msg, Throwable thrown) {
		if (!internalIsLoggable(logLevel)) return;
		LogRecord record = new LogRecord(logLevel, msg);
		record.setLoggerName(this.name);
		record.setThrown(thrown);
		setResourceBundle(record);
		log(record);
	}

	public void log(LogRecord record) {
		if (!internalIsLoggable(record.getLevel())) return;

		// apply the filter if any
		Filter f = filter;
		if (f != null && !f.isLoggable(record)) return;

		Handler[] allHandlers = getHandlers();
		for (Handler element : allHandlers) {
			element.publish(record);
		}
		// call the parent's handlers if set useParentHandlers
		Logger temp = this;
		Logger theParent = temp.parent;
		while (theParent != null && temp.getUseParentHandlers()) {
			Handler[] ha = theParent.getHandlers();
			for (Handler element : ha) {
				element.publish(record);
			}
			temp = theParent;
			theParent = temp.parent;
		}
	}

	public void logp(Level logLevel, String sourceClass, String sourceMethod, String msg) {
		if (!internalIsLoggable(logLevel)) return;
		LogRecord record = new LogRecord(logLevel, msg);
		record.setLoggerName(this.name);
		record.setSourceClassName(sourceClass);
		record.setSourceMethodName(sourceMethod);
		setResourceBundle(record);
		log(record);
	}

	public void logp(Level logLevel, String sourceClass, String sourceMethod, String msg, Object param) {
		if (!internalIsLoggable(logLevel)) return;
		LogRecord record = new LogRecord(logLevel, msg);
		record.setLoggerName(this.name);
		record.setSourceClassName(sourceClass);
		record.setSourceMethodName(sourceMethod);
		record.setParameters(new Object[] { param });
		setResourceBundle(record);
		log(record);
	}

	public void logp(Level logLevel, String sourceClass, String sourceMethod, String msg, Object[] params) {
		if (!internalIsLoggable(logLevel)) return;
		LogRecord record = new LogRecord(logLevel, msg);
		record.setLoggerName(this.name);
		record.setSourceClassName(sourceClass);
		record.setSourceMethodName(sourceMethod);
		record.setParameters(params);
		setResourceBundle(record);
		log(record);
	}

	public void logp(Level logLevel, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
		if (!internalIsLoggable(logLevel)) return;
		LogRecord record = new LogRecord(logLevel, msg);
		record.setLoggerName(this.name);
		record.setSourceClassName(sourceClass);
		record.setSourceMethodName(sourceMethod);
		record.setThrown(thrown);
		setResourceBundle(record);
		log(record);
	}

	public void logrb(Level logLevel, String sourceClass, String sourceMethod, String bundleName, String msg) {
		if (!internalIsLoggable(logLevel)) return;

		LogRecord record = new LogRecord(logLevel, msg);
		if (bundleName != null) {
			try {
				record.setResourceBundle(loadResourceBundle(bundleName));
			} catch (MissingResourceException e) {
				// ignore
			}
			record.setResourceBundleName(bundleName);
		}
		record.setLoggerName(this.name);
		record.setSourceClassName(sourceClass);
		record.setSourceMethodName(sourceMethod);
		log(record);
	}

	public void logrb(Level logLevel, String sourceClass, String sourceMethod, String bundleName, String msg, Object param) {
		if (!internalIsLoggable(logLevel)) return;

		LogRecord record = new LogRecord(logLevel, msg);
		if (bundleName != null) {
			try {
				record.setResourceBundle(loadResourceBundle(bundleName));
			} catch (MissingResourceException e) {
				// ignore
			}
			record.setResourceBundleName(bundleName);
		}
		record.setLoggerName(this.name);
		record.setSourceClassName(sourceClass);
		record.setSourceMethodName(sourceMethod);
		record.setParameters(new Object[] { param });
		log(record);
	}

	public void logrb(Level logLevel, String sourceClass, String sourceMethod, String bundleName, String msg, Object[] params) {
		if (!internalIsLoggable(logLevel)) return;

		LogRecord record = new LogRecord(logLevel, msg);
		if (bundleName != null) {
			try {
				record.setResourceBundle(loadResourceBundle(bundleName));
			} catch (MissingResourceException e) {
				// ignore
			}
			record.setResourceBundleName(bundleName);
		}
		record.setLoggerName(this.name);
		record.setSourceClassName(sourceClass);
		record.setSourceMethodName(sourceMethod);
		record.setParameters(params);
		log(record);
	}

	public void logrb(Level logLevel, String sourceClass, String sourceMethod, String bundleName, String msg, Throwable thrown) {
		if (!internalIsLoggable(logLevel)) return;

		LogRecord record = new LogRecord(logLevel, msg);
		if (bundleName != null) {
			try {
				record.setResourceBundle(loadResourceBundle(bundleName));
			} catch (MissingResourceException e) {
				// ignore
			}
			record.setResourceBundleName(bundleName);
		}
		record.setLoggerName(this.name);
		record.setSourceClassName(sourceClass);
		record.setSourceMethodName(sourceMethod);
		record.setThrown(thrown);
		log(record);
	}

	void reset() {
		levelObjVal = null;
		levelIntVal = Level.INFO.intValue();

		for (Handler handler : handlers) {
			try {
				if (handlers.remove(handler)) {
					handler.close();
				}
			} catch (Exception ignored) {
			}
		}

		updateVmLogHandler();
	}
}