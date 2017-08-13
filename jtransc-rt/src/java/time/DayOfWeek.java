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

import java.time.format.TextStyle;
import java.time.temporal.*;
import java.util.Locale;

public enum DayOfWeek implements TemporalAccessor, TemporalAdjuster {
	MONDAY,
	TUESDAY,
	WEDNESDAY,
	THURSDAY,
	FRIDAY,
	SATURDAY,
	SUNDAY;

	native public static DayOfWeek of(int dayOfWeek);

	native public static DayOfWeek from(TemporalAccessor temporal);

	native public int getValue();

	native public String getDisplayName(TextStyle style, Locale locale);

	native public boolean isSupported(TemporalField field);

	native public ValueRange range(TemporalField field);

	native public int get(TemporalField field);

	native public long getLong(TemporalField field);

	native public DayOfWeek plus(long days);

	native public DayOfWeek minus(long days);

	native public <R> R query(TemporalQuery<R> query);

	native public Temporal adjustInto(Temporal temporal);
}
