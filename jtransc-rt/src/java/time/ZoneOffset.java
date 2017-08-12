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
	public static final ZoneOffset UTC = ZoneOffset.ofTotalSeconds(0);
	public static final ZoneOffset MIN = ZoneOffset.ofTotalSeconds(-18 * 3600);
	public static final ZoneOffset MAX = ZoneOffset.ofTotalSeconds(18 * 3600);

	private ZoneOffset(int totalSeconds) {
		super();
	}

	native public static ZoneOffset of(String offsetId);

	native public static ZoneOffset ofHours(int hours);

	native public static ZoneOffset ofHoursMinutes(int hours, int minutes);

	native public static ZoneOffset ofHoursMinutesSeconds(int hours, int minutes, int seconds);

	native public static ZoneOffset from(TemporalAccessor temporal);

	native public static ZoneOffset ofTotalSeconds(int totalSeconds);

	native public int getTotalSeconds();

	native public String getId();

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
