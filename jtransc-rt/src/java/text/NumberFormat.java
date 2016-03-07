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

import java.io.InvalidObjectException;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class NumberFormat extends Format {
	public static final int INTEGER_FIELD = 0;
	public static final int FRACTION_FIELD = 1;

	protected NumberFormat() {
	}

	@Override
	native public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos);

	@Override
	public final Object parseObject(String source, ParsePosition pos) {
		return parse(source, pos);
	}

	public final String format(double value) {
		return format(value, new StringBuffer(), new FieldPosition(0)).toString();
	}

	public final String format(long value) {
		return format(value, new StringBuffer(), new FieldPosition(0)).toString();
	}

	public abstract StringBuffer format(double value, StringBuffer toAppendTo, FieldPosition pos);

	public abstract StringBuffer format(long value, StringBuffer toAppendTo, FieldPosition pos);

	public abstract Number parse(String source, ParsePosition parsePosition);

	public Number parse(String source) throws ParseException {
		ParsePosition parsePosition = new ParsePosition(0);
		Number result = parse(source, parsePosition);
		if (parsePosition.index == 0) {
			throw new ParseException("Unparseable number: \"" + source + "\"", parsePosition.errorIndex);
		}
		return result;
	}

	native public boolean isParseIntegerOnly();

	native public void setParseIntegerOnly(boolean value);

	native public static NumberFormat getInstance();

	native public static NumberFormat getInstance(Locale inLocale);

	native public static NumberFormat getNumberInstance();

	native public static NumberFormat getNumberInstance(Locale inLocale);

	native public static NumberFormat getIntegerInstance();

	native public static NumberFormat getIntegerInstance(Locale inLocale);

	native public static NumberFormat getCurrencyInstance();

	native public static NumberFormat getCurrencyInstance(Locale inLocale);

	native public static NumberFormat getPercentInstance();

	native public static NumberFormat getPercentInstance(Locale inLocale);

	public native static Locale[] getAvailableLocales();

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

	native public boolean isGroupingUsed();

	native public void setGroupingUsed(boolean newValue);

	native public int getMaximumIntegerDigits();

	native public void setMaximumIntegerDigits(int newValue);

	native public int getMinimumIntegerDigits();

	native public void setMinimumIntegerDigits(int newValue);

	native public int getMaximumFractionDigits();

	native public void setMaximumFractionDigits(int newValue);

	native public int getMinimumFractionDigits();

	native public void setMinimumFractionDigits(int newValue);

	public Currency getCurrency() {
		throw new UnsupportedOperationException();
	}

	public void setCurrency(Currency currency) {
		throw new UnsupportedOperationException();
	}

	public RoundingMode getRoundingMode() {
		throw new UnsupportedOperationException();
	}

	public void setRoundingMode(RoundingMode roundingMode) {
		throw new UnsupportedOperationException();
	}

	public static class Field extends Format.Field {
		private static final Map<String, Field> instances = new HashMap<String, Field>(11);

		protected Field(String name) {
			super(name);
			if (this.getClass() == NumberFormat.Field.class) {
				instances.put(name, this);
			}
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
