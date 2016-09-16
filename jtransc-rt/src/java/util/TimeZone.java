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

// TODO: repackage this class, used by frameworks/base.

public abstract class TimeZone implements Serializable, Cloneable {
	public static final int SHORT = 0;
	public static final int LONG = 1;

	private static final TimeZone GMT = new SimpleTimeZone(0, "GMT");
	private static final TimeZone UTC = new SimpleTimeZone(0, "UTC");

	private static TimeZone defaultTimeZone = UTC;

	private String ID;

	public TimeZone() {
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}
	public static synchronized String[] getAvailableIDs() {
		return new String[] {"UTC"};
	}

	public static synchronized String[] getAvailableIDs(int offsetMillis) {
		return getAvailableIDs();
	}

	public static synchronized TimeZone getDefault() {
		return defaultTimeZone;
	}

	public final String getDisplayName() {
		return getDisplayName(false, LONG, Locale.getDefault());
	}

	public final String getDisplayName(Locale locale) {
		return getDisplayName(false, LONG, locale);
	}

	public final String getDisplayName(boolean daylightTime, int style) {
		return getDisplayName(daylightTime, style, Locale.getDefault());
	}

	public String getDisplayName(boolean daylightTime, int style, Locale locale) {
		return "UTC";
	}

	public String getID() {
		return ID;
	}

	public int getDSTSavings() {
		return useDaylightTime() ? 3600000 : 0;
	}

	public int getOffset(long time) {
		if (inDaylightTime(new Date(time))) {
			return getRawOffset() + getDSTSavings();
		}
		return getRawOffset();
	}

	public abstract int getOffset(int era, int year, int month, int day, int dayOfWeek, int timeOfDayMillis);

	public abstract int getRawOffset();

	public static synchronized TimeZone getTimeZone(String id) {
		return TimeZone.UTC;
	}

	public boolean hasSameRules(TimeZone timeZone) {
		if (timeZone == null) return false;
		return getRawOffset() == timeZone.getRawOffset();
	}

	public abstract boolean inDaylightTime(Date time);

	public static synchronized void setDefault(TimeZone timeZone) {
		defaultTimeZone = timeZone != null ? (TimeZone) timeZone.clone() : null;
	}

	public void setID(String id) {
		ID = id;
	}

	public abstract void setRawOffset(int offsetMillis);

	public abstract boolean useDaylightTime();
}
