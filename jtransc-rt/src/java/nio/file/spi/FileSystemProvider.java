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
package java.nio.file.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public abstract class FileSystemProvider {
	protected FileSystemProvider() {

	}

	native public static List<FileSystemProvider> installedProviders();

	public abstract String getScheme();

	public abstract FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException;

	public abstract FileSystem getFileSystem(URI uri);

	public abstract Path getPath(URI uri);

	public FileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException {
		throw new UnsupportedOperationException();
	}

	native public InputStream newInputStream(Path path, OpenOption... options) throws IOException;

	native public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException;

	public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
		throw new UnsupportedOperationException();
	}

	public AsynchronousFileChannel newAsynchronousFileChannel(Path path, Set<? extends OpenOption> options, ExecutorService executor, FileAttribute<?>... attrs) throws IOException {
		throw new UnsupportedOperationException();
	}

	public abstract SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException;

	public abstract DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException;

	public abstract void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException;

	public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void createLink(Path link, Path existing) throws IOException {
		throw new UnsupportedOperationException();
	}

	public abstract void delete(Path path) throws IOException;

	native public boolean deleteIfExists(Path path) throws IOException;

	public Path readSymbolicLink(Path link) throws IOException {
		throw new UnsupportedOperationException();
	}

	public abstract void copy(Path source, Path target, CopyOption... options) throws IOException;

	public abstract void move(Path source, Path target, CopyOption... options) throws IOException;

	public abstract boolean isSameFile(Path path, Path path2) throws IOException;

	public abstract boolean isHidden(Path path) throws IOException;

	public abstract FileStore getFileStore(Path path) throws IOException;

	public abstract void checkAccess(Path path, AccessMode... modes) throws IOException;

	public abstract <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options);

	public abstract <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException;

	public abstract Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException;

	public abstract void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException;
}
