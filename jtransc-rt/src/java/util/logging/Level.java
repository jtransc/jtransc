package java.util.logging;

import com.jtransc.lang.VMStack;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

public class Level implements Serializable {

	private static final List<Level> levels = new ArrayList<Level>(9);
	public static final Level OFF = new Level("OFF", Integer.MAX_VALUE);
	public static final Level SEVERE = new Level("SEVERE", 1000);
	public static final Level WARNING = new Level("WARNING", 900);
	public static final Level INFO = new Level("INFO", 800);
	public static final Level CONFIG = new Level("CONFIG", 700);
	public static final Level FINE = new Level("FINE", 500);
	public static final Level FINER = new Level("FINER", 400);
	public static final Level FINEST = new Level("FINEST", 300);
	public static final Level ALL = new Level("ALL", Integer.MIN_VALUE);

	public static Level parse(String name) throws IllegalArgumentException {
		if (name == null) throw new NullPointerException("name == null");

		boolean isNameAnInt;
		int nameAsInt;
		try {
			nameAsInt = Integer.parseInt(name);
			isNameAnInt = true;
		} catch (NumberFormatException e) {
			nameAsInt = 0;
			isNameAnInt = false;
		}

		synchronized (levels) {
			for (Level level : levels) {
				if (name.equals(level.getName())) {
					return level;
				}
			}

			if (isNameAnInt) {
                /*
                 * Loop through levels a second time, so that the returned
                 * instance will be passed on the order of construction.
                 */
				for (Level level : levels) {
					if (nameAsInt == level.intValue()) {
						return level;
					}
				}
			}
		}

		if (!isNameAnInt) {
			throw new IllegalArgumentException("Cannot parse name '" + name + "'");
		}

		return new Level(name, nameAsInt);
	}

	private final String name;
	private final int value;
	private final String resourceBundleName;
	private transient ResourceBundle rb;

	protected Level(String name, int level) {
		this(name, level, null);
	}

	protected Level(String name, int level, String resourceBundleName) {
		if (name == null) {
			throw new NullPointerException("name == null");
		}
		this.name = name;
		this.value = level;
		this.resourceBundleName = resourceBundleName;
		if (resourceBundleName != null) {
			try {
				rb = ResourceBundle.getBundle(resourceBundleName, Locale.getDefault());
			} catch (MissingResourceException e) {
				rb = null;
			}
		}
		synchronized (levels) {
			levels.add(this);
		}
	}

	/**
	 * Gets the name of this level.
	 *
	 * @return this level's name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the name of the resource bundle associated with this level.
	 *
	 * @return the name of this level's resource bundle.
	 */
	public String getResourceBundleName() {
		return this.resourceBundleName;
	}

	/**
	 * Gets the integer value indicating this level.
	 *
	 * @return this level's integer value.
	 */
	public final int intValue() {
		return this.value;
	}

	/**
	 * Serialization helper method to maintain singletons and add any new
	 * levels.
	 *
	 * @return the resolved instance.
	 */
	private Object readResolve() {
		synchronized (levels) {
			for (Level level : levels) {
				if (value != level.value) continue;
				if (!name.equals(level.name)) continue;
				if (Objects.equals(resourceBundleName, level.resourceBundleName)) return level;
			}
			// This is a new value, so add it.
			levels.add(this);
			return this;
		}
	}

	/**
	 * Serialization helper to setup transient resource bundle instance.
	 *
	 * @param in
	 *            the input stream to read the instance data from.
	 * @throws IOException
	 *             if an IO error occurs.
	 * @throws ClassNotFoundException
	 *             if a class is not found.
	 */
	private void readObject(ObjectInputStream in) throws IOException,
		ClassNotFoundException {
		in.defaultReadObject();
		if (resourceBundleName != null) {
			try {
				rb = ResourceBundle.getBundle(resourceBundleName);
			} catch (MissingResourceException e) {
				rb = null;
			}
		}
	}

	/**
	 * Gets the localized name of this level. The default locale is used. If no
	 * resource bundle is associated with this level then the original level
	 * name is returned.
	 *
	 * @return the localized name of this level.
	 */
	public String getLocalizedName() {
		if (rb == null) {
			return name;
		}

		try {
			return rb.getString(name);
		} catch (MissingResourceException e) {
			return name;
		}
	}

	/**
	 * Compares two {@code Level} objects for equality. They are considered to
	 * be equal if they have the same level value.
	 *
	 * @param o
	 *            the other object to compare this level to.
	 * @return {@code true} if this object equals to the supplied object,
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof Level)) {
			return false;
		}

		return ((Level) o).intValue() == this.value;
	}

	/**
	 * Returns the hash code of this {@code Level} object.
	 *
	 * @return this level's hash code.
	 */
	@Override
	public int hashCode() {
		return this.value;
	}

	/**
	 * Returns the string representation of this {@code Level} object. In
	 * this case, it is the level's name.
	 *
	 * @return the string representation of this level.
	 */
	@Override
	public final String toString() {
		return this.name;
	}
}