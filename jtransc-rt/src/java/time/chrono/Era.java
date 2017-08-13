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

import java.time.format.TextStyle;
import java.time.temporal.*;
import java.util.Locale;

public interface Era extends TemporalAccessor, TemporalAdjuster {
	int getValue();

	default boolean isSupported(TemporalField field) {
		throw new RuntimeException("Not implemented");
	}

	default ValueRange range(TemporalField field) {
		throw new RuntimeException("Not implemented");
	}

	default int get(TemporalField field) {
		throw new RuntimeException("Not implemented");
	}

	default long getLong(TemporalField field) {
		throw new RuntimeException("Not implemented");
	}

	default <R> R query(TemporalQuery<R> query) {
		throw new RuntimeException("Not implemented");
	}

	default Temporal adjustInto(Temporal temporal) {
		throw new RuntimeException("Not implemented");
	}

	default String getDisplayName(TextStyle style, Locale locale) {
		throw new RuntimeException("Not implemented");
	}
}
