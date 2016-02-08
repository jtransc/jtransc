/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.io;

public class FileInputStream extends InputStream {
	private final FileDescriptor fd;
	private final String path;

	private volatile boolean closed = false;

	public FileInputStream(String name) throws FileNotFoundException {
		this(name != null ? new File(name) : null);
	}

	public FileInputStream(File file) throws FileNotFoundException {
		String name = (file != null ? file.getPath() : null);
		if (name == null) throw new NullPointerException();
		if (file.isInvalid()) throw new FileNotFoundException("Invalid file path");
		fd = new FileDescriptor();
		path = name;
		open(name);
	}

	public FileInputStream(FileDescriptor fdObj) {
		if (fdObj == null) throw new NullPointerException();
		fd = fdObj;
		path = null;
	}

	private native void open(String name) throws FileNotFoundException;

	public int read() throws IOException {
		return read0();
	}

	private native int read0() throws IOException;

	private native int readBytes(byte b[], int off, int len) throws IOException;

	public int read(byte b[]) throws IOException {
		return readBytes(b, 0, b.length);
	}

	public int read(byte data[], int offset, int length) throws IOException {
		return readBytes(data, offset, length);
	}

	public native long skip(long n) throws IOException;

	public native int available() throws IOException;

	public void close() throws IOException {
		if (closed) {
			return;
		}
		closed = true;
	}

	public final FileDescriptor getFD() throws IOException {
		if (fd != null) return fd;
		throw new IOException();
	}

	private native void close0() throws IOException;

	protected void finalize() throws IOException {
		if ((fd != null) && (fd != FileDescriptor.in)) {
			close();
		}
	}
}
