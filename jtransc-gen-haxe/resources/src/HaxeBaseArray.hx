class HaxeBaseArray extends java_.lang.Object_ {
    public var length:Int = 0;

	override public function toString__Ljava_lang_String_():java_.lang.String_ {
	    return HaxeNatives.str("HaxeBaseArray");
	}

	public function getDynamic(index:Int):Dynamic {
	    return null;
	}

	public function setDynamic(index:Int, value:Dynamic) {
	}

    public function sort(from:Int, to:Int) {
        if (from != 0 || to != length) throw "HaxeArray.sort not implementeed for ranges";
        //data.sort();
        //throw "HaxeArray.sort not implementeed";
    }
}