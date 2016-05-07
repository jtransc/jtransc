import haxe.Utf8;

abstract String16(String) {
	public var length(get, never):Int;

	public inline function new(wrap:String) {
		this = wrap;
	}

	@:from inline static public function fromString(s:String) return new String16(s);
	@:to inline public function toString() return this;

	private inline function get_length() return Utf8.length(this);
	//public inline function charCodeAt(index:Int) return Utf8.charCodeAt(this, index);
	//public inline function toLowerCase(): String16 return this.toLowerCase();
	//public inline function toUpperCase(): String16 return this.toUpperCase();
	//
	//public function substring(startIndex:Int, endIndex:Int = 2147483647):String16 return Utf8.sub(this, startIndex, endIndex - startIndex);
	//public function substr(pos:Int, len:Int = 2147483647):String16 return Utf8.sub(this, pos, len);
	//
	//public inline function indexOf(str:String, ?startIndex:Int):Int return this.indexOf(str, startIndex);
	//public inline function lastIndexOf(str:String, ?startIndex:Int):Int return this.lastIndexOf(str, startIndex);
	//
	//@:op(A + B) public inline function add(that:String16):String16 return this + that.toString();
	//
	//static public inline function compare(a:String16, b:String16):Int return Utf8.compare(a, b);
	//
	//@:op(A  < B) public inline function lt(that:String16):Bool return compare(this, that) < 0;
	//@:op(A  > B) public inline function gt(that:String16):Bool return compare(this, that) > 0;
	//@:op(A <= B) public inline function le(that:String16):Bool return compare(this, that) <= 0;
	//@:op(A >= B) public inline function ge(that:String16):Bool return compare(this, that) >= 0;
	//@:op(A == B) public inline function eq(that:String16):Bool return compare(this, that) == 0;
	//@:op(A != B) public inline function ne(that:String16):Bool return compare(this, that) != 0;

	public function getChars(): HaxeArrayChar {
		var out = new HaxeArrayChar(length);
		var n = 0;
		Utf8.iter(this, function(c) {
			out.set(n++, c);
		});
		return out;
	}
}
