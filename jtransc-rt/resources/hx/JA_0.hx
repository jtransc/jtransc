import haxe.io.*;

{{ HAXE_CLASS_ANNOTATIONS }}
class JA_0 extends {% CLASS java.lang.Object %} {
    {{ HAXE_FIELD_ANNOTATIONS }}
    public var length:Int = 0;

    {{ HAXE_FIELD_ANNOTATIONS }}
	public var desc:String;

	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function {% METHOD java.lang.Object:toString:()Ljava/lang/String; %}() {
		var className = Type.getClassName(Type.getClass(this));
	    return N.str('$className($length, $desc)');
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function {% METHOD java.lang.Object:getClass:()Ljava/lang/Class; %}() {
		return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N.str(desc));
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
    public function toArray():Array<Dynamic> {
        return [for (n in 0 ... length) getDynamic(n)];
    }

	#if debug
	{{ HAXE_METHOD_ANNOTATIONS }}
	private function checkBounds(index:Int):Int {
		if (index < 0 || index >= length) {
			trace('Index $index out of range 0..$length');
			throw new {% CLASS java.lang.ArrayIndexOutOfBoundsException %}().{% METHOD java.lang.ArrayIndexOutOfBoundsException:<init>:(I)V %}(index);
		}
		return index;
	}
	#else
	{{ HAXE_METHOD_ANNOTATIONS }}
	inline private function checkBounds(index:Int) {
		return index;
	}
	#end

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function getDynamic(index:Int):Dynamic {
		checkBounds(index);
	    return null;
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function setDynamic(index:Int, value:Dynamic) checkBounds(index);

	{{ HAXE_METHOD_ANNOTATIONS }}
    public function sort(from:Int, to:Int) {
        if (from != 0 || to != length) throw "HaxeArray.sort not implementeed for ranges";
        //data.sort();
        //throw "HaxeArray.sort not implementeed";
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    public function clone():JA_0 {
        //return cast({% METHOD java.lang.Object:clone:()Ljava/lang/Object; %}(), JA_0);
        throw 'Must override';
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    public override function {% METHOD java.lang.Object:clone:()Ljava/lang/Object; %}():{% CLASS java.lang.Object %} {
    	return this.clone();
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function getArrayBufferView():ArrayBufferView throw 'Not implemented';

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function getBytes():haxe.io.Bytes return getArrayBufferView().buffer;

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function getElementBytesSize():Int throw 'Not implemented';

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function getBytesLength():Int return this.length * getElementBytesSize();

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function toByteArray():JA_B return new JA_B(0, UInt8Array.fromBytes(getBytes(), 0, Std.int(getBytesLength() / 1)));

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function toCharArray():JA_C return new JA_C(0, UInt16Array.fromBytes(getBytes(), 0, Std.int(getBytesLength() / 2)));

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function toShortArray():JA_S return new JA_S(0, UInt16Array.fromBytes(getBytes(), 0, Std.int(getBytesLength() / 2)));

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function toIntArray():JA_I return new JA_I(0, Int32Array.fromBytes(getBytes(), 0, Std.int(getBytesLength() / 4)));

	//public function toLongArray():JA_J return new JA_J(0, Int32Array.fromBytes(getBytes(), 0, Std.int(getBytesLength() / 4)));

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function toFloatArray():JA_F return new JA_F(0, Float32Array.fromBytes(getBytes(), 0, Std.int(getBytesLength() / 4)));

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function toDoubleArray():JA_D return new JA_D(0, Float64Array.fromBytes(getBytes(), 0, Std.int(getBytesLength() / 8)));
}