package com.jtransc.async;

public class JTranscAsyncTools {
	static public <T> void runInThread(final JTranscAsyncHandler<T> handler, final ThreadRun<T> run) {
		new Thread() {
			@Override
			public void run() {
				final T result;
				try {
					result = run.run();
				} catch (Throwable t) {
					handler.complete(null, t);
					return;
				}
				handler.complete(result, null);
			}
		}.start();
	}

	public interface ThreadRun<T> {
		T run() throws Throwable;
	}
}
