package com.jtransc.io.async;

import com.jtransc.async.JTranscAsyncHandler;
import com.jtransc.io.JTranscFileStat;
import com.jtransc.io.JTranscIoTools;
import com.jtransc.service.JTranscService;

public class JTranscAsyncResources {
	static private JTranscAsyncResources _instance;

	static public JTranscAsyncResources getInstance() {
		if (_instance == null) _instance = JTranscService.getFirst(JTranscAsyncResources.class);
		return _instance;
	}

	public void statAsync(ClassLoader classLoader, String path, JTranscAsyncHandler<JTranscFileStat> callback) {
		JTranscFileStat stat = new JTranscFileStat();
		stat.path = path;
		stat.exists = false;
		stat.length = 0L;
		stat.isDirectory = false;
		try {
			stat.length = classLoader.getResourceAsStream(path).available();
			stat.exists = true;
		} catch (Throwable ignored) {
		}
		callback.complete(stat, null);
	}

	public void openAsync(ClassLoader classLoader, String path, JTranscAsyncHandler<JTranscAsyncStream> callback) {
		final byte[] data;
		try {
			data = JTranscIoTools.readStreamFully(classLoader.getResourceAsStream(path));
		} catch (Throwable t) {
			callback.complete(null, t);
			return;
		}
		callback.complete(new JTranscAsyncBytesStream(data), null);
	}
}
