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
package java.time;

import java.io.Serializable;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.DAYS;

public final class Period implements ChronoPeriod, Serializable {
	public static final Period ZERO = new Period(0, 0, 0);

	private final int years;
	private final int months;
	private final int days;

	private Period(int years, int months, int days) {
		this.years = years;
		this.months = months;
		this.days = days;
	}

	public static Period _of(int adjYears, int adjMonths, int adjDays) {
		if (adjYears == 0 && adjMonths == 0 && adjDays == 0) return ZERO;
		// @TODO: Is this necessary?
		//adjMonths += adjDays / 30;
		//adjDays %= 30;
		//adjYears += adjMonths / 12;
		//adjMonths %= 12;
		return new Period(adjYears, adjMonths, adjDays);
	}

	public static Period ofYears(int years) {
		return _of(years, 0, 0);
	}

	public static Period ofMonths(int months) {
		return _of(0, months, 0);
	}

	public static Period ofWeeks(int weeks) {
		return _of(0, 0, weeks * 7);
	}

	public static Period ofDays(int days) {
		return _of(0, 0, days);
	}

	public static Period of(int years, int months, int days) {
		return _of(years, months, days);
	}

	public static Period from(TemporalAmount amount) {
		return _of((int) amount.get(ChronoUnit.YEARS), (int) amount.get(ChronoUnit.MONTHS), (int) amount.get(ChronoUnit.DAYS));
	}

	public static Period parse(CharSequence text) {
		final Pattern pattern = Pattern.compile("^(-?)P((-?[0-9]+)Y)?((-?[0-9]+)M)?((-?[0-9]+)D)?$");
		final Matcher matcher = pattern.matcher(text);
		if (!matcher.matches()) throw new DateTimeParseException("Text cannot be parsed to a Period", text, 0);
		final boolean globalNegate = Objects.equals(matcher.group(1), "-");
		final String yearsStr = matcher.group(3);
		final String monthsStr = matcher.group(5);
		final String daysStr = matcher.group(7);
		final int years = (yearsStr != null) ? Integer.parseInt(yearsStr) : 0;
		final int months = (monthsStr != null) ? Integer.parseInt(monthsStr) : 0;
		final int days = (daysStr != null) ? Integer.parseInt(daysStr) : 0;
		final Period period = _of(years, months, days);
		return globalNegate ? period.negated() : period;
	}

	public static Period between(LocalDate startDateInclusive, LocalDate endDateExclusive) {
		// @TODO: Probably not exact in some edge cases, but much simpler for jtransc.
		long start = startDateInclusive.toEpochDay();
		long end = endDateExclusive.toEpochDay();
		return _of(0, 0, (int) (end - start)).normalized();
	}

	public long get(TemporalUnit unit) {
		if (!(unit instanceof ChronoUnit)) return 0L;

		switch ((ChronoUnit) unit) {
			case DAYS:
				return days;
			case MONTHS:
				return months;
			case YEARS:
				return years;
			default:
				return 0L;
		}
	}

	public List<TemporalUnit> getUnits() {
		return Arrays.asList(ChronoUnit.DAYS, ChronoUnit.MONTHS, ChronoUnit.YEARS);
	}

	public IsoChronology getChronology() {
		return IsoChronology.INSTANCE;
	}

	private int getTotalDays() {
		return getYears() * 365 + getMonths() * 30 + getDays();
	}

	public boolean isZero() {
		return getTotalDays() == 0;
	}

	public boolean isNegative() {
		return getTotalDays() < 0;
	}

	public int getYears() {
		return years;
	}

	public int getMonths() {
		return months;
	}

	public int getDays() {
		return days;
	}

	public Period withYears(int years) {
		return _of(years, months, days);
	}

	public Period withMonths(int months) {
		return _of(years, months, days);
	}

	public Period withDays(int days) {
		return _of(years, months, days);
	}

	private Period _plus(long yearsDelta, long monthsDelta, long daysDelta) {
		return _of((int) (years + yearsDelta), (int) (months + monthsDelta), (int) (days + daysDelta));
	}

	private Period _minus(long yearsDelta, long monthsDelta, long daysDelta) {
		return _of((int) (years - yearsDelta), (int) (months - monthsDelta), (int) (days - daysDelta));
	}

	public Period plus(TemporalAmount delta) {
		return _plus(0, 0, delta.get(DAYS));
	}

	public Period plusYears(long delta) {
		return _plus(delta, 0, 0);
	}

	public Period plusMonths(long delta) {
		return _plus(0, delta, 0);
	}

	public Period plusDays(long delta) {
		return _plus(0, 0, delta);
	}

	public Period minus(TemporalAmount delta) {
		return _minus(0, 0, delta.get(DAYS));
	}

	public Period minusYears(long delta) {
		return _minus(delta, 0, 0);
	}

	public Period minusMonths(long delta) {
		return _minus(0, delta, 0);
	}

	public Period minusDays(long delta) {
		return _minus(0, 0, delta);
	}

	public Period multipliedBy(int scalar) {
		return _of(years * scalar, months * scalar, days * scalar);
	}

	public Period negated() {
		return _of(-years, -months, -days);
	}

	public Period normalized() {
		// @TODO: Should it be like this?
		//adjMonths += adjDays / 30;
		//adjDays %= 30;
		//adjYears += adjMonths / 12;
		//adjMonths %= 12;
		return this;
	}

	public long toTotalMonths() {
		return (years / 12) + months + days * 30;
	}

	public Temporal addTo(Temporal temporal) {
		// @TODO: Probably enough accurate for jtransc
		return temporal.plus(this.getTotalDays(), ChronoUnit.DAYS);
	}

	public Temporal subtractFrom(Temporal temporal) {
		// @TODO: Probably enough accurate for jtransc
		return temporal.plus(-this.getTotalDays(), ChronoUnit.DAYS);
	}

	public boolean equals(Object obj) {
		return this == obj;
	}

	public int hashCode() {
		return getTotalDays();
	}

	public String toString() {
		if (isZero()) return "P0D";
		String out = "P";
		if (years != 0) out += years + "Y";
		if (months != 0) out += months + "M";
		if (days != 0) out += days + "D";
		return out;
	}
}
