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
package java.time.temporal;

import java.time.Duration;

public enum ChronoUnit implements TemporalUnit {
	NANOS("Nanos", Duration.ofNanos(1)),
	MICROS("Micros", Duration.ofNanos(1000)),
	MILLIS("Millis", Duration.ofNanos(1000_000)),
	SECONDS("Seconds", Duration.ofSeconds(1)),
	MINUTES("Minutes", Duration.ofSeconds(60)),
	HOURS("Hours", Duration.ofSeconds(3600)),
	HALF_DAYS("HalfDays", Duration.ofSeconds(43200)),
	DAYS("Days", Duration.ofSeconds(86400)),
	WEEKS("Weeks", Duration.ofSeconds(7 * 86400L)),
	MONTHS("Months", Duration.ofSeconds(31556952L / 12)),
	YEARS("Years", Duration.ofSeconds(31556952L)),
	DECADES("Decades", Duration.ofSeconds(31556952L * 10L)),
	CENTURIES("Centuries", Duration.ofSeconds(31556952L * 100L)),
	MILLENNIA("Millennia", Duration.ofSeconds(31556952L * 1000L)),
	ERAS("Eras", Duration.ofSeconds(31556952L * 1000_000_000L)),
	FOREVER("Forever", Duration.ofSeconds(Long.MAX_VALUE, 999_999_999));

	private final String name;
	private final Duration duration;

	ChronoUnit(String name, Duration estimatedDuration) {
		this.name = name;
		this.duration = estimatedDuration;
	}

	native public Duration getDuration();

	native public boolean isDurationEstimated();

	native public boolean isDateBased();

	native public boolean isTimeBased();

	native public boolean isSupportedBy(Temporal temporal);

	native public <R extends Temporal> R addTo(R temporal, long amount);

	native public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive);

	native public String toString();
}