{{ HAXE_CLASS_ANNOTATIONS }}
class JavaHaxeInput {
}

{{ HAXE_CLASS_ANNOTATIONS }}
class Haxe extends haxe.io.Input {
	{{ HAXE_FIELD_ANNOTATIONS }}
	var i: {% CLASS java.io.InputStream %};

	{{ HAXE_CONSTRUCTOR_ANNOTATIONS }}
	public function new(i) {
		this.i = i;
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function readByte():Int return this.i{% IMETHOD java.io.InputStream:read:()I %}();
}

{{ HAXE_CLASS_ANNOTATIONS }}
class Java extends {% CLASS java.io.InputStream %} {
	{{ HAXE_FIELD_ANNOTATIONS }}
	var i: haxe.io.Input;

	{{ HAXE_CONSTRUCTOR_ANNOTATIONS }}
	public function new(i) {
		super();
		this.i = i;
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function {% METHOD java.io.InputStream:read:()I %}() return i.readByte();
}