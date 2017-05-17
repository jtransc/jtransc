package com.jtransc.io.async;

import com.jtransc.async.JTranscAsyncHandler;
import com.jtransc.io.JTranscFileMode;

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

	public void openAsync(JTranscFileMode mode, JTranscAsyncHandler<JTranscAsyncStream> handler) {
		fs().open(path, mode, handler);
	}
}
