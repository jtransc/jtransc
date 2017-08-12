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

public final class ZoneOffsetTransition implements Comparable<ZoneOffsetTransition>, Serializable {
	native public static ZoneOffsetTransition of(LocalDateTime transition, ZoneOffset offsetBefore, ZoneOffset offsetAfter);

	native public Instant getInstant();

	native public long toEpochSecond();

	native public LocalDateTime getDateTimeBefore();

	native public LocalDateTime getDateTimeAfter();

	native public ZoneOffset getOffsetBefore();

	native public ZoneOffset getOffsetAfter();

	native public Duration getDuration();

	native public boolean isGap();

	native public boolean isOverlap();

	native public boolean isValidOffset(ZoneOffset offset);

	native List<ZoneOffset> getValidOffsets();

	native public int compareTo(ZoneOffsetTransition transition);

	native public boolean equals(Object other);

	native public int hashCode();

	native public String toString();
}
