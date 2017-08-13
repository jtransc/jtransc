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

import com.jtransc.text.JTranscLocale;
import com.jtransc.text.JTranscStringTools;
import com.jtransc.util.JTranscStrings;

import java.io.InvalidObjectException;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class NumberFormat extends Format {
	public static final int INTEGER_FIELD = 0;
	public static final int FRACTION_FIELD = 1;

	private boolean parseIntegerOnly = false;
	private boolean groupingUsed = true;
	private int maximumIntegerDigits = 100;
	private int minimumIntegerDigits = 1;
	private int maximumFractionDigits = 100;
	private int minimumFractionDigits = 0;
	private RoundingMode roundingMode = RoundingMode.HALF_EVEN;
	private Locale locale;
	private int intGroupDigits = 3;
	private String intGroupSeparator = ".";
	private String commaSeparator = ",";
	private Currency currency;

	protected NumberFormat() {
		this(Locale.getDefault());
	}


	private NumberFormat(Locale locale) {
		this.locale = locale;
		//System.out.println(locale.getLanguage());

		// https://docs.oracle.com/cd/E19455-01/806-0169/overview-9/index.html
		intGroupDigits = JTranscLocale.getIntNumberOfDigits(locale);
		intGroupSeparator = JTranscLocale.getGroupSeparator(locale);
		commaSeparator = JTranscLocale.getDecimalSeparator(locale);
	}

	@Override
	public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
		toAppendTo.append(format((Number) number));
		return toAppendTo;
	}

	@Override
	public final Object parseObject(String source, ParsePosition pos) {
		return parse(source, pos);
	}

	private String format(Number value) {
		return _format(value.toString());
	}

	public final String format(double value) {
		return _format(JTranscStringTools.toString(value));
	}

	public final String format(long value) {
		return _format(Long.toString(value));
	}

	private String _format(String value) {
		// @TODO: Here we add dots and/or commas
		return _formatInt(value);
	}

	private String _formatInt(String value) {
		return JTranscStrings.join(JTranscStrings.splitInChunksRightToLeft(value, intGroupDigits), intGroupSeparator);
	}

	public StringBuffer format(double value, StringBuffer toAppendTo, FieldPosition pos) {
		toAppendTo.append(format(value));
		return toAppendTo;
	}

	public StringBuffer format(long value, StringBuffer toAppendTo, FieldPosition pos) {
		toAppendTo.append(format(value));
		return toAppendTo;
	}

	public native Number parse(String source, ParsePosition parsePosition);

	public Number parse(String source) throws ParseException {
		ParsePosition parsePosition = new ParsePosition(0);
		Number result = parse(source, parsePosition);
		if (parsePosition.index == 0) {
			throw new ParseException("Unparseable number: \"" + source + "\"", parsePosition.errorIndex);
		}
		return result;
	}

	public boolean isParseIntegerOnly() {
		return parseIntegerOnly;
	}

	public void setParseIntegerOnly(boolean value) {
		this.parseIntegerOnly = value;
	}

	public static NumberFormat getInstance() {
		return new NumberFormat();
	}

	public static NumberFormat getInstance(Locale locale) {
		return new NumberFormat(locale);
	}

	public static NumberFormat getNumberInstance() {
		return new NumberFormat();
	}

	public static NumberFormat getNumberInstance(Locale locale) {
		return new NumberFormat(locale);
	}

	public static NumberFormat getIntegerInstance() {
		return new NumberFormat();
	}

	public static NumberFormat getIntegerInstance(Locale locale) {
		return new NumberFormat(locale);
	}

	public static NumberFormat getCurrencyInstance() {
		return new NumberFormat();
	}

	public static NumberFormat getCurrencyInstance(Locale inLocale) {
		return new NumberFormat();
	}

	public static NumberFormat getPercentInstance() {
		return getPercentInstance(Locale.getDefault(Locale.Category.FORMAT));
	}

	public static NumberFormat getPercentInstance(Locale locale) {
		return new NumberFormat(locale);
	}

	public static Locale[] getAvailableLocales() {
		return new Locale[] { Locale.ENGLISH };
	}

	@Override
	public int hashCode() {
		return getMaximumIntegerDigits() * 37 + getMaximumFractionDigits();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (getClass() != obj.getClass()) return false;
		NumberFormat that = (NumberFormat) obj;
		return (
			getMaximumIntegerDigits() == that.getMaximumIntegerDigits() &&
				getMinimumIntegerDigits() == that.getMinimumIntegerDigits() &&
				getMaximumFractionDigits() == that.getMaximumFractionDigits() &&
				getMinimumFractionDigits() == that.getMinimumFractionDigits() &&
				isGroupingUsed() == that.isGroupingUsed() &&
				isParseIntegerOnly() == that.isParseIntegerOnly()
		);
	}

	@Override
	public Object clone() {
		NumberFormat other = (NumberFormat) super.clone();
		return other;
	}

	public boolean isGroupingUsed() {
		return groupingUsed;
	}

	public void setGroupingUsed(boolean newValue) {
		groupingUsed = newValue;
	}

	public int getMaximumIntegerDigits() {
		return maximumIntegerDigits;
	}

	public void setMaximumIntegerDigits(int newValue) {
		maximumIntegerDigits = newValue;
	}

	public int getMinimumIntegerDigits() {
		return minimumIntegerDigits;
	}

	public void setMinimumIntegerDigits(int newValue) {
		minimumIntegerDigits = newValue;
	}

	public int getMaximumFractionDigits() {
		return maximumFractionDigits;
	}

	public void setMaximumFractionDigits(int newValue) {
		maximumFractionDigits = newValue;
	}

	public int getMinimumFractionDigits() {
		return minimumFractionDigits;
	}

	public void setMinimumFractionDigits(int newValue) {
		minimumFractionDigits = newValue;
	}

	public Currency getCurrency() {
		if (currency == null) currency = Currency.getInstance(this.locale);
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public RoundingMode getRoundingMode() {
		return roundingMode;
	}

	public void setRoundingMode(RoundingMode roundingMode) {
		this.roundingMode = roundingMode;
	}

	public static class Field extends Format.Field {
		private static final Map<String, Field> instances = new HashMap<String, Field>(11);

		protected Field(String name) {
			super(name);
			instances.put(name, this);
		}

		@Override
		protected Object readResolve() throws InvalidObjectException {
			return instances.get(getName());
		}

		public static final Field INTEGER = new Field("integer");
		public static final Field FRACTION = new Field("fraction");
		public static final Field EXPONENT = new Field("exponent");
		public static final Field DECIMAL_SEPARATOR = new Field("decimal separator");
		public static final Field SIGN = new Field("sign");
		public static final Field GROUPING_SEPARATOR = new Field("grouping separator");
		public static final Field EXPONENT_SYMBOL = new Field("exponent symbol");
		public static final Field PERCENT = new Field("percent");
		public static final Field PERMILLE = new Field("per mille");
		public static final Field CURRENCY = new Field("currency");
		public static final Field EXPONENT_SIGN = new Field("exponent sign");
	}
}
