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

package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public abstract class AbstractSelectableChannel extends SelectableChannel {
	protected AbstractSelectableChannel(SelectorProvider provider) {
		throw new RuntimeException("Not implemented");
	}

	native public final SelectorProvider provider();

	native public final boolean isRegistered();

	native public final SelectionKey keyFor(Selector sel);

	native public final SelectionKey register(Selector sel, int ops, Object att) throws ClosedChannelException;

	native protected final void implCloseChannel() throws IOException;

	protected abstract void implCloseSelectableChannel() throws IOException;

	native public final boolean isBlocking();

	native public final Object blockingLock();

	public native final SelectableChannel configureBlocking(boolean block) throws IOException;

	protected abstract void implConfigureBlocking(boolean block) throws IOException;
}
