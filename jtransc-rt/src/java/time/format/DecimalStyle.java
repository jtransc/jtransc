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
package java.time.format;

import java.util.Locale;
import java.util.Set;

public final class DecimalStyle {
	public static final DecimalStyle STANDARD = new DecimalStyle('0', '+', '-', '.');
	private final char zeroDigit;
	private final char positiveSign;
	private final char negativeSign;
	private final char decimalSeparator;

	private DecimalStyle(char zeroChar, char positiveSignChar, char negativeSignChar, char decimalPointChar) {
		this.zeroDigit = zeroChar;
		this.positiveSign = positiveSignChar;
		this.negativeSign = negativeSignChar;
		this.decimalSeparator = decimalPointChar;
	}

	native public static Set<Locale> getAvailableLocales();

	native public static DecimalStyle ofDefaultLocale();

	native public static DecimalStyle of(Locale locale);

	native public char getZeroDigit();

	native public DecimalStyle withZeroDigit(char zeroDigit);

	native public char getPositiveSign();

	native public DecimalStyle withPositiveSign(char positiveSign);

	native public char getNegativeSign();

	native public DecimalStyle withNegativeSign(char negativeSign);

	native public char getDecimalSeparator();

	native public DecimalStyle withDecimalSeparator(char decimalSeparator);

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
