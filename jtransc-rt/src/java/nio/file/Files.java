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

import java.io.*;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.attribute.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public final class Files {
	native public static InputStream newInputStream(Path path, OpenOption... options) throws IOException;

	native public static OutputStream newOutputStream(Path path, OpenOption... options) throws IOException;

	native public static SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException;

	native public static SeekableByteChannel newByteChannel(Path path, OpenOption... options) throws IOException;

	native public static DirectoryStream<Path> newDirectoryStream(Path dir) throws IOException;

	native public static DirectoryStream<Path> newDirectoryStream(Path dir, String glob) throws IOException;

	native public static DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException;

	native public static Path createFile(Path path, FileAttribute<?>... attrs) throws IOException;

	native public static Path createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException;

	native public static Path createDirectories(Path dir, FileAttribute<?>... attrs) throws IOException;

	native public static Path createTempFile(Path dir, String prefix, String suffix, FileAttribute<?>... attrs) throws IOException;

	native public static Path createTempFile(String prefix, String suffix, FileAttribute<?>... attrs) throws IOException;

	native public static Path createTempDirectory(Path dir, String prefix, FileAttribute<?>... attrs) throws IOException;

	native public static Path createTempDirectory(String prefix, FileAttribute<?>... attrs) throws IOException;

	native public static Path createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException;

	native public static Path createLink(Path link, Path existing) throws IOException;

	native public static void delete(Path path) throws IOException;

	native public static boolean deleteIfExists(Path path) throws IOException;

	native public static Path copy(Path source, Path target, CopyOption... options) throws IOException;

	native public static Path move(Path source, Path target, CopyOption... options) throws IOException;

	native public static Path readSymbolicLink(Path link) throws IOException;

	native public static FileStore getFileStore(Path path) throws IOException;

	native public static boolean isSameFile(Path path, Path path2) throws IOException;

	native public static boolean isHidden(Path path) throws IOException;

	native public static String probeContentType(Path path) throws IOException;

	native public static <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options);

	native public static <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException;

	native public static Path setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException;

	native public static Object getAttribute(Path path, String attribute, LinkOption... options) throws IOException;

	native public static Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException;

	native public static Set<PosixFilePermission> getPosixFilePermissions(Path path, LinkOption... options) throws IOException;

	native public static Path setPosixFilePermissions(Path path, Set<PosixFilePermission> perms) throws IOException;

	native public static UserPrincipal getOwner(Path path, LinkOption... options) throws IOException;

	native public static Path setOwner(Path path, UserPrincipal owner) throws IOException;

	native public static boolean isSymbolicLink(Path path);

	native public static boolean isDirectory(Path path, LinkOption... options);

	native public static boolean isRegularFile(Path path, LinkOption... options);

	native public static FileTime getLastModifiedTime(Path path, LinkOption... options) throws IOException;

	native public static Path setLastModifiedTime(Path path, FileTime time) throws IOException;

	native public static long size(Path path) throws IOException;

	native public static boolean exists(Path path, LinkOption... options);

	native public static boolean notExists(Path path, LinkOption... options);

	native public static boolean isReadable(Path path);

	native public static boolean isWritable(Path path);

	native public static boolean isExecutable(Path path);

	native public static Path walkFileTree(Path start, Set<FileVisitOption> options, int maxDepth, FileVisitor<? super Path> visitor) throws IOException;

	native public static Path walkFileTree(Path start, FileVisitor<? super Path> visitor) throws IOException;

	native public static BufferedReader newBufferedReader(Path path, Charset cs) throws IOException;

	native public static BufferedReader newBufferedReader(Path path) throws IOException;

	native public static BufferedWriter newBufferedWriter(Path path, Charset cs, OpenOption... options) throws IOException;

	native public static BufferedWriter newBufferedWriter(Path path, OpenOption... options) throws IOException;

	native public static long copy(InputStream in, Path target, CopyOption... options) throws IOException;

	native public static long copy(Path source, OutputStream out) throws IOException;

	native public static byte[] readAllBytes(Path path) throws IOException;

	native public static List<String> readAllLines(Path path, Charset cs) throws IOException;

	native public static List<String> readAllLines(Path path) throws IOException;

	native public static Path write(Path path, byte[] bytes, OpenOption... options) throws IOException;

	native public static Path write(Path path, Iterable<? extends CharSequence> lines, Charset cs, OpenOption... options) throws IOException;

	native public static Path write(Path path, Iterable<? extends CharSequence> lines, OpenOption... options) throws IOException;

	native public static Stream<Path> list(Path dir) throws IOException;

	native public static Stream<Path> walk(Path start, int maxDepth, FileVisitOption... options) throws IOException;

	native public static Stream<Path> walk(Path start, FileVisitOption... options) throws IOException;

	native public static Stream<Path> find(Path start, int maxDepth, BiPredicate<Path, BasicFileAttributes> matcher, FileVisitOption... options) throws IOException;

	native public static Stream<String> lines(Path path, Charset cs) throws IOException;

	native public static Stream<String> lines(Path path) throws IOException;
}
