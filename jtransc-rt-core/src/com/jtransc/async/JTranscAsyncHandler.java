package com.jtransc.async;

public interface JTranscAsyncHandler<T> {
	void complete(T value, Throwable error);
}
