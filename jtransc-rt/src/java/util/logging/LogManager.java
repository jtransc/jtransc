package java.util.logging;

import com.jtransc.io.JTranscConsole;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.util.*;

public class LogManager {
	private static final LoggingPermission perm = new LoggingPermission("control", null);
	static LogManager manager;
	public static final String LOGGING_MXBEAN_NAME = "java.util.logging:type=Logging";
	public static LoggingMXBean getLoggingMXBean() {
		throw new UnsupportedOperationException();
	}

	// FIXME: use weak reference to avoid heap memory leak
	private Hashtable<String, Logger> loggers;
	private Properties props;
	private PropertyChangeSupport listeners;

	static {
		// init LogManager singleton instance
		String className = System.getProperty("java.util.logging.manager");
		if (className != null) {
			manager = (LogManager) getInstanceByClass(className);
		}
		if (manager == null) {
			manager = new LogManager();
		}

		// read configuration
		try {
			manager.readConfiguration();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// if global logger has been initialized, set root as its parent
		Logger root = new Logger("", null);
		root.setLevel(Level.INFO);
		Logger.global.setParent(root);

		manager.addLogger(root);
		manager.addLogger(Logger.global);
	}

	protected LogManager() {
		loggers = new Hashtable<String, Logger>();
		props = new Properties();
		listeners = new PropertyChangeSupport(this);
		// add shutdown hook to ensure that the associated resource will be
		// freed when JVM exits
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override public void run() {
				reset();
			}
		});
	}

	public void checkAccess() {
	}

	public synchronized boolean addLogger(Logger logger) {
		String name = logger.getName();
		if (loggers.get(name) != null) return false;
		addToFamilyTree(logger, name);
		loggers.put(name, logger);
		logger.setManager(this);
		return true;
	}

	private void addToFamilyTree(Logger logger, String name) {
		Logger parent = null;
		// find parent
		int lastSeparator;
		String parentName = name;
		while ((lastSeparator = parentName.lastIndexOf('.')) != -1) {
			parentName = parentName.substring(0, lastSeparator);
			parent = loggers.get(parentName);
			if (parent != null) {
				setParent(logger, parent);
				break;
			} else if (getProperty(parentName + ".level") != null ||
				getProperty(parentName + ".handlers") != null) {
				parent = Logger.getLogger(parentName);
				setParent(logger, parent);
				break;
			}
		}
		if (parent == null && (parent = loggers.get("")) != null) {
			setParent(logger, parent);
		}

		// find children
		// TODO: performance can be improved here?
		String nameDot = name + '.';
		Collection<Logger> allLoggers = loggers.values();
		for (final Logger child : allLoggers) {
			Logger oldParent = child.getParent();
			if (parent == oldParent && (name.length() == 0 || child.getName().startsWith(nameDot))) {
				final Logger thisLogger = logger;
				child.setParent(thisLogger);
				if (oldParent != null) {
					// -- remove from old parent as the parent has been changed
					oldParent.children.remove(child);
				}
			}
		}
	}

	public synchronized Logger getLogger(String name) {
		return loggers.get(name);
	}
	public synchronized Enumeration<String> getLoggerNames() {
		return loggers.keys();
	}
	public static LogManager getLogManager() {
		return manager;
	}
	public String getProperty(String name) {
		return props.getProperty(name);
	}
	public void readConfiguration() throws IOException {
		// check config class
		String configClassName = System.getProperty("java.util.logging.config.class");
		if (configClassName == null || getInstanceByClass(configClassName) == null) {
			// if config class failed, check config file
			String configFile = System.getProperty("java.util.logging.config.file");

			if (configFile == null) {
				// if cannot find configFile, use default logging.properties
				configFile = System.getProperty("java.home") + File.separator + "lib" + File.separator + "logging.properties";
			}

			InputStream input = null;
			try {
				try {
					input = new FileInputStream(configFile);
				} catch (IOException exception) {
					// fall back to using the built-in logging.properties file
					input = LogManager.class.getResourceAsStream("logging.properties");
					if (input == null) {
						throw exception;
					}
				}
				readConfiguration(new BufferedInputStream(input));
			} finally {
				input.close();
			}
		}
	}

	// use SystemClassLoader to load class from system classpath
	static Object getInstanceByClass(final String className) {
		try {
			Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className);
			return clazz.newInstance();
		} catch (Exception e) {
			try {
				Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
				return clazz.newInstance();
			} catch (Exception innerE) {
				JTranscConsole.error("Loading class '" + className + "' failed");
				JTranscConsole.error(innerE);
				return null;
			}
		}
	}

	// actual initialization process from a given input stream
	private synchronized void readConfigurationImpl(InputStream ins)
		throws IOException {
		reset();
		props.load(ins);

		// The RI treats the root logger as special. For compatibility, always
		// update the root logger's handlers.
		Logger root = loggers.get("");
		if (root != null) {
			root.setManager(this);
		}

		// parse property "config" and apply setting
		String configs = props.getProperty("config");
		if (configs != null) {
			StringTokenizer st = new StringTokenizer(configs, " ");
			while (st.hasMoreTokens()) {
				String configerName = st.nextToken();
				getInstanceByClass(configerName);
			}
		}

		// set levels for logger
		Collection<Logger> allLoggers = loggers.values();
		for (Logger logger : allLoggers) {
			String property = props.getProperty(logger.getName() + ".level");
			if (property != null) {
				logger.setLevel(Level.parse(property));
			}
		}
		listeners.firePropertyChange(null, null, null);
	}

	public void readConfiguration(InputStream ins) throws IOException {
		checkAccess();
		readConfigurationImpl(ins);
	}

	public synchronized void reset() {
		checkAccess();
		props = new Properties();
		Enumeration<String> names = getLoggerNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			Logger logger = getLogger(name);
			if (logger != null) {
				logger.reset();
			}
		}
		Logger root = loggers.get("");
		if (root != null) {
			root.setLevel(Level.INFO);
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		if (l == null) {
			throw new NullPointerException("l == null");
		}
		checkAccess();
		listeners.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		checkAccess();
		listeners.removePropertyChangeListener(l);
	}

	synchronized Logger getOrCreate(String name, String resourceBundleName) {
		Logger result = getLogger(name);
		if (result == null) {
			result = new Logger(name, resourceBundleName);
			addLogger(result);
		}
		return result;
	}

	synchronized void setParent(Logger logger, Logger newParent) {
		logger.parent = newParent;

		if (logger.levelObjVal == null) {
			setLevelRecursively(logger, null);
		}
		newParent.children.add(logger);
		logger.updateVmLogHandler();
	}

	synchronized void setLevelRecursively(Logger logger, Level newLevel) {
		int previous = logger.levelIntVal;
		logger.levelObjVal = newLevel;

		if (newLevel == null) {
			logger.levelIntVal = logger.parent != null ? logger.parent.levelIntVal : Level.INFO.intValue();
		} else {
			logger.levelIntVal = newLevel.intValue();
		}

		if (previous != logger.levelIntVal) {
			for (Logger child : logger.children) {
				if (child.levelObjVal == null) {
					setLevelRecursively(child, null);
				}
			}
		}
	}
}