import haxe.Int64;

class HaxeIO {
}

#if js
@:jsRequire("buffer", "Buffer")
extern class Buffer {
	public function new(a:Dynamic):Void;
}
#end

class SyncStream {
	private var position:Float = 0;
	private var length:Float = 0;

	#if js
	private var fd:Dynamic;
	private var fs:Dynamic = untyped __js__("require('fs');");
	#end

	public function new() {

	}

	//public static final int O_RDONLY = 1;
	//public static final int O_RDWR = 2;
	//public static final int O_SYNC = 4;
	//public static final int O_DSYNC = 8;

	public function syncioOpen(path:String, flags:Int):Void {
		//trace('syncioOpen:$path:$flags');
		this.position = 0;
		#if js
		var flagsStr = '';
		if ((flags & 1) != 0) flagsStr += 'r';
		if ((flags & 2) != 0) flagsStr += 'w';
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
		fd.setLength(HaxeNatives.longToFloat(length));
		return length;
	}
}
