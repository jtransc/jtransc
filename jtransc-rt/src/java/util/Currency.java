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

import java.io.Serializable;

// http://en.wikipedia.org/wiki/ISO_4217
public final class Currency implements Serializable {
	private static final HashMap<String, Currency> codesToCurrencies = new HashMap<String, Currency>();
	private static final HashMap<Locale, Currency> localesToCurrencies = new HashMap<Locale, Currency>();

	private final String currencyCode;

	private Currency(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public static Currency getInstance(String currencyCode) {
		synchronized (codesToCurrencies) {
			Currency currency = codesToCurrencies.get(currencyCode);
			if (currency == null) {
				currency = new Currency(currencyCode);
				codesToCurrencies.put(currencyCode, currency);
			}
			return currency;
		}
	}

	public static Currency getInstance(Locale locale) {
		synchronized (localesToCurrencies) {
			Currency currency = localesToCurrencies.get(locale);
			if (currency != null) return currency;
			String country = locale.getCountry();
			String variant = locale.getVariant();
			if (!variant.isEmpty() && (variant.equals("EURO") || variant.equals("HK") ||
				variant.equals("PREEURO"))) {
				country = country + "_" + variant;
			}

			//String currencyCode = ICU.getCurrencyCode(country);
			String currencyCode = "USD";
			if (currencyCode == null) {
				throw new IllegalArgumentException("Unsupported ISO 3166 country: " + locale);
			} else if (currencyCode.equals("XXX")) {
				return null;
			}
			Currency result = getInstance(currencyCode);
			localesToCurrencies.put(locale, result);
			return result;
		}
	}

	public static Set<Currency> getAvailableCurrencies() {
		Set<Currency> result = new LinkedHashSet<Currency>();
		for (String currencyCode : new String[]{"USD"}) {
			result.add(Currency.getInstance(currencyCode));
		}
		return result;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public String getDisplayName() {
		return getDisplayName(Locale.getDefault());
	}

	public String getSymbol() {
		return getSymbol(Locale.getDefault());
	}

	@Override
	public String toString() {
		return currencyCode;
	}

	private Object readResolve() {
		return getInstance(currencyCode);
	}

	// Specific
	public String getDisplayName(Locale locale) {
		return "USD";
	}

	public String getSymbol(Locale locale) {
		if (locale.getCountry().length() == 0) return currencyCode;
		return "$";
	}

	public int getDefaultFractionDigits() {
		return 2;
	}
}
