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

public interface Temporal extends TemporalAccessor {

	boolean isSupported(TemporalUnit unit);

	Temporal with(TemporalField field, long newValue);

	Temporal plus(long amountToAdd, TemporalUnit unit);

	long until(Temporal endExclusive, TemporalUnit unit);

	default Temporal with(TemporalAdjuster adjuster) {
		return adjuster.adjustInto(this);
	}

	default Temporal plus(TemporalAmount amount) {
		return amount.addTo(this);
	}

	default Temporal minus(TemporalAmount amount) {
		return amount.subtractFrom(this);
	}

	default Temporal minus(long amountToSubtract, TemporalUnit unit) {
		return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
	}
}
