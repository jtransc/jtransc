package com.jtransc.io.async;

import com.jtransc.async.JTranscAsyncHandler;

import java.util.Arrays;

public class JTranscAsyncBytesStream extends JTranscAsyncStream {
	public int length;
	public byte[] data;

	public JTranscAsyncBytesStream(byte[] data) {
		this.data = data;
		this.length = 0;
	}

	@Override
	public void writeAsync(long position, byte[] data, int offset, int size, JTranscAsyncHandler<Integer> callback) {
		long endPosition = position + size;
		if (endPosition > this.data.length) {
			this.data = Arrays.copyOf(this.data, (int) Math.max(this.data.length * 2, endPosition));
		}
		this.length = Math.max(this.length, (int) endPosition);

		System.arraycopy(data, offset, this.data, (int) position, size);
	}

	@Override
	public void readAsync(long position, byte[] data, int offset, int size, JTranscAsyncHandler<Integer> callback) {
		int available = (int) (this.length - position);
		int toRead = Math.min(available, size);
		if (toRead > 0) System.arraycopy(this.data, (int) position, data, offset, toRead);
		callback.complete(toRead, null);
	}

	@Override
	public void setLengthAsync(long length, JTranscAsyncHandler<Long> callback) {
		this.length = (int) length;
		this.data = Arrays.copyOf(data, this.length);
	}

	@Override
	public void getLengthAsync(JTranscAsyncHandler<Long> callback) {
		callback.complete((long) data.length, null);
	}

	@Override
	public void closeAsync(JTranscAsyncHandler<Integer> callback) {
		callback.complete(0, null);
	}
}
