class HaxeArrayBase extends {% CLASS java.lang.Object %} {
    public var length:Int = 0;
	public var desc:String;

	override public function {% METHOD java.lang.Object:toString:()Ljava/lang/String; %}() {
		var className = Type.getClassName(Type.getClass(this));
	    return HaxeNatives.str('$className($length, $desc)');
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

    public function clone():HaxeArrayBase {
        //return cast({% METHOD java.lang.Object:clone:()Ljava/lang/Object; %}(), HaxeArrayBase);
        throw 'Must override';
    }

    public override function {% METHOD java.lang.Object:clone:()Ljava/lang/Object; %}():{% CLASS java.lang.Object %} {
    	return this.clone();
    }
}