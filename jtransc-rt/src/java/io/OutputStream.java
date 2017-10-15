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

package java.io;

import com.jtransc.JTranscArrays;
import com.jtransc.annotation.JTranscSync;

public abstract class OutputStream implements Closeable, Flushable {

	/**
	 * Default constructor.
	 */
	@JTranscSync
	public OutputStream() {
	}

	/**
	 * Closes this stream. Implementations of this method should free any
	 * resources used by the stream. This implementation does nothing.
	 *
	 * @throws IOException if an error occurs while closing this stream.
	 */
	@JTranscSync
	public void close() throws IOException {
		/* empty */
	}

	/**
	 * Flushes this stream. Implementations of this method should ensure that
	 * any buffered data is written out. This implementation does nothing.
	 *
	 * @throws IOException if an error occurs while flushing this stream.
	 */
	@JTranscSync
	public void flush() throws IOException {
        /* empty */
	}

	/**
	 * Equivalent to {@code write(buffer, 0, buffer.length)}.
	 */
	public void write(byte[] buffer) throws IOException {
		write(buffer, 0, buffer.length);
	}

	/**
	 * Writes {@code count} bytes from the byte array {@code buffer} starting at
	 * position {@code offset} to this stream.
	 *
	 * @param buffer the buffer to be written.
	 * @param offset the start position in {@code buffer} from where to get bytes.
	 * @param count  the number of bytes from {@code buffer} to write to this
	 *               stream.
	 * @throws IOException               if an error occurs while writing to this stream.
	 * @throws IndexOutOfBoundsException if {@code offset < 0} or {@code count < 0}, or if
	 *                                   {@code offset + count} is bigger than the length of
	 *                                   {@code buffer}.
	 */
	public void write(byte[] buffer, int offset, int count) throws IOException {
		JTranscArrays.checkOffsetAndCount(buffer.length, offset, count);
		for (int i = offset; i < offset + count; i++) {
			write(buffer[i]);
		}
	}

	/**
	 * Writes a single byte to this stream. Only the least significant byte of
	 * the integer {@code oneByte} is written to the stream.
	 *
	 * @param oneByte the byte to be written.
	 * @throws IOException if an error occurs while writing to this stream.
	 */
	public abstract void write(int oneByte) throws IOException;

	/**
	 * Returns true if this writer has encountered and suppressed an error. Used
	 * by PrintStreams as an alternative to checked exceptions.
	 */
	boolean checkError() {
		return false;
	}
}
