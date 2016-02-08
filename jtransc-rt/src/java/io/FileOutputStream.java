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

import jtransc.JTranscIOSync;
import jtransc.JTranscIOSyncFile;

public class FileOutputStream extends OutputStream {
	private final FileDescriptor fd;
	private final boolean append;
	private final String path;
	private final JTranscIOSyncFile jfd;
	private volatile boolean closed = false;

	public FileOutputStream(String name) throws FileNotFoundException {
		this(name != null ? new File(name) : null, false);
	}

	public FileOutputStream(String name, boolean append) throws FileNotFoundException {
		this(name != null ? new File(name) : null, append);
	}

	public FileOutputStream(File file) throws FileNotFoundException {
		this(file, false);
	}

	public FileOutputStream(File file, boolean append) throws FileNotFoundException {
		String name = (file != null ? file.getPath() : null);
		if (name == null) throw new NullPointerException();
		if (file.isInvalid()) throw new FileNotFoundException("Invalid file path");
		this.fd = new FileDescriptor();
		this.append = append;
		this.path = name;

		jfd = JTranscIOSync.open(name, JTranscIOSync.O_RDWR);
		try {
			if (append) {
				jfd.seek(jfd.length());
			}
		} catch (IOException e) {

		}
	}

    public FileOutputStream(FileDescriptor fdObj) {
	    /*
        if (fdObj == null) throw new NullPointerException();
        this.fd = fdObj;
        this.append = false;
        this.path = null;
	    */
	    throw new RuntimeException("Not implemented");

        //fd.attach(this);
    }

	public void write(int b) throws IOException {
		jfd.write(b);
	}

	public void write(byte b[]) throws IOException {
		jfd.writeBytes(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException {
		jfd.writeBytes(b, off, len);
	}

	public void close() throws IOException {
		if (closed) return;
		closed = true;
		jfd.close();
	}

	public final FileDescriptor getFD() throws IOException {
		if (fd != null) return fd;
		throw new IOException();
	}

	//private FileChannel channel;
	//public FileChannel getChannel() {
	//    synchronized (this) {
	//        if (channel == null) channel = FileChannelImpl.open(fd, path, false, true, append, this);
	//        return channel;
	//    }
	//}

	protected void finalize() throws IOException {
		if (fd == null) return;
		if (fd == FileDescriptor.out || fd == FileDescriptor.err) {
			flush();
		} else {
			close();
		}
	}
}
