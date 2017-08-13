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

import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.time.temporal.*;
import java.time.zone.ZoneRules;

public final class ZoneOffset extends ZoneId implements TemporalAccessor, TemporalAdjuster, Comparable<ZoneOffset>, Serializable {
	public static final ZoneOffset UTC = new ZoneOffset(0);
	public static final ZoneOffset MIN = new ZoneOffset(-18 * 3600);
	public static final ZoneOffset MAX = new ZoneOffset(18 * 3600);

	private final int totalSeconds;

	private ZoneOffset(int totalSeconds) {
		this.totalSeconds = totalSeconds;
	}

	private static ZoneOffset _of(int totalHours, int totalMinutes, int totalSeconds) {
		int finalSeconds = totalHours * 3600 + totalMinutes * 60 + totalSeconds;
		switch (finalSeconds) {
			case 0:
				return UTC;
			default:
				return new ZoneOffset(finalSeconds);
		}
	}

	public static ZoneOffset of(String offsetId) {
		return UTC;
	}

	public static ZoneOffset ofHours(int hours) {
		return _of(hours, 0, 0);
	}

	public static ZoneOffset ofHoursMinutes(int hours, int minutes) {
		return _of(hours, minutes % 60, 0);
	}

	public static ZoneOffset ofHoursMinutesSeconds(int hours, int minutes, int seconds) {
		return _of(hours, minutes % 60, seconds % 60);
	}

	public static ZoneOffset ofTotalSeconds(int totalSeconds) {
		return  _of(0, 0, totalSeconds);
	}

	native public static ZoneOffset from(TemporalAccessor temporal);

	public int getTotalSeconds() {
		return totalSeconds;
	}

	public String getId() {
		return "UTC";
	}

	native public ZoneRules getRules();

	native public boolean isSupported(TemporalField field);

	native public ValueRange range(TemporalField field);

	native public int get(TemporalField field);

	native public long getLong(TemporalField field);

	native public <R> R query(TemporalQuery<R> query);

	native public Temporal adjustInto(Temporal temporal);

	native void write(DataOutput out) throws IOException;

	native public int compareTo(ZoneOffset other);

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
