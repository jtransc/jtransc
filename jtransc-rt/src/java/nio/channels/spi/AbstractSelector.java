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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

public abstract class AbstractSelector extends Selector {
	protected AbstractSelector(SelectorProvider provider) {
		
	}

	native public final void close() throws IOException;

	protected abstract void implCloseSelector() throws IOException;

	native public final boolean isOpen();

	native public final SelectorProvider provider();

	native protected final Set<SelectionKey> cancelledKeys();

	protected abstract SelectionKey register(AbstractSelectableChannel ch, int ops, Object att);

	native protected final void deregister(AbstractSelectionKey key);

	native protected final void begin();

	native protected final void end();

}
