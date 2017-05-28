#if cpp
class MyStringBuf {
	var parts = [];
	private var _length: Int = 0;

	public var length(get, never): Int;

	public function get_length() {
		return _length;
	}

	public function new() {
	}
	public function add(str: String) {
		_length += str.length;
		parts.push(str);
	}
	public function addChar(c: Int) {
		add(N.ichar(c));
	}
	public function toString() {
		return parts.join('');
	}
}
#else
typedef MyStringBuf = StringBuf;
#end
