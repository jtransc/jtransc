package com.jtransc.io.async;

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscMethodBodyList;
import com.jtransc.async.JTranscAsyncHandler;
import com.jtransc.io.JTranscFileMode;
import com.jtransc.io.JTranscSyncIO;
import com.jtransc.service.JTranscService;

public class JTranscAsyncFileSystem {
	static private JTranscAsyncFileSystem _instance;

	static public JTranscAsyncFileSystem getInstance() {
		if (_instance == null) _instance = JTranscService.getFirst(JTranscAsyncFileSystem.class);
		return _instance;
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = "N.completeFuture(p0, () async { var length = new File(N.istr(p0)).length(); return N.boxLong(N.lnew(len)); });"),
	})
	public void getLength(String path, JTranscAsyncHandler<Long> handler) {
		try {
			handler.complete(JTranscSyncIO.impl.getLength(path), null);
		} catch (Throwable t) {
			handler.complete(null, t);
		}
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = "N.completeFuture(p0, () async { new Directory(N.istr(p0)).create(recursive: false); return N.boxBool(true); });"),
	})
	public void mkdir(String path, JTranscAsyncHandler<Boolean> handler) {
		try {
			handler.complete(JTranscSyncIO.impl.createDirectory(path), null);
		} catch (Throwable t) {
			handler.complete(null, t);
		}
	}

	public void open(final String path, final JTranscFileMode mode, final JTranscAsyncHandler<JTranscAsyncStream> handler) {
		try {
			new JTranscAsyncFileStream().openAsync(path, mode.mode, mode.modestr, new JTranscAsyncHandler<JTranscAsyncFileStream>() {
				@Override
				public void complete(JTranscAsyncFileStream value, Throwable error) {
					handler.complete(value, error);
				}
			});
		} catch (Throwable t) {
			handler.complete(null, t);
		}
	}
}
