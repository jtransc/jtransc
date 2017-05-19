import haxe.io.*;

{{ HAXE_CLASS_ANNOTATIONS }}
class JA_0 extends {% CLASS java.lang.Object %} {
    {{ HAXE_FIELD_ANNOTATIONS }} public var length:Int = 0;
    {{ HAXE_FIELD_ANNOTATIONS }} public var desc:String;

	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function {% METHOD java.lang.Object:toString:()Ljava/lang/String; %}() {
		var className = Type.getClassName(Type.getClass(this));
	    return N.str('$className($length, $desc)');
	}

	{{ HAXE_METHOD_ANNOTATIONS }} override public function {% METHOD java.lang.Object:getClass:()Ljava/lang/Class; %}() return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N.str(desc));
	{{ HAXE_METHOD_ANNOTATIONS }} public function toArray():Array<Dynamic> return [for (n in 0 ... length) getDynamic(n)];

	#if debug
	{{ HAXE_METHOD_ANNOTATIONS }}
	private function checkBounds(index:Int):Int {
		if (index < 0 || index >= length) {
			trace('Index $index out of range 0..$length');
			throw new {% CLASS java.lang.ArrayIndexOutOfBoundsException %}(){% IMETHOD java.lang.ArrayIndexOutOfBoundsException:<init>:(I)V %}(index);
		}
		return index;
	}
	#else
	{{ HAXE_METHOD_ANNOTATIONS }} inline private function checkBounds(index:Int) return index;
	#end

	{{ HAXE_METHOD_ANNOTATIONS }} public function getDynamic(index:Int):Dynamic { checkBounds(index); return null; }
	{{ HAXE_METHOD_ANNOTATIONS }} public function setDynamic(index:Int, value:Dynamic) checkBounds(index);
	{{ HAXE_METHOD_ANNOTATIONS }} public function sort(from:Int, to:Int) if (from != 0 || to != length) throw "HaxeArray.sort not implementeed for ranges";
	{{ HAXE_METHOD_ANNOTATIONS }} public function clone():JA_0 throw 'Must override';
	{{ HAXE_METHOD_ANNOTATIONS }} public override function {% METHOD java.lang.Object:clone:()Ljava/lang/Object; %}():{% CLASS java.lang.Object %} return this.clone();
	{{ HAXE_METHOD_ANNOTATIONS }} public function getArrayBufferView():ArrayBufferView throw 'Not implemented';
	{{ HAXE_METHOD_ANNOTATIONS }} public function getBytes():haxe.io.Bytes return getArrayBufferView().buffer;
	{{ HAXE_METHOD_ANNOTATIONS }} public function getElementBytesSize():Int throw 'Not implemented';
	{{ HAXE_METHOD_ANNOTATIONS }} public function getBytesLength():Int return this.length * getElementBytesSize();
	{{ HAXE_METHOD_ANNOTATIONS }} public function copyTo(srcPos: Int, dst: JA_0, dstPos: Int, length: Int) throw 'Must override';
}