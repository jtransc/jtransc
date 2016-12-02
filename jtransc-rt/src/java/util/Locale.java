/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.util;

import com.jtransc.JTranscSystemProperties;

import java.io.*;

public final class Locale implements Cloneable, Serializable {
	public static final Locale CANADA = new Locale(true, "en", "CA");
	public static final Locale CANADA_FRENCH = new Locale(true, "fr", "CA");
	public static final Locale CHINA = new Locale(true, "zh", "CN");
	public static final Locale CHINESE = new Locale(true, "zh", "");
	public static final Locale ENGLISH = new Locale(true, "en", "");
	public static final Locale FRANCE = new Locale(true, "fr", "FR");
	public static final Locale FRENCH = new Locale(true, "fr", "");
	public static final Locale GERMAN = new Locale(true, "de", "");
	public static final Locale GERMANY = new Locale(true, "de", "DE");
	public static final Locale ITALIAN = new Locale(true, "it", "");
	public static final Locale ITALY = new Locale(true, "it", "IT");
	public static final Locale JAPAN = new Locale(true, "ja", "JP");
	public static final Locale JAPANESE = new Locale(true, "ja", "");
	public static final Locale KOREA = new Locale(true, "ko", "KR");
	public static final Locale KOREAN = new Locale(true, "ko", "");
	public static final Locale PRC = new Locale(true, "zh", "CN");
	public static final Locale ROOT = new Locale(true, "", "");
	public static final Locale SIMPLIFIED_CHINESE = new Locale(true, "zh", "CN");
	public static final Locale TAIWAN = new Locale(true, "zh", "TW");
	public static final Locale TRADITIONAL_CHINESE = new Locale(true, "zh", "TW");
	public static final Locale UK = new Locale(true, "en", "GB");
	public static final Locale US = new Locale(true, "en", "US");

	private static final Locale SPANISH = new Locale(true, "es", "ES");

	private static Locale defaultLocale = US;

	static {
		String language = JTranscSystemProperties.userLanguage();
		String region = JTranscSystemProperties.userRegion();
		String variant = JTranscSystemProperties.userVariant();
		defaultLocale = new Locale(language, region, variant);
	}

	public static Locale forLanguageTag(String languageTag) {
		String[] parts = languageTag.split("-");
		String language = parts[0].toLowerCase();
		for (Locale locale : getAvailableLocales()) {
			if (Objects.equals(locale.getLanguage(), language)) return locale;
		}

		return new Locale(true, language, "");
	}

	private transient String countryCode;
	private transient String languageCode;
	private transient String variantCode;
	private transient String cachedToStringResult;

	private Locale(boolean unused, String lowerCaseLanguageCode, String upperCaseCountryCode) {
		this.languageCode = lowerCaseLanguageCode;
		this.countryCode = upperCaseCountryCode;
		this.variantCode = "";
	}

	public Locale(String language) {
		this(language, "", "");
	}

	public Locale(String language, String country) {
		this(language, country, "");
	}

	public Locale(String language, String country, String variant) {
		Objects.requireNonNull(language);
		Objects.requireNonNull(country);
		Objects.requireNonNull(variant);

		if (language.isEmpty() && country.isEmpty()) {
			languageCode = "";
			countryCode = "";
			variantCode = variant;
			return;
		}

		languageCode = language.toLowerCase(Locale.US);
		// Map new language codes to the obsolete language
		// codes so the correct resource bundles will be used.
		if (languageCode.equals("he")) {
			languageCode = "iw";
		} else if (languageCode.equals("id")) {
			languageCode = "in";
		} else if (languageCode.equals("yi")) {
			languageCode = "ji";
		}

		countryCode = country.toUpperCase(Locale.US);

		// Work around for be compatible with RI
		variantCode = variant;
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
		if (object instanceof Locale) {
			Locale o = (Locale) object;
			return languageCode.equals(o.languageCode) && countryCode.equals(o.countryCode) && variantCode.equals(o.variantCode);
		}
		return false;
	}

	static private Locale[] AVAILABLE_LOCALES = new Locale[]{
		ENGLISH, SPANISH, FRENCH, GERMAN, ITALY
	};

	public static Locale[] getAvailableLocales() {
		return AVAILABLE_LOCALES;
	}

	public String getCountry() {
		return countryCode;
	}

	public static Locale getDefault() {
		return defaultLocale;
	}

	public final String getDisplayCountry() {
		return getDisplayCountry(getDefault());
	}

	public String getDisplayCountry(Locale locale) {
		if (countryCode.isEmpty()) return "";
		return locale.countryCode;
	}

	public final String getDisplayLanguage() {
		return getDisplayLanguage(getDefault());
	}

