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

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

public abstract class Selector implements Closeable {
	protected Selector() {
	}

	public static Selector open() throws IOException {
		return SelectorProvider.provider().openSelector();
	}

	public abstract boolean isOpen();

	public abstract SelectorProvider provider();

	public abstract Set<SelectionKey> keys();

	public abstract Set<SelectionKey> selectedKeys();

	public abstract int selectNow() throws IOException;

	public abstract int select(long timeout) throws IOException;

	public abstract int select() throws IOException;

	public abstract Selector wakeup();

	public abstract void close() throws IOException;
}
