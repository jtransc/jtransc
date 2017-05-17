package com.jtransc.io.async;

import com.jtransc.annotation.JTranscAddMembers;
import com.jtransc.annotation.JTranscAddMembersList;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscMethodBodyList;
import com.jtransc.async.JTranscAsyncHandler;
import com.jtransc.io.JTranscSyncIO;

import java.io.FileNotFoundException;
import java.io.IOException;

@JTranscAddMembersList({
	@JTranscAddMembers(target = "dart", value = "RandomAccessFile raf;"),
})
public class JTranscAsyncFileStream extends JTranscAsyncStream {
	JTranscSyncIO.ImplStream syncStream;

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = {
			"var mode = (N.istr(p2) == 'rw') ? FileMode.WRITE : FileMode.READ;",
			"N.completeFuture(p3, () async {",
			"	this.raf = await new File(N.istr(p0)).open(mode: mode);",
			"	return this;",
			"});",
		})
	})
	public void openAsync(String path, int mode, String modestr, JTranscAsyncHandler<JTranscAsyncFileStream> callback) {
		try {
			syncStream = JTranscSyncIO.impl.open(path, mode);
			callback.complete(this, null);
		} catch (FileNotFoundException e) {
			callback.complete(null, e);
		}
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = {
			"N.completeFuture(p4, () async {",
			"	await this.raf.setPosition(p0.toInt());",
			"	await this.raf.writeFrom(p1.data, p2, p2 + p3);",
			"	return N.boxInt(p3);",
			"});",
		}),
	})
	@Override
	public void writeAsync(long position, byte[] data, int offset, int size, JTranscAsyncHandler<Integer> callback) {
		syncStream.setPosition(position);
		callback.complete(syncStream.write(data, offset, size), null);
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = {
			"N.completeFuture(p4, () async {",
			"	await this.raf.setPosition(p0.toInt());",
			"	var count = await this.raf.readInto(p1.data, p2, p2 + p3);",
			"	return N.boxInt(count);",
			"});",
		}),
	})
	@Override
	public void readAsync(long position, byte[] data, int offset, int size, JTranscAsyncHandler<Integer> callback) {
		syncStream.setPosition(position);
		callback.complete(syncStream.read(data, offset, size), null);
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = "N.completeFuture(p1, () async { await this.raf.truncate(p0.toInt()); return N.boxLong(p0.toInt()); });"),
	})
	@Override
	public void setLengthAsync(long length, JTranscAsyncHandler<Long> callback) {
		syncStream.setLength(length);
		callback.complete(length, null);
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = "N.completeFuture(p0, () async { var length = await this.raf.length(); return N.boxLong(N.lnew(length.toInt())); });"),
	})
	@Override
	public void getLengthAsync(JTranscAsyncHandler<Long> callback) {
		callback.complete(syncStream.getLength(), null);
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = "N.completeFuture(p0, () async { await this.raf.close(); return N.boxInt(0); });"),
	})
	@Override
	public void closeAsync(JTranscAsyncHandler<Integer> callback) {
		try {
			syncStream.close();
			callback.complete(0, null);
		} catch (IOException e) {
			callback.complete(null, e);
		}
	}
}
