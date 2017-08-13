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

public abstract class Clock {
	protected Clock() {
	}

	static private Clock systemUTC;

	public static Clock systemUTC() {
		if (systemUTC == null) {
			systemUTC = new SimplifiedClock() {
				public long millis() {
					return System.currentTimeMillis();
				}
			};
		}
		return systemUTC;
	}

	public static Clock systemDefaultZone() {
		return systemUTC();
	}

	public static Clock system(ZoneId zone) {
		return systemUTC();
	}

	public static Clock tickSeconds(ZoneId zone) {
		return tick(system(zone), Duration.ofSeconds(1));
	}

	public static Clock tickMinutes(ZoneId zone) {
		return tick(system(zone), Duration.ofMinutes(1));
	}

	public static Clock tick(final Clock baseClock, final Duration tickDuration) {
		final long tickMillis = tickDuration.toMillis();
		return new SimplifiedClock() {
			@Override
			public long millis() {
				return (baseClock.millis() / tickMillis) * tickMillis;
			}
		};
	}

	public static Clock fixed(final Instant fixedInstant, final ZoneId zone) {
		final long fixedMillis = fixedInstant.toEpochMilli();
		return new SimplifiedClock() {
			@Override
			public ZoneId getZone() {
				return zone;
			}

			@Override
			public long millis() {
				return fixedMillis;
			}
		};
	}

	public static Clock offset(final Clock baseClock, final Duration offsetDuration) {
		final long add = offsetDuration.toMillis();
		return new SimplifiedClock() {
			@Override
			public long millis() {
				return baseClock.millis() + add;
			}
		};
	}

	public ZoneId getZone() {
		return ZoneId.systemDefault();
	}

	public abstract Clock withZone(ZoneId zone);

	public long millis() {
		return instant().toEpochMilli();
	}

	public abstract Instant instant();

	public boolean equals(Object obj) {
		return this == obj;
	}

	public int hashCode() {
		return System.identityHashCode(this);
	}

	static private abstract class SimplifiedClock extends Clock {
		@Override
		public ZoneId getZone() {
			return ZoneId.systemDefault();
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		abstract public long millis();

		@Override
		public Instant instant() {
			return Instant.ofEpochMilli(millis());
		}
	}
}