package com.jtransc.imaging.async.impl;

import com.jtransc.async.JTranscAsyncHandler;
import com.jtransc.async.JTranscAsyncTools;
import com.jtransc.imaging.JTranscBitmapData32;
import com.jtransc.imaging.JTranscNativeBitmap;
import com.jtransc.imaging.async.JTranscAsyncBitmapDataDecoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class JTranscAsyncBitmapDataDecoderAwt extends JTranscAsyncBitmapDataDecoder {
	@Override
	public void readFromBytesAsyncImpl(final byte[] data, final JTranscAsyncHandler<JTranscNativeBitmap> handler) {
		JTranscAsyncTools.runInThread(handler, new JTranscAsyncTools.ThreadRun<JTranscNativeBitmap>() {
			@Override
			public JTranscNativeBitmap run() throws Throwable {
				final BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
				return new JTranscNativeBitmap(image) {
					private JTranscBitmapData32 _data;

					@Override
					public JTranscBitmapData32 getData() {
						if (_data == null) {
							_data = new JTranscBitmapData32(true, 1, 1, new int[1]);
						}
						return _data;
					}
				};
			}
		});
	}
}
