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

package com.jtransc.internal;

public class JTranscTempBuffer {
	//static private char[] charBuffer;
	//static private byte[] byteBuffer;

	//private static final int BUFFER_SIZE = 1024;

	static public char[] tempChar(int len) {
		/*
		if (len > BUFFER_SIZE) return new char[len];
		if (charBuffer == null) charBuffer = new char[BUFFER_SIZE];
		return charBuffer;
		*/
		return new char[len];
	}

	static public byte[] tempByte(int len) {
		/*
		if (len > BUFFER_SIZE) return new byte[len];
		if (byteBuffer == null) byteBuffer = new byte[BUFFER_SIZE];
		return byteBuffer;
		*/
		return new byte[len];
	}
}
