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
	private final RandomAccessFile rf;

	private volatile boolean closed = false;

	public FileInputStream(String name) throws FileNotFoundException {
		this(name != null ? new File(name) : null);
	}

	public FileInputStream(File file) throws FileNotFoundException {
		//String name = (file != null ? file.getPath() : null);
		//if (name == null) throw new NullPointerException();
		//if (file.isInvalid()) throw new FileNotFoundException("Invalid file path");
		this.fd = new FileDescriptor();
		//this.path = JTranscSyncIO.impl.normalizePath(file.getAbsolutePath());
		this.rf = new RandomAccessFile(file, "r");
	}

	public FileInputStream(FileDescriptor fdObj) {
		throw new RuntimeException("Not implemented");
	}

	public int read() throws IOException {
		return this.rf.read();
	}

	public int read(byte b[]) throws IOException {
		return this.rf.read(b);
	}

	public int read(byte data[], int offset, int length) throws IOException {
		return this.rf.read(data, offset, length);
	}

	public long skip(long n) throws IOException {
		this.rf.seek(this.rf.getFilePointer() + n);
		return this.rf.getFilePointer();
	}

	public int available() throws IOException {
		return (int) (this.rf.length() - this.rf.getFilePointer());
	}

	public void close() throws IOException {
		if (closed) return;
		closed = true;
		this.rf.close();
	}

	public final FileDescriptor getFD() throws IOException {
		throw new IOException();
	}

	protected void finalize() throws IOException {
		if ((fd != FileDescriptor.in)) {
			close();
		}
	}
}
