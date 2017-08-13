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

import com.jtransc.IntegerUtils;

import java.time.format.TextStyle;
import java.time.temporal.*;
import java.util.Locale;

public enum Month implements TemporalAccessor, TemporalAdjuster {
	JANUARY,
	FEBRUARY,
	MARCH,
	APRIL,
	MAY,
	JUNE,
	JULY,
	AUGUST,
	SEPTEMBER,
	OCTOBER,
	NOVEMBER,
	DECEMBER;

	public static Month of(int month) {
		return values()[month - 1];
	}

	native public static Month from(TemporalAccessor temporal);

	public int getValue() {
		return ordinal() + 1;
	}

	public String getDisplayName(TextStyle style, Locale locale) {
		return name();
	}

	native public boolean isSupported(TemporalField field);

	native public ValueRange range(TemporalField field);

	public long getLong(TemporalField field) {
		return (field == ChronoField.MONTH_OF_YEAR) ? getValue() : 0L;
	}

	private Month _plus(long months) {
		return of(IntegerUtils.umod((int) (ordinal() + months), 7));
	}

	public Month plus(long months) {
		return _plus(months);
	}

	public Month minus(long months) {
		return _plus(-months);
	}

	public int length(boolean leapYear) {
		switch (this) {
			case FEBRUARY:
				return leapYear ? 29 : 28;
			case APRIL:
			case JUNE:
			case SEPTEMBER:
			case NOVEMBER:
				return 30;
			default:
				return 31;
		}
	}

	public int minLength() {
		return Math.min(length(false), length(true));
	}

	public int maxLength() {
		return Math.min(length(false), length(true));
	}

	public int firstDayOfYear(boolean leapYear) {
		// We can pre-compute this if required
		int count = 1;
		for (Month value : values()) {
			count += length(leapYear);
			if (value == this) break;
		}
		return count;
	}

	public Month firstMonthOfQuarter() {
		return values()[this.ordinal() / 3 * 3];
	}

	native public <R> R query(TemporalQuery<R> query);

	native public Temporal adjustInto(Temporal temporal);
}
