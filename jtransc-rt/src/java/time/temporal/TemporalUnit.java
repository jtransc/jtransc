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

public interface TemporalUnit {

	Duration getDuration();

	boolean isDurationEstimated();

	boolean isDateBased();

	boolean isTimeBased();

	<R extends Temporal> R addTo(R temporal, long amount);

	long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive);

	String toString();

	default boolean isSupportedBy(Temporal temporal) {
		throw new RuntimeException("Not implemented");
	}

}
