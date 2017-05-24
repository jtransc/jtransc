package com.jtransc.imaging.async;

import com.jtransc.async.JTranscAsyncHandler;
import com.jtransc.imaging.JTranscNativeBitmap;
import com.jtransc.io.async.JTranscAsyncFile;
import com.jtransc.service.JTranscService;

public abstract class JTranscAsyncBitmapDataDecoder {
	static private JTranscAsyncBitmapDataDecoder _instance;

	static public JTranscAsyncBitmapDataDecoder getInstance() {
		if (_instance == null) _instance = JTranscService.getFirst(JTranscAsyncBitmapDataDecoder.class);
		return _instance;
	}

	static public void readFromURLAsync(String url, JTranscAsyncHandler<JTranscNativeBitmap> handler) {
		getInstance().readFromURLAsync(url, handler);
	}

	static public void readFromFileAsync(String url, JTranscAsyncHandler<JTranscNativeBitmap> handler) {
		getInstance().readFromFileAsync(url, handler);
	}

	static public void readFromBytesAsync(byte[] data, JTranscAsyncHandler<JTranscNativeBitmap> handler) {
		getInstance().readFromBytesAsyncImpl(data, handler);
	}


	public void readFromURLAsyncImpl(String url, JTranscAsyncHandler<JTranscNativeBitmap> handler) {
		throw new RuntimeException("Not implemented");
	}

	public void readFromFileAsyncImpl(final String file, final JTranscAsyncHandler<JTranscNativeBitmap> handler) {
		new JTranscAsyncFile(file).readAllAsync(new JTranscAsyncHandler<byte[]>() {
			@Override
			public void complete(byte[] data, Throwable error) {
				if (error != null) {
					handler.complete(null, error);
				} else {
					readFromBytesAsyncImpl(data, handler);
				}
			}
		});
	}

	abstract public void readFromBytesAsyncImpl(final byte[] data, final JTranscAsyncHandler<JTranscNativeBitmap> handler);
}
