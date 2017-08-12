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
package java.time.chrono;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;

public interface ChronoPeriod extends TemporalAmount {
	static ChronoPeriod between(ChronoLocalDate startDateInclusive, ChronoLocalDate endDateExclusive) {
		return startDateInclusive.until(endDateExclusive);
	}

	@Override
	long get(TemporalUnit unit);

	@Override
	List<TemporalUnit> getUnits();

	Chronology getChronology();

	default boolean isZero() {
		throw new RuntimeException("Not implemented");
	}

	default boolean isNegative() {
		throw new RuntimeException("Not implemented");
	}

	ChronoPeriod plus(TemporalAmount amountToAdd);

	ChronoPeriod minus(TemporalAmount amountToSubtract);

	ChronoPeriod multipliedBy(int scalar);

	default ChronoPeriod negated() {
		return multipliedBy(-1);
	}

	ChronoPeriod normalized();

	Temporal addTo(Temporal temporal);

	Temporal subtractFrom(Temporal temporal);

	boolean equals(Object obj);

	int hashCode();

	String toString();
}
