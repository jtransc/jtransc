import 'dart:typed_data';
import 'dart:math' as Math;

// JTransc {{ JTRANSC_VERSION }} : https://github.com/jtransc/jtransc

// https://www.dartlang.org/articles/dart-vm/numeric-computation
class N {
	static final int MIN_INT32 = -2147483648;
	static final int MAX_INT32 = 2147483647;

	static final int MIN_INT64 = -9223372036854775808;
	static final int MAX_INT64 = 9223372036854775807;

	static final double DOUBLE_NAN = longBitsToDouble(0x7FF8000000000000);

	static final Int8List _tempI8 = new Int8List(8);
	static final Float32List _tempF32 = _tempI8.buffer.asFloat32List();
	static final Int32List _tempI32 = _tempI8.buffer.asInt32List();
	static final Float64List _tempF64 = _tempI8.buffer.asFloat64List();
	static final Int64List _tempI64 = _tempI8.buffer.asInt64List();

	static void init() {
	}

	// FAILS
	//static int  I(int v) { _tempI32[0] = v.toInt(); return _tempI32[0]; }
	//static int  L(int v) { _tempF64[0] = v.toInt(); return _tempF64[0]; }
	//static double F(int v) { _tempF32[0] = v.toDouble(); return _tempF32[0]; }
	//static double D(int v) { _tempF64[0] = v.toDouble(); return _tempF64[0]; }

	//static int  I(int v) { Int32List   out = new Int32List(1)  ; out[0] = v.toInt(); return out[0]; }
	//static int  L(int v) { Int64List   out = new Int64List(1)  ; out[0] = v.toInt(); return out[0]; }
	//static double F(int v) { Float32List out = new Float32List(1); out[0] = v.toDouble(); return out[0]; }
	//static double D(int v) { Float64List out = new Float64List(1); out[0] = v.toDouble(); return out[0]; }

	// https://github.com/dart-lang/fixnum/blob/master/lib/src/int.dart
	static int  I(int v) { return (v & 0x7fffffff) - (v & 0x80000000); }
	static int  L(int v) { return (v & 0x7fffffffffffffff) - (v & 0x8000000000000000); }
	static double F(int v) { return v.toDouble(); }
	static double D(int v) { return v.toDouble(); }

	static int ineg(int r) {
		if (r == MIN_INT32) return MIN_INT32;
		return I(-r);
	}

	static int iadd(int l, int r) { return I(l + r); }
	static int isub(int l, int r) { return I(l - r); }
	static int imul(int l, int r) { return I(l * r); }
	static int idiv(int l, int r) { return I(l ~/ r); }
	static int irem(int l, int r) { return I(l.remainder(r)); }
	static int iand(int l, int r) { return I(l & r); }
	static int ixor(int l, int r) { return I(l ^ r); }
	static int ior(int l, int r) { return I(l | r); }

	static int FIXSHIFT(int r) {
		if (r < 0) {
			return (32 - ((-r) & 0x1F)) & 0x1F;
		} else {
			return r & 0x1F;
		}
	}

	static int ishl(int l, int r) { return I(l << FIXSHIFT(r)); }
	static int ishr(int l, int r) { return I(l >> FIXSHIFT(r)); }
	static int iushr(int l, int r) { return I((l & 0xffffffff) >> FIXSHIFT(r)); }

	static void fillSecureRandomBytes(Int8List data) {
		var random = new Math.Random.secure();
		for (var n = 0; n < data.length; n++) data[n] = random.nextInt(0xFF);
	}

	//static int lnew(int high, int low) { return (high << 32) | low; }
	static int lneg(int v) {
		if (v == MIN_INT64) return MIN_INT64;
		return L(-v);
	}
	static int ladd(int l, int r) { return L(l + r); }
	static int lsub(int l, int r) { return L(l - r); }
	static int lmul(int l, int r) { return L(l * r); }
	static int lrem(int l, int r) { return L(l.remainder(L(r))); }
	static int ldiv(int l, int r) { return L(l ~/ r); }
	static int lxor(int l, int r) { return L(l ^ r); }
	static int lor (int l, int r) { return L(l | r); }
	static int land(int l, int r) { return L(l & r); }
	static int lshl(int l, int r) { return L(l << r); }
	static int lshr(int l, int r) { return L(l >> r); }
	static int lcmp(int l, int r) { return I((l < r) ? -1 : ((l > r) ? 1 : 0)); }
	static int lushr(int l, int r) { return L((l & 0xffffffffffffffff) >> r); }

