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

public final class FileDescriptor {
	private int fd;

	public FileDescriptor() {
		fd = -1;
	}

	private FileDescriptor(int fd) {
		this.fd = fd;
	}

	public static final FileDescriptor in = new FileDescriptor(0);

	public static final FileDescriptor out = new FileDescriptor(1);

	public static final FileDescriptor err = new FileDescriptor(2);

	public boolean valid() {
		return fd != -1;
	}

	public native void sync() throws SyncFailedException;
}
