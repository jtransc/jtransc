import haxe.Int64;

class HaxeIO {
}

#if js
@:jsRequire("buffer", "Buffer")
extern class Buffer {
	public function new(a:Dynamic):Void;
}
#end

class SyncFS {
	#if js
	static public var fs:Dynamic = untyped __js__("(typeof require != 'undefined') ? require('fs') : null;");
	#end

	static public function getLength(path:String):Int64 {
		#if js
		var stat = fs.lstatSync(path);
		return HaxeNatives.floatToLong(stat.size);
		#else
		return 0;
		#end
	}

	static public function getBooleanAttributes(path:String):Int {
		var out = 0;
		#if js
		try {
			var stat = fs.lstatSync(path);
			out |= BA_EXISTS;
			if (stat.isFile()) out |= BA_REGULAR;
			if (stat.isDirectory()) out |= BA_DIRECTORY;
		} catch (e:Dynamic) {
		}
		#end
		return out;
	}

	static public function checkAccess(path:String, flags:Int):Bool {
		#if js
		try {
			var mode = 0;
			if ((flags & ACCESS_EXECUTE) != 0) mode |= fs.X_OK;
			if ((flags & ACCESS_WRITE) != 0) mode |= fs.W_OK;
			if ((flags & ACCESS_READ) != 0) mode |= fs.R_OK;
			fs.accessSync(path, mode);
			return true;
		} catch (e:Dynamic) {
			return false;
		}
		#end
		return false;
	}

	static public function delete(path:String):Bool {
		try {
			#if js
			fs.unlinkSync(path);
			#else
			#end
			return true;
		} catch (e:Dynamic) {
			return false;
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
		#if js
		var flagsStr = '';
		if ((flags & O_RDONLY) != 0) flagsStr += 'r';
		if ((flags & O_RDWR) != 0) flagsStr += 'w';
		this.fd = fs.openSync(path, flagsStr); // @TODO: convert flags!!!
		var stat = fs.fstatSync(this.fd);
		this.length = stat.size;
		#end
	}

	public function syncioReadBytes(data:HaxeByteArray, offset:Int, length:Int):Int {
		if (length == 0) return 0;
		//trace('syncioReadBytes:$fd:$length');
		#if js
		var readed = fs.readSync(fd, new Buffer(data.getBytesData()), offset, length, this.position);
		this.position += readed;
		return Std.int(readed);
		#else
		throw 'Not implemented';
		#end
	}

	public function syncioWriteBytes(data:HaxeByteArray, offset:Int, length:Int):Int {
		if (length == 0) return 0;
		//trace('syncioWriteBytes:$fd:$length');
		#if js
		var written = fs.writeSync(fd, new Buffer(data.getBytesData()), offset, length, this.position);
		this.position += written;
		return Std.int(written);
		#else
		throw 'Not implemented';
		#end
	}

	public function syncioClose():Void {
		//trace('syncioClose');
		#if js
		fs.closeSync(fd);
		#else
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
		#end
		return length;
	}
}
