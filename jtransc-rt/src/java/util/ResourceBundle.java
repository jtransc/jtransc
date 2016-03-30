/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1999 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.util;

import java.io.IOException;

public abstract class ResourceBundle {
	protected ResourceBundle parent = null;

	public ResourceBundle() {
	}

	public final String getString(String key) {
		return (String) getObject(key);
	}

	public final String[] getStringArray(String key) {
		return (String[]) getObject(key);
	}

	public final Object getObject(String key) {
		return handleGetObject(key);
	}

	public Locale getLocale() {
		return Locale.getDefault();
	}

	protected void setParent(ResourceBundle parent) {
		this.parent = parent;
	}

	public static final ResourceBundle getBundle(String baseName) {
		return new ResourceBundle() {
			@Override
			protected Object handleGetObject(String key) {
				return key;
			}

			@Override
			public Enumeration<String> getKeys() {
				return null;
			}
		};
	}

	public static final ResourceBundle getBundle(String baseName, Control control) {
		return getBundle(baseName);
	}

	public static final ResourceBundle getBundle(String baseName, Locale locale) {
		return getBundle(baseName);
	}

	public static final ResourceBundle getBundle(String baseName, Locale targetLocale, Control control) {
		return getBundle(baseName);
	}

	public static ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader) {
		return getBundle(baseName);
	}

	public static ResourceBundle getBundle(String baseName, Locale targetLocale, ClassLoader loader, Control control) {
		return getBundle(baseName);
	}

	public static final void clearCache() {

	}

	public static final void clearCache(ClassLoader loader) {

	}

	protected abstract Object handleGetObject(String key);

	public abstract Enumeration<String> getKeys();

	public boolean containsKey(String key) {
		return handleGetObject(key) != null;
	}

	native public Set<String> keySet();

	native protected Set<String> handleKeySet();

	public static class Control {
		public static final List<String> FORMAT_DEFAULT = Collections.unmodifiableList(Arrays.asList("java.class", "java.properties"));
		public static final List<String> FORMAT_CLASS = Collections.unmodifiableList(Arrays.asList("java.class"));
		public static final List<String> FORMAT_PROPERTIES = Collections.unmodifiableList(Arrays.asList("java.properties"));
		public static final long TTL_DONT_CACHE = -1;
		public static final long TTL_NO_EXPIRATION_CONTROL = -2;
		private static final Control INSTANCE = new Control();

		protected Control() {
		}

		native public static final Control getControl(List<String> formats);

		native public static final Control getNoFallbackControl(List<String> formats);

		native public List<String> getFormats(String baseName);

		native public List<Locale> getCandidateLocales(String baseName, Locale locale);

		native public Locale getFallbackLocale(String baseName, Locale locale);

		native public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException;

		native public long getTimeToLive(String baseName, Locale locale);

		native public boolean needsReload(String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime);

		native public String toBundleName(String baseName, Locale locale);

		native public final String toResourceName(String bundleName, String suffix);
	}

	private static class SingleFormatControl extends Control {
		private static final Control PROPERTIES_ONLY = new SingleFormatControl(FORMAT_PROPERTIES);
		private static final Control CLASS_ONLY = new SingleFormatControl(FORMAT_CLASS);
		private final List<String> formats;

		protected SingleFormatControl(List<String> formats) {
			this.formats = formats;
		}

		public List<String> getFormats(String baseName) {
			Objects.requireNonNull(baseName);
			return formats;
		}
	}
}
