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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public final class ZoneRules implements Serializable {
	native public static ZoneRules of(
		ZoneOffset baseStandardOffset,
		ZoneOffset baseWallOffset,
		List<ZoneOffsetTransition> standardOffsetTransitionList,
		List<ZoneOffsetTransition> transitionList,
		List<ZoneOffsetTransitionRule> lastRules
	);

	public static ZoneRules of(ZoneOffset offset) {
		return offset.getRules();
	}

	native public boolean isFixedOffset();

	native public ZoneOffset getOffset(Instant instant);

	native public ZoneOffset getOffset(LocalDateTime localDateTime);

	native public List<ZoneOffset> getValidOffsets(LocalDateTime localDateTime);

	native public ZoneOffsetTransition getTransition(LocalDateTime localDateTime);

	native public ZoneOffset getStandardOffset(Instant instant);

	native public Duration getDaylightSavings(Instant instant);

	native public boolean isDaylightSavings(Instant instant);

	native public boolean isValidOffset(LocalDateTime localDateTime, ZoneOffset offset);

	native public ZoneOffsetTransition nextTransition(Instant instant);

	native public ZoneOffsetTransition previousTransition(Instant instant);

	native public List<ZoneOffsetTransition> getTransitions();

	native public List<ZoneOffsetTransitionRule> getTransitionRules();

	native public boolean equals(Object otherRules);

	native public int hashCode();

	native public String toString();
}
