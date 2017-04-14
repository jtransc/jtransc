import haxe.io.UInt8Array;
import haxe.io.Bytes;
import haxe.io.BytesData;

class JA_B extends JA_0 {
    public var data:UInt8Array = null;

    public function new(length:Int, data: UInt8Array = null) {
        super();
        if (data == null) data = new UInt8Array(length); else length = data.length;
        this.data = data;
        this.length = length;
        this.desc = "[B";
    }

	override public function getElementBytesSize():Int return 1;
	override public function getArrayBufferView() return data.view;
    public function getTypedArray() return data;

    override public function getBytes():Bytes {
    	#if js
    	return Bytes.ofData(data.getData().buffer);
    	#else
		return data.getData().bytes;
		#end
    }

    public function getBytesData():BytesData {
	    // typedef UInt8ArrayData = js.html.Uint8Array;
    	// typedef BytesData = js.html.ArrayBuffer;
    	#if js
    	return data.getData().buffer;
    	#else
		return data.getData().bytes.getData();
		#end
    }

    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        var out = new JA_B(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

    static public function fromUInt8Array(items:UInt8Array) {
        if (items == null) return null;
        var out = new JA_B(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

    static public function fromBytes(bytes:Bytes) {
        if (bytes == null) return null;
        var out = new JA_B(bytes.length);
        var bytesData = bytes.getData();
        for (n in 0 ... bytes.length) out.set(n, Bytes.fastGet(bytesData, n));
        return out;
    }

    public function get(index:Int):Int return N.i2b(this.data[checkBounds(index)]);
    public function set(index:Int, value:Int):Void this.data[checkBounds(index)] = value;

	public function getBool(index:Int):Bool return get(index) != 0;
	public function setBool(index:Int, value:Bool):Void set(index, value ? 1 : 0);

	override public function getDynamic(index:Int):Dynamic return get(index);
	override public function setDynamic(index:Int, value:Dynamic) set(index, value);

    public function join(separator:String) {
        var out = '';
        for (n in 0 ... length) {
            if (n != 0) out += separator;
            out += get(n);
        }
        return out;
    }

    public override function clone() {
        var out = new JA_B(length);
        copy(this, out, 0, 0, length);
        return out;
    }

    static public function copy(from:JA_B, to:JA_B, fromPos:Int, toPos:Int, length:Int) {
    	if (from == to && toPos > fromPos) {
			var n = length;
			while (--n >= 0) to.set(toPos + n, from.get(fromPos + n));
    	} else {
	        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
		}
    }

}