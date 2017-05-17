package com.jtransc.io.async.impl;

import com.jtransc.async.JTranscAsyncHandler;
import com.jtransc.async.JTranscAsyncTools;
import com.jtransc.io.JTranscFileMode;
import com.jtransc.io.async.JTranscAsyncFileSystem;
import com.jtransc.io.async.JTranscAsyncStream;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class JTranscAsyncFileSystemNIO extends JTranscAsyncFileSystem {
	@Override
	public void getLength(final String path, final JTranscAsyncHandler<Long> handler) {
		JTranscAsyncTools.runInThread(handler, new JTranscAsyncTools.ThreadRun<Long>() {
			@Override
			public Long run() {
				return new File(path).length();
			}
		});
	}

	@Override
	public void mkdir(final String path, final JTranscAsyncHandler<Boolean> handler) {
		JTranscAsyncTools.runInThread(handler, new JTranscAsyncTools.ThreadRun<Boolean>() {
			@Override
			public Boolean run() {
				new File(path).mkdir();
				return true;
			}
		});
	}

	@Override
	public void open(final String path, final JTranscFileMode mode, final JTranscAsyncHandler<JTranscAsyncStream> handler) {
		StandardOpenOption[] options = new StandardOpenOption[0];

		switch (mode) {
			case READ:
				options = new StandardOpenOption[]{StandardOpenOption.READ};
				break;
			case WRITE:
				options = new StandardOpenOption[]{StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE};
				break;
			case APPEND:
				options = new StandardOpenOption[]{StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.APPEND};
				break;
		}

		final AsynchronousFileChannel channel;
		try {
			channel = AsynchronousFileChannel.open(Paths.get(path), options);
		} catch (Throwable t) {
			handler.complete(null, t);
			return;
		}
		handler.complete(new JTranscAsyncStream() {
			@Override
			public void writeAsync(long position, byte[] data, int offset, int size, JTranscAsyncHandler<Integer> callback) {
				channel.write(ByteBuffer.wrap(data, offset, size), position, callback, new CompletionHandler<Integer, Object>() {
					@Override
					public void completed(Integer result, Object attachment) {
						((JTranscAsyncHandler<Integer>)attachment).complete(result, null);
					}

					@Override
					public void failed(Throwable exc, Object attachment) {
						((JTranscAsyncHandler<Integer>)attachment).complete(null, exc);
					}
				});
			}

			@Override
			public void readAsync(long position, byte[] data, int offset, int size, JTranscAsyncHandler<Integer> callback) {
				channel.read(ByteBuffer.wrap(data, offset, size), position, callback, new CompletionHandler<Integer, Object>() {
					@Override
					public void completed(Integer result, Object attachment) {
						((JTranscAsyncHandler<Integer>)attachment).complete(result, null);
					}

					@Override
					public void failed(Throwable exc, Object attachment) {
						((JTranscAsyncHandler<Integer>)attachment).complete(null, exc);
					}
				});
			}

			@Override
			public void setLengthAsync(long length, JTranscAsyncHandler<Long> callback) {
				try {
					channel.truncate(length);
				} catch (IOException e) {
					callback.complete(null, e);
					return;
				}
				callback.complete(length, null);
			}

			@Override
			public void getLengthAsync(JTranscAsyncHandler<Long> callback) {
				final long size;
				try {
					size = channel.size();
				} catch (IOException e) {
					callback.complete(null, e);
					return;
				}
				callback.complete(size, null);
			}

			@Override
			public void closeAsync(JTranscAsyncHandler<Integer> callback) {
				try {
					channel.close();
				} catch (IOException e) {
					callback.complete(null, e);
					return;
				}
				callback.complete(0, null);
			}
		}, null);
	}

}
