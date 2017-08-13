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
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;

public class FileChannel extends AbstractInterruptibleChannel implements SeekableByteChannel, GatheringByteChannel, ScatteringByteChannel {
	protected FileChannel() {
	}

	native protected void implCloseChannel() throws IOException;

	native public static FileChannel open(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException;

	native public static FileChannel open(Path path, OpenOption... options) throws IOException;

	public native int read(ByteBuffer dst) throws IOException;

	public native long read(ByteBuffer[] dsts, int offset, int length) throws IOException;

	public final long read(ByteBuffer[] dsts) throws IOException {
		return read(dsts, 0, dsts.length);
	}

	public native int write(ByteBuffer src) throws IOException;

	public native long write(ByteBuffer[] srcs, int offset, int length) throws IOException;

	public final long write(ByteBuffer[] srcs) throws IOException {
		return write(srcs, 0, srcs.length);
	}

	public native long position() throws IOException;

	public native FileChannel position(long newPosition) throws IOException;

	public native long size() throws IOException;

	public native FileChannel truncate(long size) throws IOException;

	public native void force(boolean metaData) throws IOException;

	public native long transferTo(long position, long count, WritableByteChannel target) throws IOException;

	public native long transferFrom(ReadableByteChannel src, long position, long count) throws IOException;

	public native int read(ByteBuffer dst, long position) throws IOException;

	public native int write(ByteBuffer src, long position) throws IOException;

	public static class MapMode {
		public static final MapMode READ_ONLY = new MapMode("READ_ONLY");
		public static final MapMode READ_WRITE = new MapMode("READ_WRITE");
		public static final MapMode PRIVATE = new MapMode("PRIVATE");
		private final String name;

		private MapMode(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}

	}

	//public native MappedByteBuffer map(MapMode mode, long position, long size) throws IOException;

	public native FileLock lock(long position, long size, boolean shared) throws IOException;

	public final FileLock lock() throws IOException {
		return lock(0L, Long.MAX_VALUE, false);
	}

	public native FileLock tryLock(long position, long size, boolean shared) throws IOException;

	public final FileLock tryLock() throws IOException {
		return tryLock(0L, Long.MAX_VALUE, false);
	}

}
