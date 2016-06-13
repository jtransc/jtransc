package com.jtransc.io;

import com.jtransc.annotation.JTranscAddFile;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.io.FileNotFoundException;
import java.io.IOException;

@JTranscAddFile(target = "js", priority = -2000, prepend = "js/io.js")
public class JTranscSyncIO {
	static public final int BA_EXISTS    = 0x01;
	static public final int BA_REGULAR   = 0x02;
	static public final int BA_DIRECTORY = 0x04;
	static public final int BA_HIDDEN    = 0x08;

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
			try {
				file.open(path, mode);
				return file;
			} catch (Throwable e) {
				e.printStackTrace();
				throw new FileNotFoundException(path);
			}
		}

		@Override
		@HaxeMethodBody("return HaxeIO.SyncFS.getLength(p0._str);")
		@JTranscMethodBody(target = "js", value = "return Int64.ofFloat(IO.getLength(N.istr(p0)));")
		native public long getLength(String file);

		@Override
		@HaxeMethodBody("return HaxeIO.SyncFS.delete(p0._str);")
		public native boolean delete(String file);

		@Override
		@HaxeMethodBody("return HaxeIO.SyncFS.getBooleanAttributes(p0._str);")
		@JTranscMethodBody(target = "js", value = "return IO.getBooleanAttributes(N.istr(p0));")
		native public int getBooleanAttributes(String file);

		@Override
		@HaxeMethodBody("return HaxeIO.SyncFS.checkAccess(p0._str, p1);")
		@JTranscMethodBody(target = "js", value = "return IO.checkAccess(N.istr(p0), p1);")
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

		@Override
		@HaxeMethodBody(target = "sys", value = "return HaxeNatives.str(Sys.getCwd());")
		@HaxeMethodBody(target = "js", value = "return HaxeNatives.str(untyped __js__('HaxeNatives.isNode() ? process.cwd() : \"/assets\"'));")
		@HaxeMethodBody("return HaxeNatives.str('');")
		@JTranscMethodBody(target = "js", value = "return N.str(IO.getCwd());")
		public native String getCwd();

		@Override
		@HaxeMethodBody(target = "sys", value = "return Sys.setCwd(p0._str);")
		@HaxeMethodBody(target = "js", value = "untyped __js__('process.chdir({0})', p0._str);")
		@HaxeMethodBody("")
		public native void setCwd(String path);
	};

	@HaxeAddMembers({
		"private var _stream = new HaxeIO.SyncStream();"
	})
	static private class JTranscIOSyncFile extends ImplStream {
		@JTranscMethodBody(target = "js", value = "this._stream = new IO.Stream();")
		public JTranscIOSyncFile() {
		}

		@HaxeMethodBody("_stream.syncioOpen(p0._str, p1);")
		@JTranscMethodBody(target = "js", value = "this._stream.open(N.istr(p0), p1);")
		native void open(String name, int mode) throws FileNotFoundException;

		@HaxeMethodBody("_stream.syncioClose();")
		@JTranscMethodBody(target = "js", value = "this._stream.close();")
		public native void close() throws IOException;

		@HaxeMethodBody("return _stream.syncioReadBytes(p0, p1, p2);")
		@JTranscMethodBody(target = "js", value = "return this._stream.read(p0.data, p1, p2);")
		public native int read(byte b[], int off, int len);

		@HaxeMethodBody("return _stream.syncioWriteBytes(p0, p1, p2);")
		@JTranscMethodBody(target = "js", value = "return this._stream.write(p0.data, p1, p2);")
		public native int write(byte b[], int off, int len);

		@HaxeMethodBody("return _stream.syncioPosition();")
		@JTranscMethodBody(target = "js", value = "return Int64.ofFloat(this._stream.getPosition());")
		public native long getPosition();

		@HaxeMethodBody("_stream.syncioSetPosition(p0);")
		@JTranscMethodBody(target = "js", value = "this._stream.setPosition(Int64.toFloat(p0));")
		public native void setPosition(long pos);

		@HaxeMethodBody("return _stream.syncioLength();")
		@JTranscMethodBody(target = "js", value = "return Int64.ofFloat(this._stream.getLength());")
		public native long getLength();

		@HaxeMethodBody("_stream.syncioSetLength(p0);")
		@JTranscMethodBody(target = "js", value = "this._stream.setLength(Int64.toFloat(p0));")
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
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.getTotalSpace");
			return parent.getTotalSpace(file);
		}

		public long getFreeSpace(String file) {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.getFreeSpace");
			return parent.getFreeSpace(file);
		}

		public long getUsableSpace(String file) {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.getUsableSpace");
			return parent.getUsableSpace(file);
		}

		public boolean setReadOnly(String file) {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.setReadOnly");
			return parent.setReadOnly(file);
		}

		public boolean setLastModifiedTime(String file, long time) {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.setLastModifiedTime");
			return parent.setLastModifiedTime(file, time);
		}

		public boolean rename(String fileOld, String fileNew) {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.rename");
			return parent.rename(fileOld, fileNew);
		}

		public boolean createDirectory(String file) {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.createDirectory");
			return parent.createDirectory(file);
		}

		public String[] list(String file) {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.list");
			return parent.list(file);
		}

		public boolean delete(String file) {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.delete");
			return parent.delete(file);
		}

		public boolean createFileExclusively(String file) {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.createFileExclusively");
			return parent.createFileExclusively(file);
		}

		public boolean setPermission(String file, int access, boolean enable, boolean owneronly) {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.setPermission");
			return parent.setPermission(file, access, enable, owneronly);
		}

		public long getLastModifiedTime(String file) {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.getLastModifiedTime");
			return parent.getLastModifiedTime(file);
		}

		public boolean checkAccess(String file, int access) {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.checkAccess");
			return parent.checkAccess(file, access);
		}

		public int getBooleanAttributes(String file) {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.getBooleanAttributes");
			return parent.getBooleanAttributes(file);
		}

		public String getCwd() {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.getCwd");
			return parent.getCwd();
		}

		public void setCwd(String path) {
			if (parent == null) throw new RuntimeException("Not implemented JTranscSyncIO.setCwd");
			parent.setCwd(path);
		}
	}

	static public abstract class ImplStream {
		private byte[] temp = new byte[1];

		abstract public void setPosition(long offset);

		abstract public long getPosition();

		abstract public void setLength(long length);

		abstract public long getLength();

		public int read() {
			return (read(temp, 0, 1) == 1) ? temp[0] : -1;
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
