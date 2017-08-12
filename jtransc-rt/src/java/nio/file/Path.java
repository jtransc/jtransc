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
package java.nio.file;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

public interface Path extends Comparable<Path>, Iterable<Path>, Watchable {
	FileSystem getFileSystem();

	boolean isAbsolute();

	Path getRoot();

	Path getFileName();

	Path getParent();

	int getNameCount();

	Path getName(int index);

	Path subpath(int beginIndex, int endIndex);

	boolean startsWith(Path other);

	boolean startsWith(String other);

	boolean endsWith(Path other);

	boolean endsWith(String other);

	Path normalize();

	Path resolve(Path other);

	Path resolve(String other);

	Path resolveSibling(Path other);

	Path resolveSibling(String other);

	Path relativize(Path other);

	URI toUri();

	Path toAbsolutePath();

	Path toRealPath(LinkOption... options) throws IOException;

	File toFile();

	WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException;

	WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException;

	Iterator<Path> iterator();

	int compareTo(Path other);

	boolean equals(Object other);

	int hashCode();

	String toString();
}
