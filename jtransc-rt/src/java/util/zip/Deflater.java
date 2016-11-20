/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util.zip;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Deflater {
	public static final int BEST_COMPRESSION = 9;
	public static final int BEST_SPEED = 1;
	public static final int NO_COMPRESSION = 0;
	public static final int DEFAULT_COMPRESSION = -1;
	public static final int DEFAULT_STRATEGY = 0;
	public static final int DEFLATED = 8;
	public static final int FILTERED = 1;
	public static final int HUFFMAN_ONLY = 2;
	public static final int NO_FLUSH = 0;
	public static final int SYNC_FLUSH = 2;
	public static final int FULL_FLUSH = 3;
	private static final int FINISH = 4;
	private int flushParm = NO_FLUSH;

	//static final private int Z_NO_FLUSH=0;
	//static final private int Z_PARTIAL_FLUSH=1;
	//static final private int Z_SYNC_FLUSH=2;
	//static final private int Z_FULL_FLUSH=3;
	//static final private int Z_FINISH=4;

	private boolean finished;

	private int compressLevel = DEFAULT_COMPRESSION;

	private int strategy = DEFAULT_STRATEGY;

	private long streamHandle = -1;

	private int inRead;

	private int inLength;

	private boolean noHeader;

	com.jtransc.compression.jzlib.Deflater impl;

	public Deflater() {
		this(DEFAULT_COMPRESSION, false);
	}

	public Deflater(int level) {
		this(level, false);
	}

	public Deflater(int level, boolean noHeader) {
		this.compressLevel = level;
		this.noHeader = noHeader;
		this.impl = new com.jtransc.compression.jzlib.Deflater(level, noHeader);
	}

	public int deflate(byte[] buf) {
		return deflate(buf, 0, buf.length);
	}

	public synchronized int deflate(byte[] buf, int offset, int byteCount) {
		return deflateImpl(buf, offset, byteCount, flushParm);
	}

	public synchronized int deflate(byte[] buf, int offset, int byteCount, int flush) {
		return deflateImpl(buf, offset, byteCount, flush);
	}

	private synchronized int deflateImpl(byte[] buf, int offset, int byteCount, int flush) {
		this.impl.setOutput(buf, offset, byteCount);
		long outstart = impl.getTotalOut();
		int result = this.impl.deflate(flush);
		long outend = impl.getTotalOut();
		return (int) (outend - outstart);
	}

	public synchronized void end() {
		endImpl();
	}

	private void endImpl() {
		if (streamHandle != -1) {
			impl.end();
			streamHandle = -1;
		}
	}

	@Override
	protected void finalize() {
		try {
			synchronized (this) {
				end(); // to allow overriding classes to clean up
				endImpl(); // in case those classes don't call super.end()
			}
		} finally {
			try {
				super.finalize();
			} catch (Throwable t) {
				throw new AssertionError(t);
			}
		}
	}

	public synchronized void finish() {
		flushParm = FINISH;
	}

	public synchronized boolean finished() {
		return finished;
	}

	public synchronized int getAdler() {
		return (int) impl.getAdler();
	}

	public synchronized int getTotalIn() {

		return (int) impl.getTotalIn();
	}

	public synchronized int getTotalOut() {
		return (int) impl.getTotalOut();
	}

	public synchronized boolean needsInput() {
		return (inRead == inLength);
	}

	public synchronized void reset() {
		flushParm = NO_FLUSH;
		finished = false;
		impl.init(compressLevel, noHeader);
	}

	public void setDictionary(byte[] dictionary) {
		setDictionary(dictionary, 0, dictionary.length);
	}

	public synchronized void setDictionary(byte[] buf, int offset, int byteCount) {
		impl.setDictionary(buf, offset, byteCount);
	}

	public void setInput(byte[] buf) {
		setInput(buf, 0, buf.length);
	}

	public synchronized void setInput(byte[] buf, int offset, int byteCount) {
		inLength = byteCount;
		inRead = 0;
		impl.setInput(buf, offset, byteCount, false);
	}

	public synchronized void setLevel(int level) {
		compressLevel = level;
	}

	public synchronized void setStrategy(int strategy) {
		this.strategy = strategy;
	}

	public synchronized long getBytesRead() {
		return impl.getTotalIn();
	}

	public synchronized long getBytesWritten() {
		return impl.getTotalOut();
	}
}
