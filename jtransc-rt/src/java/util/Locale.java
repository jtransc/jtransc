/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util;

import java.io.Serializable;

public final class Locale implements Cloneable, Serializable {
	//static private final  Cache LOCALECACHE = new Cache();
	static public final Locale ENGLISH = createConstant("en", "");
	static public final Locale FRENCH = createConstant("fr", "");
	static public final Locale GERMAN = createConstant("de", "");
	static public final Locale ITALIAN = createConstant("it", "");
	static public final Locale JAPANESE = createConstant("ja", "");
	static public final Locale KOREAN = createConstant("ko", "");
	static public final Locale CHINESE = createConstant("zh", "");
	static public final Locale SIMPLIFIED_CHINESE = createConstant("zh", "CN");
	static public final Locale TRADITIONAL_CHINESE = createConstant("zh", "TW");
	static public final Locale FRANCE = createConstant("fr", "FR");
	static public final Locale GERMANY = createConstant("de", "DE");
	static public final Locale ITALY = createConstant("it", "IT");
	static public final Locale JAPAN = createConstant("ja", "JP");
	static public final Locale KOREA = createConstant("ko", "KR");
	static public final Locale CHINA = SIMPLIFIED_CHINESE;
	static public final Locale PRC = SIMPLIFIED_CHINESE;
	static public final Locale TAIWAN = TRADITIONAL_CHINESE;
	static public final Locale UK = createConstant("en", "GB");
	static public final Locale US = createConstant("en", "US");
	static public final Locale CANADA = createConstant("en", "CA");
	static public final Locale CANADA_FRENCH = createConstant("fr", "CA");
	static public final Locale ROOT = createConstant("", "");
	static public final char PRIVATE_USE_EXTENSION = 'x';
	static public final char UNICODE_LOCALE_EXTENSION = 'u';

	private String language;
	private String country;
	private String variant;

	public Locale(String language, String country, String variant) {
		this.language = language;
		this.country = country;
		this.variant = variant;
	}

	public Locale(String language, String country) {
		this(language, country, "");
	}

	public Locale(String language) {
		this(language, "", "");
	}

	private static Locale createConstant(String lang, String country) {
		return null;
	}

	public static Locale getDefault() {
		return ENGLISH;
	}

	public static Locale getDefault(Locale.Category category) {
		return ENGLISH;
	}

	native public static synchronized void setDefault(Locale newLocale);

	native public static synchronized void setDefault(Locale.Category category, Locale newLocale);

	native public static Locale[] getAvailableLocales();

	native public static String[] getISOCountries();

	native public static String[] getISOLanguages();

	public String getLanguage() {
		return this.language;
	}

	native public String getScript();

	public String getCountry() {
		return this.country;
	}

	public String getVariant() {
		return this.variant;
	}

	native public boolean hasExtensions();

	native public Locale stripExtensions();

	native public String getExtension(char key);

	native public Set<Character> getExtensionKeys();

	native public Set<String> getUnicodeLocaleAttributes();

	native public String getUnicodeLocaleType(String key);

	native public Set<String> getUnicodeLocaleKeys();

	native public final String toString();

	native public String toLanguageTag();

	native public static Locale forLanguageTag(String languageTag);

	native public String getISO3Language() throws MissingResourceException;

	native public String getISO3Country() throws MissingResourceException;

	native public final String getDisplayLanguage();

	native public String getDisplayLanguage(Locale inLocale);

	native public String getDisplayScript();

	native public String getDisplayScript(Locale inLocale);

	native public final String getDisplayCountry();

	native public String getDisplayCountry(Locale inLocale);

	native public final String getDisplayVariant();

	native public String getDisplayVariant(Locale inLocale);

	native public final String getDisplayName();

	native public String getDisplayName(Locale inLocale);

	native public Object clone();

	native public int hashCode();

	native public boolean equals(Object obj);

	public enum Category {
		DISPLAY, FORMAT;
		/*
        DISPLAY("user.language.display", "user.script.display", "user.country.display", "user.variant.display"),
        FORMAT("user.language.format", "user.script.format", "user.country.format", "user.variant.format");

        Category(String languageKey, String scriptKey, String countryKey, String variantKey) {
            this.languageKey = languageKey;
            this.scriptKey = scriptKey;
            this.countryKey = countryKey;
            this.variantKey = variantKey;
        }

        final String languageKey;
        final String scriptKey;
        final String countryKey;
        final String variantKey;
        */
	}

	public static final class Builder {
		public Builder() {

		}

		native public Builder setLocale(Locale locale);

		native public Builder setLanguageTag(String languageTag);

		native public Builder setLanguage(String language);

		native public Builder setScript(String script);

		native public Builder setRegion(String region);

		native public Builder setVariant(String variant);

		native public Builder setExtension(char key, String value);

		native public Builder setUnicodeLocaleKeyword(String key, String type);

		native public Builder addUnicodeLocaleAttribute(String attribute);

		native public Builder removeUnicodeLocaleAttribute(String attribute);

		native public Builder clear();

		native public Builder clearExtensions();

		native public Locale build();
	}

	public static enum FilteringMode {
		AUTOSELECT_FILTERING,
		EXTENDED_FILTERING,
		IGNORE_EXTENDED_RANGES,
		MAP_EXTENDED_RANGES,
		REJECT_EXTENDED_RANGES
	}
}
