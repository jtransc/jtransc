package com.jtransc.io.async;

import com.jtransc.async.JTranscAsyncHandler;
import com.jtransc.io.JTranscFileMode;
import com.jtransc.io.JTranscFileStat;

import java.util.Arrays;

public class JTranscAsyncFile {
	public String path;

	public JTranscAsyncFile(String path) {
		this.path = path;
	}

	static private JTranscAsyncFileSystem fs() {
		return JTranscAsyncFileSystem.getInstance();
	}

	public void getLengthAsync(JTranscAsyncHandler<Long> handler) {
		fs().getLength(path, handler);
	}

	public void mkdirAsync(JTranscAsyncHandler<Boolean> handler) {
		fs().mkdir(path, handler);
	}

	public void statAsync(JTranscAsyncHandler<JTranscFileStat> handler) {
		fs().stat(path, handler);
	}

	public void openAsync(JTranscFileMode mode, JTranscAsyncHandler<JTranscAsyncStream> handler) {
		fs().open(path, mode, handler);
	}

	public void readAllAsync(final JTranscAsyncHandler<byte[]> handler) {
		openAsync(JTranscFileMode.READ, new JTranscAsyncHandler<JTranscAsyncStream>() {
			@Override
			public void complete(final JTranscAsyncStream asyncStream, Throwable error) {
				if (error != null) {
					handler.complete(null, error);
				} else {
					asyncStream.getLengthAsync(new JTranscAsyncHandler<Long>() {
						@Override
						public void complete(Long value, Throwable error) {
							if (error != null) {
								handler.complete(null, error);
							} else {
								final byte[] data = new byte[value.intValue()];
								asyncStream.readAsync(0L, data, 0, data.length, new JTranscAsyncHandler<Integer>() {
									@Override
									public void complete(Integer value, Throwable error) {
										if (error != null) {
											handler.complete(null, error);
										} else {
											handler.complete(Arrays.copyOf(data, value), null);
										}
									}
								});
							}
						}
					});
				}
			}
		});
	}
}
