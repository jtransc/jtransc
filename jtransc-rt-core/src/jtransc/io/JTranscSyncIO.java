package jtransc.io;

import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

import java.io.FileNotFoundException;
import java.io.IOException;

public class JTranscSyncIO {
	public static final int O_RDONLY = 1;
	public static final int O_RDWR = 2;
	public static final int O_SYNC = 4;
	public static final int O_DSYNC = 8;

	public static final int ACCESS_EXECUTE = 0x01;
	public static final int ACCESS_WRITE = 0x02;
	public static final int ACCESS_READ = 0x04;

	static public Impl impl = new Impl(null) {
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
		@HaxeMethodBody("return HaxeIO.SyncFS.delete(p0._str);")
		public native boolean delete(String file);

		@Override
		@HaxeMethodBody("return HaxeIO.SyncFS.getBooleanAttributes(p0._str);")
		native public int getBooleanAttributes(String file);

		@Override
		@HaxeMethodBody("return HaxeIO.SyncFS.checkAccess(p0._str, p1);")
		public native boolean checkAccess(String file, int access);

		@Override
		@HaxeMethodBody("return HaxeIO.SyncFS.createDirectory(p0._str);")
		public native boolean createDirectory(String file);

		@Override
		@HaxeMethodBody("return HaxeIO.SyncFS.rename(p0._str, p1._str);")
		public native boolean rename(String fileOld, String fileNew);

		@Override
		@HaxeMethodBody("return HaxeNatives.strArray(HaxeIO.SyncFS.list(p0._str));")
		public native String[] list(String file);
	};

	@HaxeAddMembers({
		"private var _stream = new HaxeIO.SyncStream();"
	})
	static private class JTranscIOSyncFile extends ImplStream {

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


	static public class ByteStream extends ImplStream {
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

	static public abstract class Impl {
		protected Impl parent;

		public Impl(Impl parent) {
			this.parent = parent;
		}

		public abstract ImplStream open(String path, int mode) throws FileNotFoundException;

		public long getLength(String path) {
			try {
				ImplStream stream = open(path, O_RDONLY);
				try {
					return stream.getLength();
				} finally {
					stream.close();
				}
			} catch (Throwable e) {
				return 0L;
			}
		}

		public long getTotalSpace(String file) {
			if (parent != null) return parent.getTotalSpace(file);
			throw new RuntimeException("Not implemented JTranscSyncIO.getTotalSpace");
		}

		public long getFreeSpace(String file) {
			if (parent != null) return parent.getFreeSpace(file);
			throw new RuntimeException("Not implemented JTranscSyncIO.getFreeSpace");
		}

		public long getUsableSpace(String file) {
			if (parent != null) return parent.getUsableSpace(file);
			throw new RuntimeException("Not implemented JTranscSyncIO.getUsableSpace");
		}

		public boolean setReadOnly(String file) {
			if (parent != null) return parent.setReadOnly(file);
			throw new RuntimeException("Not implemented JTranscSyncIO.setReadOnly");
		}

		public boolean setLastModifiedTime(String file, long time) {
			if (parent != null) return parent.setLastModifiedTime(file, time);
			throw new RuntimeException("Not implemented JTranscSyncIO.setLastModifiedTime");
		}

		public boolean rename(String fileOld, String fileNew) {
			if (parent != null) return parent.rename(fileOld, fileNew);
			throw new RuntimeException("Not implemented JTranscSyncIO.rename");
		}

		public boolean createDirectory(String file) {
			if (parent != null) return parent.createDirectory(file);
			throw new RuntimeException("Not implemented JTranscSyncIO.createDirectory");
		}

		public String[] list(String file) {
			if (parent != null) return parent.list(file);
			throw new RuntimeException("Not implemented JTranscSyncIO.list");
		}

		public boolean delete(String file) {
			if (parent != null) return parent.delete(file);
			throw new RuntimeException("Not implemented JTranscSyncIO.delete");
		}

		public boolean createFileExclusively(String file) {
			if (parent != null) return parent.createFileExclusively(file);
			throw new RuntimeException("Not implemented JTranscSyncIO.createFileExclusively");
		}

		public boolean setPermission(String file, int access, boolean enable, boolean owneronly) {
			if (parent != null) return parent.setPermission(file, access, enable, owneronly);
			throw new RuntimeException("Not implemented JTranscSyncIO.setPermission");
		}

		public long getLastModifiedTime(String file) {
			if (parent != null) return parent.getLastModifiedTime(file);
			throw new RuntimeException("Not implemented JTranscSyncIO.getLastModifiedTime");
		}

		public boolean checkAccess(String file, int access) {
			if (parent != null) return parent.checkAccess(file, access);
			throw new RuntimeException("Not implemented JTranscSyncIO.checkAccess");
		}

		public int getBooleanAttributes(String file) {
			if (parent != null) return parent.getBooleanAttributes(file);
			throw new RuntimeException("Not implemented JTranscSyncIO.getBooleanAttributes");
		}

	}

	static public abstract class ImplStream {
		private byte[] temp = new byte[1];

		abstract public void setPosition(long offset);

		abstract public long getPosition();

		abstract public void setLength(long length);

		abstract public long getLength();

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

		abstract public int read(byte[] data, int offset, int size);

		abstract public int write(byte[] data, int offset, int size);

		abstract public void close() throws IOException;
	}

	// @TODO: Remove this!
	static public JTranscIOSyncFile open(String name, int mode) throws FileNotFoundException {
		JTranscIOSyncFile out = new JTranscIOSyncFile();
		out.open(name, mode);
		return out;
	}

}
