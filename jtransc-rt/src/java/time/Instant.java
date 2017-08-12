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
import java.time.temporal.*;

public final class Instant implements Temporal, TemporalAdjuster, Comparable<Instant>, Serializable {
	public static final Instant EPOCH = new Instant(0, 0);

	public static final Instant MIN = Instant.ofEpochSecond(-31557014167219200L, 0);

	public static final Instant MAX = Instant.ofEpochSecond(31556889864403199L, 999_999_999);

	private final long seconds;
	private final int nanos;

	private Instant(long seconds, int nanos) {
		this.seconds = seconds;
		this.nanos = nanos;
	}

	public static Instant now() {
		return Clock.systemUTC().instant();
	}

	public static Instant now(Clock clock) {
		return clock.instant();
	}

	public static Instant ofEpochSecond(long epochSecond) {
		return new Instant(epochSecond, 0);
	}

	public static Instant ofEpochSecond(long epochSecond, long nanoAdjustment) {
		return new Instant(
			epochSecond + nanoAdjustment / 1000_000_000L,
			(int) (nanoAdjustment % 1000_000_000L)
		);
	}

	native public static Instant ofEpochMilli(long epochMilli);

	native public static Instant from(TemporalAccessor temporal);

	native public static Instant parse(final CharSequence text);

	@Override
	native public boolean isSupported(TemporalField field);

	@Override
	native public boolean isSupported(TemporalUnit unit);

	@Override  // override for Javadoc
	native public ValueRange range(TemporalField field);

	@Override  // override for Javadoc and performance
	native public int get(TemporalField field);

	@Override
	native public long getLong(TemporalField field);

	native public long getEpochSecond();

	native public int getNano();

	@Override
	native public Instant with(TemporalAdjuster adjuster);

	@Override
	native public Instant with(TemporalField field, long newValue);

	native public Instant truncatedTo(TemporalUnit unit);

	@Override
	native public Instant plus(TemporalAmount amountToAdd);

	@Override
	native public Instant plus(long amountToAdd, TemporalUnit unit);

	native public Instant plusSeconds(long secondsToAdd);

	native public Instant plusMillis(long millisToAdd);

	native public Instant plusNanos(long nanosToAdd);

	@Override
	native public Instant minus(TemporalAmount amountToSubtract);

	@Override
	native public Instant minus(long amountToSubtract, TemporalUnit unit);

	native public Instant minusSeconds(long secondsToSubtract);

	native public Instant minusMillis(long millisToSubtract);

	native public Instant minusNanos(long nanosToSubtract);

	@SuppressWarnings("unchecked")
	@Override
	native public <R> R query(TemporalQuery<R> query);

	@Override
	native public Temporal adjustInto(Temporal temporal);

	@Override
	native public long until(Temporal endExclusive, TemporalUnit unit);

	native public OffsetDateTime atOffset(ZoneOffset offset);

	native public ZonedDateTime atZone(ZoneId zone);

	native public long toEpochMilli();

	@Override
	native public int compareTo(Instant otherInstant);

	native public boolean isAfter(Instant otherInstant);

	native public boolean isBefore(Instant otherInstant);

	@Override
	native public boolean equals(Object otherInstant);

	@Override
	native public int hashCode();

	@Override
	native public String toString();
}