	static int cmp (double a, double b) { return (a < b) ? (-1) : ((a > b) ? (1) : 0); }
	static int cmpl(double a, double b) { return (a.isNaN || b.isNaN) ? (-1) : N.cmp(a, b); }
	static int cmpg(double a, double b) { return (a.isNaN || b.isNaN) ? (1) : N.cmp(a, b); }

	static int floatToIntBits(double v) { _tempF32[0] = v; return _tempI32[0]; }
	static double intBitsToFloat(int v) { _tempI32[0] = v; return _tempF32[0]; }
	static int doubleToLongBits(double v) { _tempF64[0] = v; return _tempI64[0]; }
	static double longBitsToDouble(int v) { _tempI64[0] = v; return _tempF64[0]; }

	static double i2f(int v) { return v.toDouble(); }
	static double i2d(int v) { return v.toDouble(); }
	static double l2f(int v) { return v.toDouble(); }
	static double l2d(int v) { return v.toDouble(); }
	static int z2i(bool v)   { return v ? 1 : 0; }

	static int d2j(double v) { return L(v.toInt()); }
	static int i2j(int v)  { return L(v.toInt()); }
	static int f2j(double v) { return L(v.toInt()); }

	static int i(int v) { return (v.toInt()); }
	static int f2i(double v) { return I(v.toInt()); }
	static int d2i(double v) { return I(v.toInt()); }

	static int l2i(int v) { return I(v.toInt()); }
	static int i2b(int v) { return (v & 0x7F) - (v & 0x80); }
	static int i2s(int v) { return (v & 0x7FFF) - (v & 0x8000); }
	static int i2c(int v) { return (v & 0xFFFF); }

	static String charArrayToString(JA_C array) {
		return new String.fromCharCodes(array.data);
	}

	static String ichar(int v) {
		return new String.fromCharCode(v);
	}

	static JA_C stringToCharArray(String str) {
		var out = new JA_C(str.length);
		for (var n = 0; n < str.length; n++) out.data[n] = str.codeUnitAt(n);
		return out;
	}

	static {% CLASS java.lang.String %} str(String str) {
		//print("N.str($str)"); print(StackTrace.current);
		if (str == null) return null;
		var out = new {% CLASS java.lang.String %}();
		out._str = str;
		return out;
	}

	static {% CLASS java.lang.String %} strLitEscape(String str) {
		return N.str(str);
	}

	static String istr({% CLASS java.lang.String %} o) {
		//print("N.istr(...)");
		return (o != null) ? o._str : null;
	}

	static JA_L strArray(List<String> strs) {
		int len = strs.length;
		JA_L o = new JA_L(len, "[Ljava/lang/String;");
		for (int n = 0; n < len; n++) o.data[n] = N.str(strs[n]);
		return o;
	}

