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

	native public static Clock systemUTC();

	native public static Clock systemDefaultZone();

	native public static Clock system(ZoneId zone);

	native public static Clock tickSeconds(ZoneId zone);

	native public static Clock tickMinutes(ZoneId zone);

	native public static Clock tick(Clock baseClock, Duration tickDuration);

	native public static Clock fixed(Instant fixedInstant, ZoneId zone);

	native public static Clock offset(Clock baseClock, Duration offsetDuration);

	public abstract ZoneId getZone();

	public abstract Clock withZone(ZoneId zone);

	public long millis() {
		return instant().toEpochMilli();
	}

	public abstract Instant instant();

	native public boolean equals(Object obj);

	native public int hashCode();
}