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
	static private final HashMap<String, Locale> localesByTag = new HashMap<>();
	static private final LinkedHashSet<String> isoLanguages = new LinkedHashSet<>();
	static private final LinkedHashSet<String> isoCountries = new LinkedHashSet<>();
	static public final Locale ENGLISH = createConstant("en", "");
	static public final Locale SPANISH = createConstant("es", "");
	static public final Locale FRENCH = createConstant("fr", "");
	static public final Locale GERMAN = createConstant("de", "");
	static public final Locale ITALIAN = createConstant("it", "");
	static public final Locale JAPANESE = createConstant("ja", "");
	static public final Locale KOREAN = createConstant("ko", "");
	static public final Locale CHINESE = createConstant("zh", "");
	static public final Locale SIMPLIFIED_CHINESE = createConstant("zh", "CN");
	static public final Locale TRADITIONAL_CHINESE = createConstant("zh", "TW");
	static public final Locale SPAIN = createConstant("es", "ES");
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

	static private Locale defaultLocale = ENGLISH;

	static private Locale[] allLocales = new Locale[]{
		ENGLISH, SPANISH, FRENCH, GERMAN, ITALIAN, JAPANESE, KOREAN, CHINESE, SIMPLIFIED_CHINESE, TRADITIONAL_CHINESE,
		SPAIN, FRANCE, GERMANY, ITALY, JAPAN, KOREA, CHINA, PRC, TAIWAN, UK, US, CANADA, CANADA_FRENCH, ROOT
	};

	static {
		for (Locale locale : allLocales) {
			localesByTag.put(locale.toLanguageTag().toLowerCase(), locale);
			localesByTag.put(locale.getLanguage().toLowerCase(), locale);
			isoLanguages.add(locale.getLanguage());
			isoCountries.add(locale.getCountry());
		}
	}

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
		return new Locale(lang, country);
	}

	public static Locale getDefault() {
		return defaultLocale;
	}

	public static Locale getDefault(Locale.Category category) {
		return getDefault();
	}

	public static synchronized void setDefault(Locale newLocale) {
		Locale.defaultLocale = newLocale;
	}

	public static synchronized void setDefault(Locale.Category category, Locale newLocale) {
		Locale.defaultLocale = newLocale;
	}

	public static Locale[] getAvailableLocales() {
		return allLocales;
	}

	public static String[] getISOCountries() {
		return isoCountries.toArray(new String[isoCountries.size()]);
	}

	public static String[] getISOLanguages() {
		return isoLanguages.toArray(new String[isoLanguages.size()]);
	}

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

	public final String toString() {
		return toLanguageTag();
	}

	public String toLanguageTag() {
		return (!country.isEmpty()) ? (language + "-" + country) : language;
	}

	public static Locale forLanguageTag(String languageTag) {
		languageTag = languageTag.toLowerCase().trim();
		if (localesByTag.containsKey(languageTag)) {
			return localesByTag.get(languageTag);
		} else {
			return ENGLISH;
		}
	}

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
		DISPLAY, FORMAT
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

	public enum FilteringMode {
		AUTOSELECT_FILTERING,
		EXTENDED_FILTERING,
		IGNORE_EXTENDED_RANGES,
		MAP_EXTENDED_RANGES,
		REJECT_EXTENDED_RANGES
	}
}
