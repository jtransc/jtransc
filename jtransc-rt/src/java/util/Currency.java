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

public final class Currency implements Serializable {
	native public static Currency getInstance(String currencyCode);

	native public static Currency getInstance(Locale locale);

	native public static Set<Currency> getAvailableCurrencies();

	native public String getCurrencyCode();

	native public String getSymbol();

	native public String getSymbol(Locale locale);

	native public int getDefaultFractionDigits();

	native public int getNumericCode();

	native public String getDisplayName();

	native public String getDisplayName(Locale locale);

	@Override
	native public String toString();
}