	static {% CLASS java.lang.Class %} resolveClass(String name) {
		return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N.str(name));
	}

	static void monitorEnter({% CLASS java.lang.Object %} o) {
	}

	static void monitorExit({% CLASS java.lang.Object %} o) {
	}

	static bool   unboxBool  ({% CLASS java.lang.Boolean %}   i) { return i.{% METHOD java.lang.Boolean:booleanValue %}(); }
	static int    unboxByte  ({% CLASS java.lang.Byte %}      i) { return i.{% METHOD java.lang.Byte:byteValue %}(); }
	static int    unboxShort ({% CLASS java.lang.Short %}     i) { return i.{% METHOD java.lang.Short:shortValue %}(); }
	static int    unboxChar  ({% CLASS java.lang.Character %} i) { return i.{% METHOD java.lang.Character:charValue %}(); }
	static int    unboxInt   ({% CLASS java.lang.Integer %}   i) { return i.{% METHOD java.lang.Integer:intValue %}(); }
	static int    unboxLong  ({% CLASS java.lang.Long %}      i) { return i.{% METHOD java.lang.Long:longValue %}(); }
	static double unboxFloat ({% CLASS java.lang.Float %}     i) { return i.{% METHOD java.lang.Float:floatValue %}(); }
	static double unboxDouble({% CLASS java.lang.Double %}    i) { return i.{% METHOD java.lang.Double:doubleValue %}(); }

	static {% CLASS java.lang.Object %}    boxVoid  (        ) { return null; }
	static {% CLASS java.lang.Boolean %}   boxBool  (bool   v) { return {% SMETHOD java.lang.Boolean:valueOf:(Z)Ljava/lang/Boolean; %}(v); }
	static {% CLASS java.lang.Byte %}      boxByte  (int    v) { return {% SMETHOD java.lang.Byte:valueOf:(B)Ljava/lang/Byte; %}(v); }
	static {% CLASS java.lang.Short %}     boxShort (int    v) { return {% SMETHOD java.lang.Short:valueOf:(S)Ljava/lang/Short; %}(v); }
	static {% CLASS java.lang.Character %} boxChar  (int    v) { return {% SMETHOD java.lang.Character:valueOf:(C)Ljava/lang/Character; %}(v); }
	static {% CLASS java.lang.Integer %}   boxInt   (int    v) { return {% SMETHOD java.lang.Integer:valueOf:(I)Ljava/lang/Integer; %}(v); }
	static {% CLASS java.lang.Long %}      boxLong  (int    v) { return {% SMETHOD java.lang.Long:valueOf:(J)Ljava/lang/Long; %}(v); }
	static {% CLASS java.lang.Float %}     boxFloat (double v) { return {% SMETHOD java.lang.Float:valueOf:(F)Ljava/lang/Float; %}(v); }
	static {% CLASS java.lang.Double %}    boxDouble(double v) { return {% SMETHOD java.lang.Double:valueOf:(D)Ljava/lang/Double; %}(v); }

	static void arraycopy({% CLASS java.lang.Object %} src, int srcPos, {% CLASS java.lang.Object %} dest, int destPos, int length) {
		if (src is JA_0) return src.copyTo(dest, srcPos, destPos, length);
		throw new Exception("Not implemented arraycopy for " + src.toString());
	}
}

abstract class JA_0 extends {% CLASS java.lang.Object %} {
	int length;
	String desc;

	JA_0(int length, String desc) {
		this.length = length;
		this.desc = desc;
	}

	void setArraySlice(int index, List data);
	void copyTo(JA_0 dest, int srcPos, int destPos, int length);
}

class JA_B extends JA_0 {
	Int8List data;
	JA_B(int length, [String desc = '[B']) : super(length, desc) { data = new Int8List(length); }
	void setArraySlice(int index, List data) {
		for (var n = 0; n < data.length; n++) this.data[index + n] = data[n];
	}
	void copyTo(JA_0 dest, int srcPos, int destPos, int length) {
		(dest as JA_B).data.setRange(destPos, destPos + length, this.data, srcPos);
	}
}

class JA_Z extends JA_B {
	JA_Z(int length, [String desc = '[Z']) : super(length, desc) { }
}

class JA_C extends JA_0 {
	Int32List data;
	JA_C(int length) : super(length, '[C') { data = new Int32List(length); }
	void setArraySlice(int index, List data) {
		for (var n = 0; n < data.length; n++) this.data[index + n] = data[n];
	}
	void copyTo(JA_0 dest, int srcPos, int destPos, int length) {
		(dest as JA_C).data.setRange(destPos, destPos + length, this.data, srcPos);
	}
}

