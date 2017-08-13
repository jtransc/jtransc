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
package java.nio.file.attribute;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public final class FileTime implements Comparable<FileTime> {
	native public static FileTime from(long value, TimeUnit unit);

	native public static FileTime fromMillis(long value);

	native public static FileTime from(Instant instant);

	native public long to(TimeUnit unit);

	native public long toMillis();

	native public Instant toInstant();

	@Override
	native public boolean equals(Object obj);

	@Override
	native public int hashCode();

	@Override
	native public int compareTo(FileTime that);

	@Override
	native public String toString();
}
