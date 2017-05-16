import 'dart:typed_data';
import 'dart:math' as Math;

// JTransc {{ JTRANSC_VERSION }} : https://github.com/jtransc/jtransc

// https://www.dartlang.org/articles/dart-vm/numeric-computation
class N {
	static final int32 MIN_INT32 = -2147483648;
	static final int32 MAX_INT32 = 2147483647;

	static Int8List _tempI8 = new Int8List(8);
	static Float32List _tempF32 = _tempI8.buffer.asFloat32List();
	static Int32List _tempI32 = _tempI8.buffer.asInt32List();
	static Float64List _tempF64 = _tempI8.buffer.asFloat64List();
	static Int64List _tempI64 = _tempI8.buffer.asInt64List();

	static void init() {
	}

	static {% CLASS java.lang.Class %} resolveClass(String name) {
		return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N.str(name));
	}

	// FAILS
	//static int32  I(int v) { _tempI32[0] = v.toInt(); return _tempI32[0]; }
	//static int64  L(int v) { _tempF64[0] = v.toInt(); return _tempF64[0]; }
	//static double F(int v) { _tempF32[0] = v.toDouble(); return _tempF32[0]; }
	//static double D(int v) { _tempF64[0] = v.toDouble(); return _tempF64[0]; }

	static int32  I(int v) { var out = new Int32List(1); out[0] = v.toInt(); return out[0]; }
	static int64  L(int v) { var out = new Int64List(1); out[0] = v.toInt(); return out[0]; }
	static double F(int v) { var out = new Float32List(1); out[0] = v.toDouble(); return out[0]; }
	static double D(int v) { var out = new Float64List(1); out[0] = v.toDouble(); return out[0]; }



	static int32 ineg(int32 r) {
		if (r == MIN_INT32) return MIN_INT32;
		return I(-r);
	}
	static int32 iadd(int32 l, int32 r) { return I(I(l) + I(r)); }
	static int32 isub(int32 l, int32 r) { return I(I(l) - I(r)); }
	static int32 imul(int32 l, int32 r) { return I(I(l) * I(r)); }
	static int32 idiv(int32 l, int32 r) { return I(I(l) ~/ I(r)); }
	static int32 irem(int32 l, int32 r) { return I((I(l) % I(r)).toInt()); }

	static int32 iand(int32 l, int32 r) { return I(I(l) & I(r)); }
	static int32 ixor(int32 l, int32 r) { return I(I(l) ^ I(r)); }
	static int32 ior(int32 l, int32 r) { return I(I(l) | I(r)); }

	static int32 FIXSHIFT(int32 r) {
		return I(r) & 0x1F;
	}

	static int32 FIXSHIFT2(int32 r) {
		//return 32 - (I(r) & 0x1F);
		return (I(r) & 0x1F);
	}

	static int32 ishl(int32 l, int32 r) {
		if (r >= 0) {
			return I(I(l) << FIXSHIFT(r));
		} else {
			return I(I(l) >> FIXSHIFT2(r));
		}
	}
	static int32 ishr(int32 l, int32 r) {
		if (r >= 0) {
			return I(I(l) >> FIXSHIFT(r));
		} else {
			return I(I(l) << FIXSHIFT2(r));
		}
	}
	static int32 iushr(int32 l, int32 r) {
		if (r >= 0) {
			return I((I(l).toInt() & 0xffffffff) >> FIXSHIFT(r));
		} else {
			return I((I(l).toInt() & 0xffffffff) << FIXSHIFT2(r));
		}
	}

	static void fillSecureRandomBytes(Int8List data) {
		var random = new Math.Random.secure();
		for (var n = 0; n < data.length; n++) data[n] = random.nextInt(0xFF);
	}

	//static int64 lnew(int32 high, int32 low) { return (high << 32) | low; }
	static int64 ladd(int64 l, int64 r) { return L(L(l) + L(r)); }
	static int64 lsub(int64 l, int64 r) { return L(L(l) - L(r)); }
	static int64 lmul(int64 l, int64 r) { return L(L(l) * L(r)); }
	static int64 lrem(int64 l, int64 r) { return L(L(l) % L(r)); }
	static int64 ldiv(int64 l, int64 r) { return L(L(l) ~/ L(r)); }
	static int64 lxor(int64 l, int64 r) { return L(L(l) ^ L(r)); }
	static int64 lor (int64 l, int64 r) { return L(L(l) | L(r)); }
	static int64 land(int64 l, int64 r) { return L(L(l) & L(r)); }
	static int64 lshl(int64 l, int64 r) { return L(L(l) << L(r)); }
	static int64 lshr(int64 l, int64 r) { return L(L(l) >> L(r)); }
	static int64 lcmp(int64 l, int64 r) { return I((l < r) ? -1 : ((l > r) ? 1 : 0)); }
	static int64 lushr(int64 l, int32 r) { return L((L(l) & 0xffffffffffffffff) >> r); }

	static int32 cmp (double a, double b) { return (a < b) ? (-1) : ((a > b) ? (1) : 0); }
	static int32 cmpl(double a, double b) { return (a.isNaN || b.isNaN) ? (-1) : N.cmp(a, b); }
	static int32 cmpg(double a, double b) { return (a.isNaN || b.isNaN) ? (1) : N.cmp(a, b); }

	static int32 floatToIntBits(double v) { _tempF32[0] = v; return _tempI32[0]; }
	static double intBitsToFloat(int32 v) { _tempI32[0] = v; return _tempF32[0]; }
	static int64 doubleToLongBits(double v) { _tempF64[0] = v; return _tempI64[0]; }
	static double longBitsToDouble(int64 v) { _tempI64[0] = v; return _tempF64[0]; }

	static double i2f(int32 v) { return F(v.toDouble()); }
	static double i2d(int32 v) { return D(v); }
	static double l2f(int64 v) { return v; }
	static double l2d(int64 v) { return v; }
	static int32 z2i(bool v) { return v ? 1 : 0; }
	static int64 d2j(double v) { return v; }
	static int64 i2j(int32 v) { return v; }

	static int32 i(int32 v) { return (v.toInt()); }
	static int32 f2i(double v) { return I(v); }
	static int32 d2i(double v) { return I(v); }
	static int32 l2i(int32 v) { return I(v); }
	static int32 i2b(int32 v) {
		var out = new Int8List(1);
		out[0] = v.toInt();
		return out[0];
	}
	static int32 i2s(int32 v) {
		var out = new Int16List(1);
		out[0] = v.toInt();
		return out[0];
	}
	static int64 i2c(int32 v) { return I(v.toInt() & 0xFFFF); }

	static String charArrayToString(JA_C array) {
		return new String.fromCharCodes(array.data);
	}

	static String ichar(int32 v) {
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

	static JA_L strArray(Array strs) {
		int len = strs.length;
		JA_L o = new JA_L(len, "[Ljava/lang/String;");
		for (int n = 0; n < len; n++) o[n] = N.str(strs[n]);
		return o;
	}

	static void monitorEnter({% CLASS java.lang.Object %} o) {
	}

	static void monitorExit({% CLASS java.lang.Object %} o) {
	}

	static bool   unboxBool  ({% CLASS java.lang.Boolean %}   i) { return i.{% METHOD java.lang.Boolean:booleanValue %}(); }
	static sbyte  unboxByte  ({% CLASS java.lang.Byte %}      i) { return i.{% METHOD java.lang.Byte:byteValue %}(); }
	static short  unboxShort ({% CLASS java.lang.Short %}     i) { return i.{% METHOD java.lang.Short:shortValue %}(); }
	static ushort unboxChar  ({% CLASS java.lang.Character %} i) { return i.{% METHOD java.lang.Character:charValue %}(); }
	static int    unboxInt   ({% CLASS java.lang.Integer %}   i) { return i.{% METHOD java.lang.Integer:intValue %}(); }
	static long   unboxLong  ({% CLASS java.lang.Long %}      i) { return i.{% METHOD java.lang.Long:longValue %}(); }
	static float  unboxFloat ({% CLASS java.lang.Float %}     i) { return i.{% METHOD java.lang.Float:floatValue %}(); }
	static double unboxDouble({% CLASS java.lang.Double %}    i) { return i.{% METHOD java.lang.Double:doubleValue %}(); }

	static {% CLASS java.lang.Object %}    boxVoid  (        ) { return null; }
	static {% CLASS java.lang.Boolean %}   boxBool  (bool   v) { return {% SMETHOD java.lang.Boolean:valueOf:(Z)Ljava/lang/Boolean; %}(v); }
	static {% CLASS java.lang.Byte %}      boxByte  (sbyte  v) { return {% SMETHOD java.lang.Byte:valueOf:(B)Ljava/lang/Byte; %}(v); }
	static {% CLASS java.lang.Short %}     boxShort (short  v) { return {% SMETHOD java.lang.Short:valueOf:(S)Ljava/lang/Short; %}(v); }
	static {% CLASS java.lang.Character %} boxChar  (ushort v) { return {% SMETHOD java.lang.Character:valueOf:(C)Ljava/lang/Character; %}(v); }
	static {% CLASS java.lang.Integer %}   boxInt   (int    v) { return {% SMETHOD java.lang.Integer:valueOf:(I)Ljava/lang/Integer; %}(v); }
	static {% CLASS java.lang.Long %}      boxLong  (long   v) { return {% SMETHOD java.lang.Long:valueOf:(J)Ljava/lang/Long; %}(v); }
	static {% CLASS java.lang.Float %}     boxFloat (float  v) { return {% SMETHOD java.lang.Float:valueOf:(F)Ljava/lang/Float; %}(v); }
	static {% CLASS java.lang.Double %}    boxDouble(double v) { return {% SMETHOD java.lang.Double:valueOf:(D)Ljava/lang/Double; %}(v); }
}