	public String getDisplayLanguage(Locale locale) {
		if (languageCode.isEmpty()) return "";

		// http://b/8049507 --- frameworks/base should use fil_PH instead of tl_PH.
		// Until then, we're stuck covering their tracks, making it look like they're
		// using "fil" when they're not.
		String localeString = toString();
		if (languageCode.equals("tl")) {
			localeString = toNewString("fil", countryCode, variantCode);
		}

		//String result = ICU.getDisplayLanguageNative(localeString, locale.toString());
		//if (result == null) { // TODO: do we need to do this, or does ICU do it for us?
		//    result = ICU.getDisplayLanguageNative(localeString, Locale.getDefault().toString());
		//}
		//return result;
		return languageCode;
	}

	public final String getDisplayName() {
		return getDisplayName(getDefault());
	}

	public String getDisplayName(Locale locale) {
		int count = 0;
		StringBuilder buffer = new StringBuilder();
		if (!languageCode.isEmpty()) {
			String displayLanguage = getDisplayLanguage(locale);
			buffer.append(displayLanguage.isEmpty() ? languageCode : displayLanguage);
			++count;
		}
		if (!countryCode.isEmpty()) {
			if (count == 1) buffer.append(" (");
			String displayCountry = getDisplayCountry(locale);
			buffer.append(displayCountry.isEmpty() ? countryCode : displayCountry);
			++count;
		}
		if (!variantCode.isEmpty()) {
			if (count == 1) {
				buffer.append(" (");
			} else if (count == 2) {
				buffer.append(",");
			}
			String displayVariant = getDisplayVariant(locale);
			buffer.append(displayVariant.isEmpty() ? variantCode : displayVariant);
			++count;
		}
		if (count > 1) buffer.append(")");
		return buffer.toString();
	}

	public final String getDisplayVariant() {
		return getDisplayVariant(getDefault());
	}

	public String getDisplayVariant(Locale locale) {
		if (variantCode.length() == 0) return variantCode;
		//String result = ICU.getDisplayVariantNative(toString(), locale.toString());
		//if (result == null) { // TODO: do we need to do this, or does ICU do it for us?
		//    result = ICU.getDisplayVariantNative(toString(), Locale.getDefault().toString());
		//}
		//return result;
		return variantCode;
	}

	public String getISO3Country() {
		//String code = ICU.getISO3CountryNative(toString());
		//if (!countryCode.isEmpty() && code.isEmpty()) {
		//    throw new MissingResourceException("No 3-letter country code for locale: " + this, "FormatData_" + this, "ShortCountry");
		//}
		//return code;
		return "USA";
	}

	public String getISO3Language() {
		//String code = ICU.getISO3LanguageNative(toString());
		//if (!languageCode.isEmpty() && code.isEmpty()) {
		//    throw new MissingResourceException("No 3-letter language code for locale: " + this, "FormatData_" + this, "ShortLanguage");
		//}
		//return code;
		return "ENG";
	}

	public static String[] getISOCountries() {
		//return ICU.getISOCountries();
		return new String[]{"USA"};
	}

	public static String[] getISOLanguages() {
		//return ICU.getISOLanguages();
		return new String[]{"ENG"};
	}

	public String getLanguage() {
		return languageCode;
	}

	public String getVariant() {
		return variantCode;
	}

	@Override
	public synchronized int hashCode() {
		return countryCode.hashCode() + languageCode.hashCode() + variantCode.hashCode();
	}

	public synchronized static void setDefault(Locale locale) {
		Objects.requireNonNull(locale);
		defaultLocale = locale;
	}

	@Override
	public final String toString() {
		String result = cachedToStringResult;
		if (result == null) result = cachedToStringResult = toNewString(languageCode, countryCode, variantCode);
		return result;
	}

	private static String toNewString(String languageCode, String countryCode, String variantCode) {
		if (languageCode.length() == 0 && countryCode.length() == 0) return "";
		StringBuilder result = new StringBuilder(11);
		result.append(languageCode);
		if (countryCode.length() > 0 || variantCode.length() > 0) result.append('_');
		result.append(countryCode);
		if (variantCode.length() > 0) result.append('_');
		result.append(variantCode);
		return result.toString();
	}

	private static final ObjectStreamField[] serialPersistentFields = {
		new ObjectStreamField("country", String.class),
		new ObjectStreamField("hashcode", int.class),
		new ObjectStreamField("language", String.class),
		new ObjectStreamField("variant", String.class),
	};

	private void writeObject(ObjectOutputStream stream) throws IOException {
		ObjectOutputStream.PutField fields = stream.putFields();
		fields.put("country", countryCode);
		fields.put("hashcode", -1);
		fields.put("language", languageCode);
		fields.put("variant", variantCode);
		stream.writeFields();
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		ObjectInputStream.GetField fields = stream.readFields();
		countryCode = (String) fields.get("country", "");
		languageCode = (String) fields.get("language", "");
		variantCode = (String) fields.get("variant", "");
	}

	public enum Category {DISPLAY, FORMAT}
}
