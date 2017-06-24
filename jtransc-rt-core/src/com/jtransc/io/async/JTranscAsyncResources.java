package com.jtransc.io.async;

import com.jtransc.async.JTranscAsyncHandler;
import com.jtransc.io.JTranscFileStat;
import com.jtransc.io.JTranscIoTools;
import com.jtransc.service.JTranscService;

import java.io.FileNotFoundException;
import java.io.InputStream;

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
			InputStream resourceAsStream = classLoader.getResourceAsStream(path);
			stat.length = resourceAsStream.available();
			stat.exists = true;
		} catch (Throwable ignored) {
		}
		callback.complete(stat, null);
	}

	public void openAsync(ClassLoader classLoader, String path, JTranscAsyncHandler<JTranscAsyncStream> callback) {
		final byte[] data;
		try {
			try (final InputStream resourceAsStream = classLoader.getResourceAsStream(path)) {
				if (resourceAsStream == null) throw new FileNotFoundException(path);
				data = JTranscIoTools.readStreamFully(resourceAsStream);
			}
		} catch (Throwable t) {
			callback.complete(null, t);
			return;
		}
		callback.complete(new JTranscAsyncBytesStream(data), null);
	}

	public void readAllAsync(ClassLoader classLoader, String path, JTranscAsyncHandler<byte[]> callback) {
		final byte[] data;
		try {
			try (final InputStream resourceAsStream = classLoader.getResourceAsStream(path)) {
				if (resourceAsStream == null) throw new FileNotFoundException(path);
				data = JTranscIoTools.readStreamFully(resourceAsStream);
			}
		} catch (Throwable t) {
			callback.complete(null, t);
			return;
		}
		callback.complete(data, null);
	}
}