class JA_S extends JA_0 {
	Int16List data;
	JA_S(int length) : super(length, '[S') { data = new Int16List(length); }
	void setArraySlice(int index, List data) {
		for (var n = 0; n < data.length; n++) this.data[index + n] = data[n];
	}
	void copyTo(JA_0 dest, int srcPos, int destPos, int length) {
		(dest as JA_S).data.setRange(destPos, destPos + length, this.data, srcPos);
	}
}

class JA_I extends JA_0 {
	Int32List data;
	JA_I(int length) : super(length, '[I') { data = new Int32List(length); }
	static JA_I T(List<int> values) {
		var out = new JA_I(values.length);
		for (var n = 0; n < out.length; n++) out.data[n] = values[n];
		return out;
	}
	void setArraySlice(int index, List data) {
		for (var n = 0; n < data.length; n++) this.data[index + n] = data[n];
	}
	void copyTo(JA_0 dest, int srcPos, int destPos, int length) {
		(dest as JA_I).data.setRange(destPos, destPos + length, this.data, srcPos);
	}
}

class JA_F extends JA_0 {
	Float32List data;
	JA_F(int length) : super(length, '[F') { data = new Float32List(length); }
	void setArraySlice(int index, List data) {
		for (var n = 0; n < data.length; n++) this.data[index + n] = data[n];
	}
	void copyTo(JA_0 dest, int srcPos, int destPos, int length) {
		(dest as JA_F).data.setRange(destPos, destPos + length, this.data, srcPos);
	}
}

class JA_D extends JA_0 {
	Float64List data;
	JA_D(int length) : super(length, '[D') { data = new Float64List(length); }
	void setArraySlice(int index, List data) {
		for (var n = 0; n < data.length; n++) this.data[index + n] = data[n];
	}
	void copyTo(JA_0 dest, int srcPos, int destPos, int length) {
		(dest as JA_D).data.setRange(destPos, destPos + length, this.data, srcPos);
	}
}

class JA_J extends JA_0 {
	List data;
	JA_J(int length) : super(length, '[J') { data = new List.filled(length, 0); }
	void setArraySlice(int index, List data) {
		for (var n = 0; n < data.length; n++) this.data[index + n] = data[n];
	}
	void copyTo(JA_0 dest, int srcPos, int destPos, int length) {
		(dest as JA_J).data.setRange(destPos, destPos + length, this.data, srcPos);
	}
}

class JA_L extends JA_0 {
	List data;
	JA_L(int length, String desc) : super(length, desc) { data = new List.filled(length, null); }

	static JA_0 createMultiSure(String desc, List<int> sizes) {
		return _createMultiSure(desc, 0, sizes);
	}

	static JA_0 _createMultiSure(String desc, int index, List<int> sizes) {
		if (!desc.startsWith("[")) return null;
		if (index >= sizes.length - 1) return JA_L.create(sizes[index], desc);
		int len = sizes[index];
		JA_L o = new JA_L(len, desc);
		String desc2 = desc.substring(1);
		for (int n = 0; n < len; n++) {
			o.data[n] = JA_L._createMultiSure(desc2, index + 1, sizes);
		}
		return o;
	}

	static create(int size, String desc) {
		switch (desc) {
			case "[Z": return new JA_Z(size);
			case "[B": return new JA_B(size);
			case "[C": return new JA_C(size);
			case "[S": return new JA_S(size);
			case "[I": return new JA_I(size);
			case "[J": return new JA_J(size);
			case "[F": return new JA_F(size);
			case "[D": return new JA_D(size);
			default: return new JA_L(size, desc);
		}
	}

	void setArraySlice(int index, List data) {
		for (var n = 0; n < data.length; n++) this.data[index + n] = data[n];
	}

	void copyTo(JA_0 dest, int srcPos, int destPos, int length) {
		(dest as JA_L).data.setRange(destPos, destPos + length, this.data, srcPos);
	}
}

class WrappedThrowable extends Error {
	{% CLASS java.lang.Object %} t;

	WrappedThrowable({% CLASS java.lang.Object %} t) {
		this.t = t;
	}

	String toString() {
		return t.toString();
	}
}

/* ## BODY ## */

main() {
	Bootstrap.Main(new List(0));
}