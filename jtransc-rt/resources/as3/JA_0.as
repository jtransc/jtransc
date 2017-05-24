package {
// Abstract
public class JA_0 extends {% CLASS java.lang.Object %} {
	public var length: int;
	public var desc: String;

	public function JA_0(length: int, desc: String) {
		if (desc == null) throw new Error('JA_0.desc==null');
		this.length = length;
		this.desc = desc;
	}

	public function arraycopy(srcPos: int, dst: JA_0, dstPos: int, len: int, overlapping: Boolean): void {
		throw 'Must override JA_0.arraycopy for ' + this;
	}
}
}