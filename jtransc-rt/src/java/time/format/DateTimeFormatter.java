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

import java.text.Format;
import java.text.ParsePosition;
import java.time.Period;
import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.util.Locale;
import java.util.Set;

public final class DateTimeFormatter {
	public static final DateTimeFormatter ISO_LOCAL_DATE = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_OFFSET_DATE = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_DATE = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_LOCAL_TIME = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_OFFSET_TIME = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_TIME = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_LOCAL_DATE_TIME = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_OFFSET_DATE_TIME = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_ZONED_DATE_TIME = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_DATE_TIME = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_ORDINAL_DATE = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_WEEK_DATE = new DateTimeFormatter();
	public static final DateTimeFormatter ISO_INSTANT = new DateTimeFormatter();
	public static final DateTimeFormatter BASIC_ISO_DATE = new DateTimeFormatter();
	public static final DateTimeFormatter RFC_1123_DATE_TIME = new DateTimeFormatter();

	private Locale locale = Locale.getDefault();

	private DateTimeFormatter() {
	}

	public static DateTimeFormatter ofPattern(String pattern) {
		return ofPattern(pattern, Locale.getDefault());
	}

	public static DateTimeFormatter ofPattern(String pattern, Locale locale) {
		return new DateTimeFormatter();
	}

	public static DateTimeFormatter ofLocalizedDate(FormatStyle dateStyle) {
		return new DateTimeFormatter();
	}

	public static DateTimeFormatter ofLocalizedTime(FormatStyle timeStyle) {
		return new DateTimeFormatter();
	}

	public static DateTimeFormatter ofLocalizedDateTime(FormatStyle dateTimeStyle) {
		return new DateTimeFormatter();
	}

	public static DateTimeFormatter ofLocalizedDateTime(FormatStyle dateStyle, FormatStyle timeStyle) {
		return new DateTimeFormatter();
	}

	native public static final TemporalQuery<Period> parsedExcessDays();

	native public static final TemporalQuery<Boolean> parsedLeapSecond();

	public Locale getLocale() {
		return locale;
	}

	native public DateTimeFormatter withLocale(Locale locale);

	native public DecimalStyle getDecimalStyle();

	native public DateTimeFormatter withDecimalStyle(DecimalStyle decimalStyle);

	native public Chronology getChronology();

	native public DateTimeFormatter withChronology(Chronology chrono);

	native public ZoneId getZone();

	native public DateTimeFormatter withZone(ZoneId zone);

	native public ResolverStyle getResolverStyle();

	native public DateTimeFormatter withResolverStyle(ResolverStyle resolverStyle);

	native public Set<TemporalField> getResolverFields();

	native public DateTimeFormatter withResolverFields(TemporalField... resolverFields);

	native public DateTimeFormatter withResolverFields(Set<TemporalField> resolverFields);

	native public String format(TemporalAccessor temporal);

	native public void formatTo(TemporalAccessor temporal, Appendable appendable);

	native public TemporalAccessor parse(CharSequence text);

	native public TemporalAccessor parse(CharSequence text, ParsePosition position);

	native public <T> T parse(CharSequence text, TemporalQuery<T> query);

	native public TemporalAccessor parseBest(CharSequence text, TemporalQuery<?>... queries);

	native public TemporalAccessor parseUnresolved(CharSequence text, ParsePosition position);

	native public Format toFormat();

	native public Format toFormat(TemporalQuery<?> parseQuery);

	native public String toString();
}
