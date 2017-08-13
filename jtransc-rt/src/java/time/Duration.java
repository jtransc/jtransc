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
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;

import static java.time._TimeConsts.*;

public final class Duration implements TemporalAmount, Comparable<Duration>, Serializable {
	public static final Duration ZERO = new Duration(0, 0);

	private final long seconds;
	private final int nanos;

	private Duration(long seconds, int nanos) {
		this.seconds = seconds;
		this.nanos = nanos;
	}

	public static Duration ofDays(long days) {
		return new Duration(days * SECONDS_IN_DAY, 0);
	}

	public static Duration ofHours(long hours) {
		return new Duration(hours * SECONDS_IN_HOUR, 0);
	}

	public static Duration ofMinutes(long minutes) {
		return new Duration(minutes * SECONDS_IN_MINUTE, 0);
	}

	public static Duration ofSeconds(long seconds) {
		return new Duration(seconds, 0);
	}

	public static Duration ofSeconds(long seconds, long nanoAdjustment) {
		return new Duration(seconds + nanoAdjustment / NANOSECONDS_IN_SECOND, (int) (nanoAdjustment % NANOSECONDS_IN_SECOND));
	}

	native public static Duration ofMillis(long millis);

	public static Duration ofNanos(long nanos) {
		return ofSeconds(0, nanos);
	}

	native public static Duration of(long amount, TemporalUnit unit);

	native public static Duration from(TemporalAmount amount);

	native public static Duration parse(CharSequence text);

	native public static Duration between(Temporal startInclusive, Temporal endExclusive);

	native public long get(TemporalUnit unit);

	native public List<TemporalUnit> getUnits();

	public boolean isZero() {
		return seconds == 0 && nanos == 0;
	}

	public boolean isNegative() {
		return seconds < 0;
	}

	public long getSeconds() {
		return seconds;
	}

	public int getNano() {
		return nanos;
	}

	public Duration withSeconds(long seconds) {
		return new Duration(seconds, nanos);
	}

	public Duration withNanos(int nanoOfSecond) {
		return new Duration(seconds, nanos);
	}

	private Duration _plus(long seconds, long nanos) {
		return Duration.ofSeconds(this.seconds + seconds, this.nanos + nanos);
	}

	private Duration _minus(long seconds, long nanos) {
		return Duration.ofSeconds(this.seconds - seconds, this.nanos - nanos);
	}

	public Duration plus(Duration delta) {
		return _plus(delta.seconds, delta.nanos);
	}

	native public Duration plus(long delta, TemporalUnit unit);

	public Duration plusDays(long delta) {
		return _plus(delta * SECONDS_IN_DAY, 0);
	}

	public Duration plusHours(long delta) {
		return _plus(delta * SECONDS_IN_HOUR, 0);
	}

	public Duration plusMinutes(long delta) {
		return _plus(delta * SECONDS_IN_MINUTE, 0);
	}

	public Duration plusSeconds(long delta) {
		return _plus(delta, 0);
	}

	public Duration plusMillis(long delta) {
		return _plus(0, delta * NANOSECONDS_IN_MILLISECOND);
	}

	public Duration plusNanos(long delta) {
		return _plus(0, delta);
	}

	public Duration minus(Duration delta) {
		return _minus(delta.seconds, delta.nanos);
	}

	native public Duration minus(long delta, TemporalUnit unit);

	public Duration minusDays(long delta) {
		return _minus(delta * SECONDS_IN_DAY, 0);
	}

	public Duration minusHours(long delta) {
		return _minus(delta * SECONDS_IN_HOUR, 0);
	}

	public Duration minusMinutes(long delta) {
		return _minus(delta * SECONDS_IN_MINUTE, 0);
	}

	public Duration minusSeconds(long delta) {
		return _minus(delta, 0);
	}

	public Duration minusMillis(long delta) {
		return _minus(0, delta * NANOSECONDS_IN_MILLISECOND);
	}

	public Duration minusNanos(long delta) {
		return _minus(0, delta);
	}

	public Duration multipliedBy(long multiplicand) {
		return ofSeconds(this.seconds * multiplicand, this.nanos * multiplicand);
	}

	public Duration dividedBy(long divisor) {
		return ofSeconds(this.seconds * divisor, this.nanos * divisor);
	}

	native public Duration negated();

	native public Duration abs();

	native public Temporal addTo(Temporal temporal);

	native public Temporal subtractFrom(Temporal temporal);

	public long toDays() {
		return seconds / SECONDS_IN_DAY;
	}

	public long toHours() {
		return seconds / SECONDS_IN_HOUR;
	}

	public long toMinutes() {
		return seconds / SECONDS_IN_MINUTE;
	}

	public long toMillis() {
		return seconds * 1000 + nanos / NANOSECONDS_IN_MILLISECOND;
	}

	public long toNanos() {
		return seconds * NANOSECONDS_IN_SECOND + nanos;
	}

	native public int compareTo(Duration otherDuration);

	native public boolean equals(Object otherDuration);

	public int hashCode() {
		return (int) (seconds + nanos);
	}

	native public String toString();
}
