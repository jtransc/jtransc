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
			"var that = this;",
			"var mode = (N.istr(p2) == 'rw') ? FileMode.WRITE : FileMode.READ;",
			"N.futureToAsyncHandler(new File(N.istr(p0)).open(mode: mode), p3, (RandomAccessFile raf) { that.raf = raf; return that; });",
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
		@JTranscMethodBody(target = "dart", value = "N.futureToAsyncHandler(this.raf.writeFrom(p0.data, p1, p1 + p2), p3, (v) { return N.boxInt(p2); });"),
	})
	@Override
	public void writeAsync(byte[] data, int offset, int size, JTranscAsyncHandler<Integer> callback) {
		callback.complete(syncStream.write(data, offset, size), null);
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = "N.futureToAsyncHandler(this.raf.readInto(p0.data, p1, p1 + p2), p3, (v) { return N.boxInt(v.toInt()); });"),
	})
	@Override
	public void readAsync(byte[] data, int offset, int size, JTranscAsyncHandler<Integer> callback) {
		callback.complete(syncStream.read(data, offset, size), null);
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = "N.futureToAsyncHandler(this.raf.setPosition(p0.toInt()), p1, (v) { return N.boxLong(p0.toInt()); });"),
	})
	@Override
	public void setPositionAsync(long position, JTranscAsyncHandler<Long> callback) {
		syncStream.setPosition(position);
		callback.complete(syncStream.getPosition(), null);
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = "N.futureToAsyncHandler(this.raf.position(), p0, (v) { return N.boxLong(N.lnew(v)); });"),
	})
	@Override
	public void getPositionAsync(JTranscAsyncHandler<Long> callback) {
		callback.complete(syncStream.getPosition(), null);
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = "N.futureToAsyncHandler(this.raf.truncate(p0.toInt()), p1, (v) { return N.boxLong(p0.toInt()); });"),
	})
	@Override
	public void setLengthAsync(long length, JTranscAsyncHandler<Long> callback) {
		syncStream.setLength(length);
		callback.complete(length, null);
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = "N.futureToAsyncHandler(this.raf.length(), p0, (v) { return N.boxLong(N.lnew(v)); });"),
	})
	@Override
	public void getLengthAsync(JTranscAsyncHandler<Long> callback) {
		callback.complete(syncStream.getLength(), null);
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = "N.futureToAsyncHandler(this.raf.close(), p0, (v) { return N.boxInt(N.inew(0)); });"),
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
