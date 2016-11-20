class JavaHaxeInput {
}

class Haxe extends haxe.io.Input {
	var i: {% CLASS java.io.InputStream %};
	public function new(i) {
		this.i = i;
	}
	override public function readByte():Int return this.i.{% METHOD java.io.InputStream:read:()I %}();
}

class Java extends {% CLASS java.io.InputStream %} {
	var i: haxe.io.Input;
	public function new(i) {
		super();
		this.i = i;
	}
	override public function {% METHOD java.io.InputStream:read:()I %}() return i.readByte();
}