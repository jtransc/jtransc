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

import com.jtransc.io.JTranscSyncIO;

public class FileInputStream extends InputStream {
	private final FileDescriptor fd;
	private final JTranscSyncIO.ImplStream stream;
	private final String path;

	private volatile boolean closed = false;

	public FileInputStream(String name) throws FileNotFoundException {
		this(name != null ? new File(name) : null);
	}

	public FileInputStream(File file) throws FileNotFoundException {
		String name = (file != null ? file.getPath() : null);
		if (name == null) throw new NullPointerException();
		if (file.isInvalid()) throw new FileNotFoundException("Invalid file path");
		this.fd = new FileDescriptor();
		this.path = name;
		this.stream = JTranscSyncIO.impl.open(name, JTranscSyncIO.O_RDONLY);
	}

	public FileInputStream(FileDescriptor fdObj) {
		if (fdObj == null) throw new NullPointerException();
		fd = fdObj;
		path = null;
		this.stream = null;
	}

	private byte[] buffer = { 0 };

	public int read() throws IOException {
		int read = this.stream.read(buffer, 0, 1);
		if (read != 1) return -1;
		return buffer[0];
	}

	public int read(byte b[]) throws IOException {
		return this.stream.read(b, 0, b.length);
	}

	public int read(byte data[], int offset, int length) throws IOException {
		return this.stream.read(data, offset, length);
	}

	public long skip(long n) throws IOException {
		this.stream.setPosition(this.stream.getPosition() + n);
		return this.stream.getPosition();
	}

	public int available() throws IOException {
		return (int) (this.stream.getLength() - this.stream.getPosition());
	}

	public void close() throws IOException {
		if (closed) return;
		closed = true;
		this.stream.close();
	}

	public final FileDescriptor getFD() throws IOException {
		if (fd != null) return fd;
		throw new IOException();
	}

	protected void finalize() throws IOException {
		if ((fd != null) && (fd != FileDescriptor.in)) {
			close();
		}
	}
}
