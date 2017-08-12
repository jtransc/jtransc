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

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

public abstract class FileSystem implements Closeable {
	protected FileSystem() {
	}

	public abstract FileSystemProvider provider();

	@Override
	public abstract void close() throws IOException;

	public abstract boolean isOpen();

	public abstract boolean isReadOnly();

	public abstract String getSeparator();

	public abstract Iterable<Path> getRootDirectories();

	public abstract Iterable<FileStore> getFileStores();

	public abstract Set<String> supportedFileAttributeViews();

	public abstract Path getPath(String first, String... more);

	public abstract PathMatcher getPathMatcher(String syntaxAndPattern);

	public abstract UserPrincipalLookupService getUserPrincipalLookupService();

	public abstract WatchService newWatchService() throws IOException;
}
