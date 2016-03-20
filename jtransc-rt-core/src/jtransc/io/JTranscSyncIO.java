package jtransc.io;

import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Native;

public class JTranscSyncIO {
	public static final int O_RDONLY = 1;
	public static final int O_RDWR = 2;
	public static final int O_SYNC = 4;
	public static final int O_DSYNC = 8;

	public static final int ACCESS_EXECUTE = 0x01;
	public static final int ACCESS_WRITE   = 0x02;
	public static final int ACCESS_READ    = 0x04;

	static public Impl impl = new Impl() {
		@Override
		public ImplStream open(String path, int mode) throws FileNotFoundException {
			JTranscIOSyncFile file = new JTranscIOSyncFile();
			file.open(path, mode);
			return file;
		}

		@Override
		@HaxeMethodBody("return HaxeIO.SyncFS.getLength(p0._str);")
		native public long getLength(String file);

		@Override
		@HaxeMethodBody("return HaxeIO.SyncFS.getBooleanAttributes(p0._str);")
		native public int getBooleanAttributes(String file);

		@Override
		public long getTotalSpace(String file) {
			throw new RuntimeException("Not implemented JTranscSyncIO.getTotalSpace");
		}

		@Override
		public long getFreeSpace(String file) {
			throw new RuntimeException("Not implemented JTranscSyncIO.getFreeSpace");
		}

		@Override
		public long getUsableSpace(String file) {
			throw new RuntimeException("Not implemented JTranscSyncIO.getUsableSpace");
		}

		@Override
		public boolean setReadOnly(String file) {
			throw new RuntimeException("Not implemented JTranscSyncIO.setReadOnly");
		}

		@Override
		public boolean setLastModifiedTime(String file, long time) {
			throw new RuntimeException("Not implemented JTranscSyncIO.setLastModifiedTime");
		}

		@Override
		public boolean rename(String fileOld, String fileNew) {
			throw new RuntimeException("Not implemented JTranscSyncIO.rename");
		}

		@Override
		public boolean createDirectory(String file) {
			throw new RuntimeException("Not implemented JTranscSyncIO.createDirectory");
		}

		@Override
		public String[] list(String file) {
			throw new RuntimeException("Not implemented JTranscSyncIO.list");
		}

		@Override
		@HaxeMethodBody("return HaxeIO.SyncFS.delete(p0._str);")
		public native boolean delete(String file);

		@Override
		public boolean createFileExclusively(String file) {
			throw new RuntimeException("Not implemented JTranscSyncIO.createFileExclusively");
		}

		@Override
		public boolean setPermission(String file, int access, boolean enable, boolean owneronly) {
			throw new RuntimeException("Not implemented JTranscSyncIO.setPermission");
		}

		@Override
		public long getLastModifiedTime(String file) {
			throw new RuntimeException("Not implemented JTranscSyncIO.getLastModifiedTime");
		}

		@Override
		@HaxeMethodBody("return HaxeIO.SyncFS.checkAccess(p0._str, p1);")
		public native boolean checkAccess(String file, int access);
	};

	static abstract private class ImplStreamBase implements ImplStream {
		private byte[] temp = new byte[1];

		public int read() {
			if (read(temp, 0, 1) == 1) {
				return temp[0];
			} else {
				return -1;
			}
		}

		public void write(int b) {
			temp[0] = (byte) b;
			write(temp, 0, 1);
		}
	}

	@HaxeAddMembers({
		"private var _stream = new HaxeIO.SyncStream();"
	})
	static private class JTranscIOSyncFile extends ImplStreamBase {

		@HaxeMethodBody("_stream.syncioOpen(p0._str, p1);")
		native void open(String name, int mode) throws FileNotFoundException;

		@HaxeMethodBody("_stream.syncioClose();")
		public native void close() throws IOException;

		@HaxeMethodBody("return _stream.syncioReadBytes(p0, p1, p2);")
		public native int read(byte b[], int off, int len);

		@HaxeMethodBody("return _stream.syncioWriteBytes(p0, p1, p2);")
		public native int write(byte b[], int off, int len);

		@HaxeMethodBody("return _stream.syncioPosition();")
		public native long getPosition();

		@HaxeMethodBody("_stream.syncioSetPosition(p0);")
		public native void setPosition(long pos);

		@HaxeMethodBody("return _stream.syncioLength();")
		public native long getLength();

		@HaxeMethodBody("_stream.syncioSetLength(p0);")
		public native void setLength(long newLength);
	}


	static public class ByteStream extends ImplStreamBase {
		private int position;
		private byte[] data;

		public ByteStream(byte[] data) {
			this.data = data;
			this.position = 0;
		}

		@Override
		public void setPosition(long offset) {
			this.position = (int) offset;
		}

		@Override
		public long getPosition() {
			return this.position;
		}

		@Override
		public void setLength(long length) {
			throw new RuntimeException("Not implemented ByteStream.setLength");
		}

		@Override
		public long getLength() {
			return this.data.length;
		}

		@Override
		public int read(byte[] data, int offset, int size) {
			int available = (int) (getLength() - getPosition());
			if (available <= 0) return -1;
			int toRead = Math.min(available, size);
			for (int n = 0; n < toRead; n++) {
				data[offset + n] = this.data[this.position + n];
			}
			this.position += toRead;
			return toRead;
		}

		@Override
		public int write(byte[] data, int offset, int size) {
			throw new RuntimeException("Not implemented ByteStream.write");
		}

		@Override
		public void close() {

		}
	}

	public interface Impl {
		ImplStream open(String path, int mode) throws FileNotFoundException;

		long getLength(String path);

		@HaxeMethodBody("return HaxeIO.SyncFS.getBooleanAttributes(p0._str);")
		int getBooleanAttributes(String path);

		long getTotalSpace(String file);

		long getFreeSpace(String file);

		long getUsableSpace(String file);

		boolean setReadOnly(String file);

		boolean setLastModifiedTime(String file, long time);

		boolean rename(String fileOld, String fileNew);

		boolean createDirectory(String file);

		String[] list(String file);

		boolean delete(String file);

		boolean createFileExclusively(String file);

		boolean setPermission(String file, int access, boolean enable, boolean owneronly);

		long getLastModifiedTime(String file);

		boolean checkAccess(String file, int access);

	}

	public interface ImplStream {
		void setPosition(long offset);

		long getPosition();

		void setLength(long length);

		long getLength();

		int read();

		void write(int data);

		int read(byte[] data, int offset, int size);

		int write(byte[] data, int offset, int size);

		void close() throws IOException;
	}

	// @TODO: Remove this!
	static public JTranscIOSyncFile open(String name, int mode) throws FileNotFoundException {
		JTranscIOSyncFile out = new JTranscIOSyncFile();
		out.open(name, mode);
		return out;
	}

}
