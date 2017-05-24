package com.jtransc.imaging.async.impl;

import com.jtransc.async.JTranscAsyncHandler;
import com.jtransc.imaging.JTranscNativeBitmap;
import com.jtransc.imaging.async.JTranscAsyncBitmapDataDecoder;
import com.jtransc.target.js.JsDynamic;

public class JTranscAsyncBitmapDataDecoderHtml extends JTranscAsyncBitmapDataDecoder {
	private void _readFromURLAsyncImpl(final String path, final JTranscAsyncHandler<JTranscNativeBitmap> handler) {
		JsDynamic image = JsDynamic.global("Image").newInstance(path);
		image.call("addEventListener", "load", JsDynamic.func(new JsDynamic.Function1() {
			@Override
			public Object run(JsDynamic e) {
				return null;
			}
		}));
	}

	@Override
	public void readFromURLAsyncImpl(String url, JTranscAsyncHandler<JTranscNativeBitmap> handler) {
		_readFromURLAsyncImpl(url, handler);
	}

	@Override
	public void readFromFileAsyncImpl(String file, JTranscAsyncHandler<JTranscNativeBitmap> handler) {
		_readFromURLAsyncImpl(file, handler);
	}

	@Override
	public void readFromBytesAsyncImpl(byte[] data, JTranscAsyncHandler<JTranscNativeBitmap> handler) {
		throw new RuntimeException("Not implemented");
	}
}