class JA_0 extends {% CLASS java.lang.Object %} {
	int32 length;
	String desc;

	JA_0(int32 length, String desc) {
		this.length = length;
		this.desc = desc;
	}

	void setArraySlice(int index, Array data) {
		for (var n = 0; n < data.length; n++) this.data[index + n] = data[n];
	}
}

class JA_B extends JA_0 {
	Int8List data;
	JA_B(int32 length, [string desc = '[B']) : super(length, desc) { data = new Int8List(length); }
}

class JA_Z extends JA_B {
	JA_Z(int32 length, [string desc = '[Z']) : super(length, desc) { }
}

class JA_C extends JA_0 {
	Int32List data;
	JA_C(int32 length) : super(length, '[C') { data = new Int32List(length); }
}

class JA_S extends JA_0 {
	Int16List data;
	JA_S(int32 length) : super(length, '[S') { data = new Int16List(length); }
}

class JA_I extends JA_0 {
	Int32List data;
	JA_I(int32 length) : super(length, '[I') { data = new Int32List(length); }
	static JA_I T(Array values) {
		var out = new JA_I(values.length);
		for (var n = 0; n < out.length; n++) out.data[n] = values[n];
		return out;
	}
}

class JA_F extends JA_0 {
	Float32List data;
	JA_F(int32 length) : super(length, '[F') { data = new Float32List(length); }
}

class JA_D extends JA_0 {
	Float64List data;
	JA_D(int32 length) : super(length, '[D') { data = new Float64List(length); }
}

class JA_L extends JA_0 {
	List data;
	JA_L(int32 length, string desc) : super(length, desc) { data = new List.filled(length, null); }

	static JA_0 createMultiSure(string desc, Array<int32> sizes) {
		return _createMultiSure(desc, 0, sizes);
	}

	static JA_0 _createMultiSure(string desc, int index, Array<int32> sizes) {
		if (!desc.startsWith("[")) return null;
		if (index >= sizes.length - 1) return JA_L.create(sizes[index], desc);
		int len = sizes[index];
		JA_L o = new JA_L(len, desc);
		string desc2 = desc.substring(1);
		for (int n = 0; n < len; n++) {
			o.data[n] = JA_L._createMultiSure(desc2, index + 1, sizes);
		}
		return o;
	}

	static create(int size, string desc) {
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
}

class JA_J extends JA_0 {
	List data;
	JA_J(int32 length) : super(length, '[J') { data = new List.filled(length, 0); }
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