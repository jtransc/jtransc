import haxe.Int64;

class HaxeIO {
}

class SyncFS {
	#if js
	static public var fs:Dynamic = HaxeNatives.isNode() ? untyped __js__("require('fs')") : null;
	#end

	static public function getLength(path:String):Int64 {
		#if js
			var stat = fs.lstatSync(path);
			return HaxeNatives.floatToLong(stat.size);
		#elseif sys
			return sys.FileSystem.stat(path).size;
		#else
			return 0;
		#end
	}

	static public function getBooleanAttributes(path:String):Int {
		var out = 0;
		try {
			#if js
				var stat = fs.lstatSync(path);
				out |= BA_EXISTS;
				if (stat.isFile()) out |= BA_REGULAR;
				if (stat.isDirectory()) out |= BA_DIRECTORY;
			#elseif sys
				var stat = sys.FileSystem.stat(path);
				out |= BA_EXISTS;
				if (sys.FileSystem.isDirectory(path)) {
					out |= BA_DIRECTORY;
				} else {
					out |= BA_REGULAR;
				}
			#end
		} catch (e:Dynamic) {
		}
		return out;
	}

	static public function checkAccess(path:String, flags:Int):Bool {
		try {
			#if js
				var mode = 0;
				if ((flags & ACCESS_EXECUTE) != 0) mode |= fs.X_OK;
				if ((flags & ACCESS_WRITE) != 0) mode |= fs.W_OK;
				if ((flags & ACCESS_READ) != 0) mode |= fs.R_OK;
				fs.accessSync(path, mode);
				return true;
			#elseif sys
				// @TODO: Implement this right!
				var stat = sys.FileSystem.stat(path);
				return true;
			#else
			#end
		} catch (e:Dynamic) {
		}
		return false;
	}

	static public function delete(path:String):Bool {
		try {
			#if js
				fs.unlinkSync(path);
			#elseif sys
				sys.FileSystem.deleteFile(path);
			#else
			#end
			return true;
		} catch (e:Dynamic) {
			return false;
		}
	}

	static public function createDirectory(path:String):Bool {
		try {
			#if js
				fs.mkdirSync(path);
			#elseif sys
				sys.FileSystem.createDirectory(path);
			#else
			#end
			return true;
		} catch (e:Dynamic) {
			return false;
		}
	}

	static public function rename(oldPath:String, newPath:String):Bool {
		try {
			#if js
				fs.renameSync(oldPath, newPath);
			#elseif sys
				sys.FileSystem.rename(oldPath, newPath);
			#else
			#end
			return true;
		} catch (e:Dynamic) {
			return false;
		}
	}

	static public function list(path:String):Array<String> {
		try {
			#if js
				return fs.readdirSync(path);
			#elseif sys
				return sys.FileSystem.readDirectory(path);
			#else
				return [];
			#end
		} catch (e:Dynamic) {
			return [];
		}
	}

	static private inline var BA_EXISTS    = 0x01;
	static private inline var BA_REGULAR   = 0x02;
	static private inline var BA_DIRECTORY = 0x04;
	static private inline var BA_HIDDEN    = 0x08;

	static private inline var ACCESS_EXECUTE = 0x01;
	static private inline var ACCESS_WRITE   = 0x02;
	static private inline var ACCESS_READ    = 0x04;

}

class SyncStream {
	private var position:Float = 0;
	private var length:Float = 0;

	#if js
	private var fd:Dynamic;
	public var fs:Dynamic = SyncFS.fs;
	private function createBuffer(arg:Dynamic):Dynamic return HaxeNatives.isNode() ? untyped __js__("new Buffer(arg)") : null;
	#end

	#if sys
	private var input:sys.io.FileInput;
	private var output:sys.io.FileOutput;
	#end

	public function new() {

	}

	static private inline var O_RDONLY = 1;
	static private inline var O_RDWR = 2;
	static private inline var O_SYNC = 4;
	static private inline var O_DSYNC = 8;

	public function syncioOpen(path:String, flags:Int):Void {
		//trace('syncioOpen:$path:$flags');
		this.position = 0;
		try {
			#if js
				var flagsStr = '';
				if ((flags & O_RDONLY) != 0) flagsStr += 'r';
				if ((flags & O_RDWR) != 0) flagsStr += 'w';
				this.fd = fs.openSync(path, flagsStr); // @TODO: convert flags!!!
				var stat = fs.fstatSync(this.fd);
				this.length = stat.size;
			#elseif sys
				if ((flags & O_RDONLY) != 0) {
					this.input = sys.io.File.read(path);
					this.input.seek(0, sys.io.FileSeek.SeekEnd);
					this.length = this.input.tell();
					this.input.seek(0, sys.io.FileSeek.SeekBegin);
				} else {
					this.output = sys.io.File.append(path);
					this.output.seek(0, sys.io.FileSeek.SeekEnd);
					this.length = this.output.tell();
					this.output.seek(0, sys.io.FileSeek.SeekBegin);
				}
			#else
				throw 'Not implemented syncioOpen';
			#end
		} catch (e:Dynamic) {
			HaxeNatives.throwRuntimeException('$e');
		}
	}

	public function syncioReadBytes(data:HaxeArrayByte, offset:Int, length:Int):Int {
		if (length == 0) return 0;
		//trace('syncioReadBytes:$fd:$length');
		#if js
			var readed = fs.readSync(fd, createBuffer(data.getBytesData()), offset, length, this.position);
			this.position += readed;
			return Std.int(readed);
		#elseif sys
        	this.input.seek(Std.int(this.position), sys.io.FileSeek.SeekBegin);
			var readed = this.input.readBytes(data.getBytes(), offset, length);
			this.position += readed;
			return readed;
		#else
			throw 'Not implemented syncioReadBytes';
		#end
	}

	public function syncioWriteBytes(data:HaxeArrayByte, offset:Int, length:Int):Int {
		if (length == 0) return 0;
		//trace('syncioWriteBytes:$fd:$length');
		#if js
			var written = fs.writeSync(fd, createBuffer(data.getBytesData()), offset, length, this.position);
			this.position += written;
			return Std.int(written);
		#elseif sys
        	this.output.seek(Std.int(this.position), sys.io.FileSeek.SeekBegin);
			var written = this.output.writeBytes(data.getBytes(), offset, length);
			this.position += written;
			return written;
		#else
			throw 'Not implemented syncioWriteBytes';
		#end
	}

	public function syncioClose():Void {
		//trace('syncioClose');
		#if js
			fs.closeSync(fd);
		#elseif sys
			if (input != null) { input.close(); input = null; }
			if (output != null) { output.close(); output = null; }
		#else
			throw 'Not implemented syncioClose';
		#end
	}

	public function syncioLength():Int64 {
		return HaxeNatives.floatToLong(this.length);
	}

	public function syncioPosition():Int64 {
		return HaxeNatives.floatToLong(this.position);
	}

	public function syncioSetPosition(offset:Int64):Int64 {
		this.position = HaxeNatives.longToFloat(offset);
		return offset;
	}

	public function syncioSetLength(length:Int64):Int64 {
		this.length = HaxeNatives.longToFloat(length);
		#if js
			fd.setLength(HaxeNatives.longToFloat(length));
		#elseif sys
			throw 'Not implemented syncioSetLength';
		#else
			throw 'Not implemented syncioSetLength';
		#end
		return length;
	}
}
