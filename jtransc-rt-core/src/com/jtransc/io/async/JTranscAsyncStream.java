package com.jtransc.io.async;

import com.jtransc.async.JTranscAsyncHandler;

public abstract class JTranscAsyncStream {
	abstract public void writeAsync(byte[] data, int offset, int size, JTranscAsyncHandler<Integer> callback);

	abstract public void readAsync(byte[] data, int offset, int size, JTranscAsyncHandler<Integer> callback);

	abstract public void setPositionAsync(long position, JTranscAsyncHandler<Long> callback);

	abstract public void getPositionAsync(JTranscAsyncHandler<Long> callback);

	abstract public void setLengthAsync(long length, JTranscAsyncHandler<Long> callback);

	abstract public void getLengthAsync(JTranscAsyncHandler<Long> callback);

	public void closeAsync(JTranscAsyncHandler<Integer> callback) {
		callback.complete(0, null);
	}
}
