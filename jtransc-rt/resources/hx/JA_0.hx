import haxe.io.*;

class JA_0 extends {% CLASS java.lang.Object %} {
    public var length:Int = 0;
	public var desc:String;

	override public function {% METHOD java.lang.Object:toString:()Ljava/lang/String; %}() {
		var className = Type.getClassName(Type.getClass(this));
	    return N.str('$className($length, $desc)');
	}

	override public function {% METHOD java.lang.Object:getClass:()Ljava/lang/Class; %}() {
		return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N.str(desc));
	}

    public function toArray():Array<Dynamic> {
        return [for (n in 0 ... length) getDynamic(n)];
    }

	#if debug
	private function checkBounds(index:Int):Int {
		if (index < 0 || index >= length) {
			trace('Index $index out of range 0..$length');
			throw new {% CLASS java.lang.ArrayIndexOutOfBoundsException %}().{% METHOD java.lang.ArrayIndexOutOfBoundsException:<init>:(I)V %}(index);
		}
		return index;
	}
	#else
	inline private function checkBounds(index:Int) {
		return index;
	}
	#end

	public function getDynamic(index:Int):Dynamic {
		checkBounds(index);
	    return null;
	}

	public function setDynamic(index:Int, value:Dynamic) checkBounds(index);

    public function sort(from:Int, to:Int) {
        if (from != 0 || to != length) throw "HaxeArray.sort not implementeed for ranges";
        //data.sort();
        //throw "HaxeArray.sort not implementeed";
    }

    public function clone():JA_0 {
        //return cast({% METHOD java.lang.Object:clone:()Ljava/lang/Object; %}(), JA_0);
        throw 'Must override';
    }

    public override function {% METHOD java.lang.Object:clone:()Ljava/lang/Object; %}():{% CLASS java.lang.Object %} {
    	return this.clone();
    }

	public function getArrayBufferView():ArrayBufferView throw 'Not implemented';
	public function getBytes():haxe.io.Bytes return getArrayBufferView().buffer;
	public function getElementBytesSize():Int throw 'Not implemented';
	public function getBytesLength():Int return this.length * getElementBytesSize();

	public function toByteArray():JA_B return new JA_B(0, UInt8Array.fromBytes(getBytes(), 0, Std.int(getBytesLength() / 1)));
	public function toCharArray():JA_C return new JA_C(0, UInt16Array.fromBytes(getBytes(), 0, Std.int(getBytesLength() / 2)));
	public function toShortArray():JA_S return new JA_S(0, UInt16Array.fromBytes(getBytes(), 0, Std.int(getBytesLength() / 2)));
	public function toIntArray():JA_I return new JA_I(0, Int32Array.fromBytes(getBytes(), 0, Std.int(getBytesLength() / 4)));
	//public function toLongArray():JA_J return new JA_J(0, Int32Array.fromBytes(getBytes(), 0, Std.int(getBytesLength() / 4)));
	public function toFloatArray():JA_F return new JA_F(0, Float32Array.fromBytes(getBytes(), 0, Std.int(getBytesLength() / 4)));
	public function toDoubleArray():JA_D return new JA_D(0, Float64Array.fromBytes(getBytes(), 0, Std.int(getBytesLength() / 8)));
}