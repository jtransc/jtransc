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
import java.net.ProtocolFamily;
import java.nio.channels.*;

public abstract class SelectorProvider {
	protected SelectorProvider() {
	}

	public static SelectorProvider provider() {
		throw new RuntimeException("Not implemented");
	}

	public abstract DatagramChannel openDatagramChannel() throws IOException;

	public abstract DatagramChannel openDatagramChannel(ProtocolFamily family) throws IOException;

	public abstract Pipe openPipe() throws IOException;

	public abstract AbstractSelector openSelector() throws IOException;

	public abstract ServerSocketChannel openServerSocketChannel() throws IOException;

	public abstract SocketChannel openSocketChannel() throws IOException;

	public Channel inheritedChannel() throws IOException {
		return null;
	}

}
