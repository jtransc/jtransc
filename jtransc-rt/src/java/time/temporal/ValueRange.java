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

import java.io.Serializable;

public final class ValueRange implements Serializable {
	private final long minSmallest;
	private final long minLargest;
	private final long maxSmallest;
	private final long maxLargest;

	public static ValueRange of(long min, long max) {
		if (min > max) {
			throw new IllegalArgumentException();
		}
		return new ValueRange(min, min, max, max);
	}

	public static ValueRange of(long min, long maxSmallest, long maxLargest) {
		return of(min, min, maxSmallest, maxLargest);
	}

	public static ValueRange of(long minSmallest, long minLargest, long maxSmallest, long maxLargest) {
		if (minSmallest > minLargest || maxSmallest > maxLargest || minLargest > maxLargest) {
			throw new IllegalArgumentException();
		}
		return new ValueRange(minSmallest, minLargest, maxSmallest, maxLargest);
	}

	private ValueRange(long minSmallest, long minLargest, long maxSmallest, long maxLargest) {
		this.minSmallest = minSmallest;
		this.minLargest = minLargest;
		this.maxSmallest = maxSmallest;
		this.maxLargest = maxLargest;
	}

	public boolean isFixed() {
		return minSmallest == minLargest && maxSmallest == maxLargest;
	}

	public long getMinimum() {
		return minSmallest;
	}

	public long getLargestMinimum() {
		return minLargest;
	}

	public long getSmallestMaximum() {
		return maxSmallest;
	}

	public long getMaximum() {
		return maxLargest;
	}

	public boolean isIntValue() {
		return getMinimum() >= Integer.MIN_VALUE && getMaximum() <= Integer.MAX_VALUE;
	}

	public boolean isValidValue(long value) {
		return (value >= getMinimum() && value <= getMaximum());
	}

	public boolean isValidIntValue(long value) {
		return isIntValue() && isValidValue(value);
	}

	native public long checkValidValue(long value, TemporalField field);

	native public int checkValidIntValue(long value, TemporalField field);

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
