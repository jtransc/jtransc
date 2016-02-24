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

package java.text;

import java.io.Serializable;
import java.util.Currency;
import java.util.Locale;

public class DecimalFormatSymbols implements Cloneable, Serializable {
	public DecimalFormatSymbols() {

	}

	public DecimalFormatSymbols(Locale locale) {

	}

	native public static Locale[] getAvailableLocales();

	native public static final DecimalFormatSymbols getInstance();

	native public static final DecimalFormatSymbols getInstance(Locale locale);

	native public char getZeroDigit();

	native public void setZeroDigit(char zeroDigit);

	native public char getGroupingSeparator();

	native public void setGroupingSeparator(char groupingSeparator);

	native public char getDecimalSeparator();

	native public void setDecimalSeparator(char decimalSeparator);

	native public char getPerMill();

	native public void setPerMill(char perMill);

	native public char getPercent();

	native public void setPercent(char percent);

	native public char getDigit();

	native public void setDigit(char digit);

	native public char getPatternSeparator();

	native public void setPatternSeparator(char patternSeparator);

	native public String getInfinity();

	native public void setInfinity(String infinity);

	native public String getNaN();

	native public void setNaN(String NaN);

	native public char getMinusSign();

	native public void setMinusSign(char minusSign);

	native public String getCurrencySymbol();

	native public void setCurrencySymbol(String currency);

	native public String getInternationalCurrencySymbol();

	native public void setInternationalCurrencySymbol(String currencyCode);

	native public Currency getCurrency();

	native public void setCurrency(Currency currency);

	native public char getMonetaryDecimalSeparator();

	native public void setMonetaryDecimalSeparator(char sep);

	native public String getExponentSeparator();

	native public void setExponentSeparator(String exp);

	@Override
	native public Object clone();

	@Override
	native public boolean equals(Object obj);

	@Override
	native public int hashCode();
}
