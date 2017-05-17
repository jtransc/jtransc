package com.jtransc.io.async;

import com.jtransc.async.JTranscAsyncHandler;

public abstract class JTranscAsyncStream {
	abstract public void writeAsync(long position, byte[] data, int offset, int size, JTranscAsyncHandler<Integer> callback);

	abstract public void readAsync(long position, byte[] data, int offset, int size, JTranscAsyncHandler<Integer> callback);

	abstract public void setLengthAsync(long length, JTranscAsyncHandler<Long> callback);

	abstract public void getLengthAsync(JTranscAsyncHandler<Long> callback);

	abstract public void closeAsync(JTranscAsyncHandler<Integer> callback);
}
