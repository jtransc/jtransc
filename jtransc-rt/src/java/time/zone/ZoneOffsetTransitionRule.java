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
package java.time.zone;

import java.io.Serializable;
import java.time.*;

public final class ZoneOffsetTransitionRule implements Serializable {
	native public static ZoneOffsetTransitionRule of(
		Month month,
		int dayOfMonthIndicator,
		DayOfWeek dayOfWeek,
		LocalTime time,
		boolean timeEndOfDay,
		TimeDefinition timeDefnition,
		ZoneOffset standardOffset,
		ZoneOffset offsetBefore,
		ZoneOffset offsetAfter
	);


	native public Month getMonth();

	native public int getDayOfMonthIndicator();

	native public DayOfWeek getDayOfWeek();

	native public LocalTime getLocalTime();

	native public boolean isMidnightEndOfDay();

	native public TimeDefinition getTimeDefinition();

	native public ZoneOffset getStandardOffset();

	native public ZoneOffset getOffsetBefore();

	native public ZoneOffset getOffsetAfter();

	native public ZoneOffsetTransition createTransition(int year);

	native public boolean equals(Object otherRule);

	native public int hashCode();

	native public String toString();

	public enum TimeDefinition {
		UTC,
		WALL,
		STANDARD;

		native public LocalDateTime createDateTime(LocalDateTime dateTime, ZoneOffset standardOffset, ZoneOffset wallOffset);
	}
}
