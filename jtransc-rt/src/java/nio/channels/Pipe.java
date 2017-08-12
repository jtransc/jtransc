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
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

public abstract class Pipe {
	public static abstract class SourceChannel extends AbstractSelectableChannel implements ReadableByteChannel, ScatteringByteChannel {
		protected SourceChannel(SelectorProvider provider) {
			super(provider);
		}

		public final int validOps() {
			return SelectionKey.OP_READ;
		}
	}

	public static abstract class SinkChannel extends AbstractSelectableChannel implements WritableByteChannel, GatheringByteChannel {
		protected SinkChannel(SelectorProvider provider) {
			super(provider);
		}

		public final int validOps() {
			return SelectionKey.OP_WRITE;
		}
	}

	protected Pipe() {
	}

	public abstract SourceChannel source();

	public abstract SinkChannel sink();

	public static Pipe open() throws IOException {
		return SelectorProvider.provider().openPipe();
	}
}
