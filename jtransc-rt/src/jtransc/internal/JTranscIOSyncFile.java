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

package jtransc.internal;

import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

import java.io.FileNotFoundException;
import java.io.IOException;

@HaxeAddMembers({
        "private var _stream = new HaxeIO.SyncStream();"
})
public class JTranscIOSyncFile {
	private byte[] temp = new byte[1];

    @HaxeMethodBody("_stream.syncioOpen(p0._str, p1);")
    native void open(String name, int mode) throws FileNotFoundException;

    @HaxeMethodBody("_stream.syncioClose();")
	public native void close() throws IOException;

	public int read() throws IOException {
		if (readBytes(temp, 0, 1) == 1) {
			return temp[0];
		} else {
			return -1;
		}
	}

    @HaxeMethodBody("return _stream.syncioReadBytes(p0, p1, p2);")
	public native int readBytes(byte b[], int off, int len) throws IOException;

	public void write(int b) throws IOException {
		temp[0] = (byte) b;
		writeBytes(temp, 0, 1);
	}

    @HaxeMethodBody("_stream.syncioWriteBytes(p0, p1, p2);")
	public native void writeBytes(byte b[], int off, int len) throws IOException;

    @HaxeMethodBody("return _stream.syncioPosition();")
	public native long getFilePointer() throws IOException;

	@HaxeMethodBody("_stream.syncioSetPosition(p0);")
	public native void seek(long pos) throws IOException;

    @HaxeMethodBody("return _stream.syncioLength();")
	public native long length() throws IOException;

    @HaxeMethodBody("_stream.syncioSetLength(p0);")
	public native void setLength(long newLength) throws IOException;
}
