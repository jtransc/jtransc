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
        "private var _info:Dynamic;"
})
public class JTranscIOSyncFile {
    @HaxeMethodBody("_info = HaxeNatives.syncioOpen(p0._str, p1);")
    native void open(String name, int mode) throws FileNotFoundException;

    @HaxeMethodBody("HaxeNatives.syncioClose(_info);")
	public native void close() throws IOException;

    @HaxeMethodBody("return HaxeNatives.syncioRead(_info);")
	public native int read() throws IOException;

    @HaxeMethodBody("throw 'Not read';")
	public native int readBytes(byte b[], int off, int len) throws IOException;

    @HaxeMethodBody("HaxeNatives.syncioWrite(_info, p0);")
	public native void write(int b) throws IOException;

    @HaxeMethodBody("HaxeNatives.syncioWriteBytes(_info, p0, p1, p2);")
	public native void writeBytes(byte b[], int off, int len) throws IOException;

    @HaxeMethodBody("throw 'Not getFilePointer';")
	public native long getFilePointer() throws IOException;

    @HaxeMethodBody("throw 'Not seek';")
	public native void seek(long pos) throws IOException;

    @HaxeMethodBody("return HaxeNatives.syncioLength(_info);")
	public native long length() throws IOException;

    @HaxeMethodBody("throw 'Not setLength';")
	public native void setLength(long newLength) throws IOException;
}
