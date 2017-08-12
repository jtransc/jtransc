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
package java.nio.channels;

import java.io.IOException;

public abstract class FileLock implements AutoCloseable {
	protected FileLock(FileChannel channel, long position, long size, boolean shared) {
	}

	protected FileLock(AsynchronousFileChannel channel, long position, long size, boolean shared) {
	}

	native public final FileChannel channel();

	native public Channel acquiredBy();

	native public final long position();

	native public final long size();

	native public final boolean isShared();

	native public final boolean overlaps(long position, long size);

	public abstract boolean isValid();

	public abstract void release() throws IOException;

	native public final void close() throws IOException;

	native public final String toString();
}
