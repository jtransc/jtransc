// JTransc {{ JTRANSC_VERSION }} : https://github.com/jtransc/jtransc

import 'dart:typed_data';
import 'dart:math' as Math;
import 'dart:io';
//import 'dart:developer' as debugger;
{% for import in TARGET_IMPORTS %}
import '{{ import }}';
{% end %}

main() {
	Bootstrap.Main(new List(0));
}

// https://www.dartlang.org/articles/dart-vm/numeric-computation
class N {
	static final int MIN_INT32 = -2147483648;
	static final int MAX_INT32 = 2147483647;

	static final Int64 MIN_INT64 = N.lnew(-9223372036854775808);
	static final Int64 MAX_INT64 = N.lnew(9223372036854775807);
	static final Int64 ZERO_INT64 = new Int64(0);

	static final double DOUBLE_NAN = longBitsToDouble(N.lnew(0x7FF8000000000000));

	static final Int8List _tempI8 = new Int8List(8);
	static final Float32List _tempF32 = _tempI8.buffer.asFloat32List();
	static final Int32List _tempI32 = _tempI8.buffer.asInt32List();
	static final Float64List _tempF64 = _tempI8.buffer.asFloat64List();
	static final Int64List _tempI64 = _tempI8.buffer.asInt64List();

	static void init() {
	}

	// FAILS
	//static int  I(int v) { _tempI32[0] = v.toInt(); return _tempI32[0]; }
	//static int  L(int v) { _tempI64[0] = v.toInt(); return _tempI64[0]; }
	//static double F(int v) { _tempF32[0] = v.toDouble(); return _tempF32[0]; }
	//static double D(int v) { _tempF64[0] = v.toDouble(); return _tempF64[0]; }

	//static int  I(int v) { Int32List   out = new Int32List(1)  ; out[0] = v.toInt(); return out[0]; }
	//static int  L(int v) { Int64List   out = new Int64List(1)  ; out[0] = v.toInt(); return out[0]; }
	//static double F(int v) { Float32List out = new Float32List(1); out[0] = v.toDouble(); return out[0]; }
	//static double D(int v) { Float64List out = new Float64List(1); out[0] = v.toDouble(); return out[0]; }

	// https://github.com/dart-lang/fixnum/blob/master/lib/src/int.dart
	static int  I(int v) { return (v & 0x7fffffff) - (v & 0x80000000); }
	static double F(int v) { return v.toDouble(); }
	static double D(int v) { return v.toDouble(); }

	static int ineg(int r) {
		if (r == MIN_INT32) return MIN_INT32;
		return I(-r);
	}

	//static CHECK_CAST(i, clazz) {
	//	if (i == null) return null;
	//	if (!(i is clazz)) {
	//		throw new WrappedThrowable({% CONSTRUCTOR java.lang.ClassCastException:()V %}());
	//	}
	//	return i;
	//}

	static getJavaException(ee) {
		if (ee is WrappedThrowable) return ee.t;
		if (ee is CastError) return {% CONSTRUCTOR java.lang.ClassCastException:()V %}();
		return ee;
		//return new WrappedThrowable({% CONSTRUCTOR java.lang.ClassCastException:()V %}());
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

	static int LFIXSHIFT(int r) {
		if (r < 0) {
			return (64 - ((-r) & 0x3F)) & 0x3F;
		} else {
			return r & 0x3F;
		}
	}

	static int ishl(int l, int r) { return I(l << FIXSHIFT(r)); }
	static int ishr(int l, int r) { return I(l >> FIXSHIFT(r)); }
	static int iushr(int l, int r) { return I((l & 0xffffffff) >> FIXSHIFT(r)); }

	static int ishl_opt(int l, int r) { return I(l << r); }
	static int ishr_opt(int l, int r) { return I(l >> r); }
	static int iushr_opt(int l, int r) { return I((l & 0xffffffff) >> r); }

	static void fillSecureRandomBytes(Int8List data) {
		var random = new Math.Random.secure();
		for (var n = 0; n < data.length; n++) data[n] = random.nextInt(0xFF);
	}

	//static int lnew(int high, int low) { return (high << 32) | low; }

	static double i2f(int v) { return v.toDouble(); }
	static double i2d(int v) { return v.toDouble(); }
	static int floatToIntBits(double v) { _tempF32[0] = v; return _tempI32[0]; }
	static double intBitsToFloat(int v) { _tempI32[0] = v; return _tempF32[0]; }

	// LONG related

	static int inew(int v) { return v; }

	static Int64  lnew(int v) { return new Int64(v); }
	static Int64  lneg(int v) { return -v; }
	static Int64  ladd(Int64 l, Int64 r) { return (l + r); }
	static Int64  lsub(Int64 l, Int64 r) { return (l - r); }
	static Int64  lmul(Int64 l, Int64 r) { return (l * r); }
	static Int64  lrem(Int64 l, Int64 r) { return (l.remainder(r)); }
	static Int64  ldiv(Int64 l, Int64 r) { return (l ~/ r); }
	static Int64  lxor(Int64 l, Int64 r) { return (l ^ r); }
	static Int64  lor (Int64 l, Int64 r) { return (l | r); }
	static Int64  land(Int64 l, Int64 r) { return (l & r); }
	static Int64  lshl(Int64 l, int r) { return (l << LFIXSHIFT(r)); }
	static Int64  lshr(Int64 l, int r) { return (l >> LFIXSHIFT(r)); }
	static Int64  lushr(Int64 l, int r) { return l.shiftRightUnsigned(LFIXSHIFT(r)); }
	static Int64  lushr_opt(Int64 l, int r) { return l.shiftRightUnsigned(r); }
	static int    lcmp(Int64 l, Int64 r) { return l.compareTo(r); }
	static double j2f(Int64  v) { return v.toDouble(); }
	static double j2d(Int64  v) { return v.toDouble(); }
	static int    j2i(Int64  v) { return v.toInt32_v(); }
	static int    i2j(int    v) { return new Int64(v.toInt()); }
	static Int64  d2j(double v) {
		if (v.isNaN) {
			return ZERO_INT64;
		} else if (v.isFinite) {
			return new Int64(v.toInt());
		} else if (v >= 0) {
			return MAX_INT64;
		} else {
			return MIN_INT64;
		}
	}
	static Int64  f2j(double v) { return N.d2j(v); }

	static Int64 doubleToLongBits(double v) { _tempF64[0] = v.toDouble(); return new Int64(_tempI64[0]); }
	static double longBitsToDouble(Int64 v) { _tempI64[0] = v.toInt(); return _tempF64[0]; }

	// DOUBLE

	static int cmp (double a, double b) { return (a < b) ? (-1) : ((a > b) ? (1) : 0); }
	static int cmpl(double a, double b) { return (a.isNaN || b.isNaN) ? (-1) : N.cmp(a, b); }
	static int cmpg(double a, double b) { return (a.isNaN || b.isNaN) ? (1) : N.cmp(a, b); }

	static int z2i(bool v)   { return v ? 1 : 0; }

	static int i(int v) { return (v.toInt()); }
	static int d2i(double v) {
		if (v.isNaN) {
			return 0;
		} else if (v.isFinite) {
			return I(v.toInt());
		} else if (v >= 0) {
			return MAX_INT32;
		} else {
			return MIN_INT32;
		}
	}
	static int f2i(double v) {
		return N.d2i(v);
	}

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
		if (str == null) return null;
		var out = new {% CLASS java.lang.String %}();
		out._str = str;
		return out;
	}

	static {% CLASS java.lang.String %} strLitEscape(String str) { return N.str(str); }
	static String istr({% CLASS java.lang.String %} o) { return (o != null) ? o._str : null; }

	static JA_L strArray(List<String> strs) {
		int len = strs.length;
		JA_L o = new JA_L(len, "[Ljava/lang/String;");
		for (int n = 0; n < len; n++) o.data[n] = N.str(strs[n]);
		return o;
	}

	static {% CLASS java.lang.RuntimeException %} runtimeException(String msg) {
		print("runtimeException: '$msg'");
		return {% CONSTRUCTOR java.lang.RuntimeException:(Ljava/lang/String;)V %}(N.str(msg));
	}

	static {% CLASS java.lang.Class %} resolveClass(String name) {
		return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N.str(name));
	}

	static completeFuture({% CLASS com.jtransc.async.JTranscAsyncHandler %} handler, callback) {
		(() async {
			var result;
			try {
				result = await callback();
			} catch (e) {
				handler{% IMETHOD com.jtransc.async.JTranscAsyncHandler:complete %}(null, N.runtimeException("$e"));
				return;
			}
			handler{% IMETHOD com.jtransc.async.JTranscAsyncHandler:complete %}(result, null);
		})();
	}

	static void monitorEnter({% CLASS java.lang.Object %} o) {
	}

	static void monitorExit({% CLASS java.lang.Object %} o) {
	}

	static bool   unboxBool  ({% CLASS java.lang.Boolean %}   i) { return i{% IMETHOD java.lang.Boolean:booleanValue %}(); }
	static int    unboxByte  ({% CLASS java.lang.Byte %}      i) { return i{% IMETHOD java.lang.Byte:byteValue %}(); }
	static int    unboxShort ({% CLASS java.lang.Short %}     i) { return i{% IMETHOD java.lang.Short:shortValue %}(); }
	static int    unboxChar  ({% CLASS java.lang.Character %} i) { return i{% IMETHOD java.lang.Character:charValue %}(); }
	static int    unboxInt   ({% CLASS java.lang.Integer %}   i) { return i{% IMETHOD java.lang.Integer:intValue %}(); }
	static int    unboxLong  ({% CLASS java.lang.Long %}      i) { return i{% IMETHOD java.lang.Long:longValue %}(); }
	static double unboxFloat ({% CLASS java.lang.Float %}     i) { return i{% IMETHOD java.lang.Float:floatValue %}(); }
	static double unboxDouble({% CLASS java.lang.Double %}    i) { return i{% IMETHOD java.lang.Double:doubleValue %}(); }

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

	//static JA_L getStackTrace(Error error, int skip) {
	static JA_L getStackTrace(StackTrace st, int skip) {
		//var st = StackTrace.current;
		//var st = error.stackTrace;
		var lines = st.toString().split('\n');
		var o = new JA_L(lines.length - skip, "[Ljava/lang/StackTraceElement;");
		for (var n = 0; n < lines.length; n++) {
			var line = lines[n];

			// @TODO: Parse stacktrace elements
			var clazz = line;
			var method = '';
			var file = '';
			var lineNumber = 0;

			if (n >= skip) {
				o.data[n - skip] = {% CONSTRUCTOR java.lang.StackTraceElement:(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V %}(
					N.str(clazz), N.str(method), N.str(file), lineNumber
				);
			}
		}
		return o;
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
	static JA_B fromArray(String desc, List data) {
		var out = new JA_B(data.length);
		for (var n = 0; n < data.length; n++) out.data[n] = data[n];
		return out;
	}
}

class JA_Z extends JA_B {
	JA_Z(int length, [String desc = '[Z']) : super(length, desc) { }
	static JA_Z fromArray(String desc, List data) {
		var out = new JA_Z(data.length);
		for (var n = 0; n < data.length; n++) out.data[n] = data[n];
		return out;
	}
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
	static JA_C fromArray(String desc, List data) {
		var out = new JA_C(data.length);
		for (var n = 0; n < data.length; n++) out.data[n] = data[n];
		return out;
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
	static JA_S fromArray(String desc, List data) {
		var out = new JA_S(data.length);
		for (var n = 0; n < data.length; n++) out.data[n] = data[n];
		return out;
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
	static JA_I fromArray(String desc, List data) {
		var out = new JA_I(data.length);
		for (var n = 0; n < data.length; n++) out.data[n] = data[n];
		return out;
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
	static JA_F fromArray(String desc, List data) {
		var out = new JA_F(data.length);
		for (var n = 0; n < data.length; n++) out.data[n] = data[n];
		return out;
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

	static JA_D fromArray(String desc, List data) {
		var out = new JA_D(data.length);
		for (var n = 0; n < data.length; n++) out.data[n] = data[n];
		return out;
	}
}

class JA_J extends JA_0 {
	List data;
	JA_J(int length) : super(length, '[J') { data = new List.filled(length, Int64.ZERO); }
	void setArraySlice(int index, List<Int64> data) {
		for (var n = 0; n < data.length; n++) this.data[index + n] = data[n];
	}
	void copyTo(JA_0 dest, int srcPos, int destPos, int length) {
		(dest as JA_J).data.setRange(destPos, destPos + length, this.data, srcPos);
	}

	static JA_J fromArray(String desc, List data) {
		var out = new JA_J(data.length);
		for (var n = 0; n < data.length; n++) out.data[n] = data[n];
		return out;
	}
}

class JA_L extends JA_0 {
	List data;
	JA_L(int length, String desc) : super(length, desc) { data = new List.filled(length, null); }

	static JA_L fromArray(String desc, List data) {
		var out = new JA_L(data.length, desc);
		for (var n = 0; n < data.length; n++) out.data[n] = data[n];
		return out;
	}

	static JA_L T0(String desc) { return JA_L.fromArray(desc, []); }
	static JA_L T1(String desc, a) { return JA_L.fromArray(desc, [a]); }
	static JA_L T2(String desc, a, b) { return JA_L.fromArray(desc, [a, b]); }
	static JA_L T3(String desc, a, b, c) { return JA_L.fromArray(desc, [a, b, c]); }
	static JA_L T4(String desc, a, b, c, d) { return JA_L.fromArray(desc, [a, b, c, d]); }

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
	{% CLASS java.lang.Throwable %} t;

	WrappedThrowable({% CLASS java.lang.Throwable %} t) {
		this.t = t;
	}

	String toString() {
		return t.toString();
	}
}

// https://github.com/dart-lang/fixnum/tree/2d95f7d21690be6f077128f6bd5c29d875f71fee/lib/src

abstract class IntX implements Comparable<dynamic> {
	IntX operator +(other);
	IntX operator -(other);
	IntX operator -();
	IntX operator *(other);
	IntX operator %(other);
	IntX operator ~/(other);
	IntX remainder(other);
	IntX operator &(other);
	IntX operator |(other);
	IntX operator ^(other);
	IntX operator ~();
	IntX operator <<(int shiftAmount);
	IntX operator >>(int shiftAmount);
	IntX shiftRightUnsigned(int shiftAmount);
	int compareTo(other);
	bool operator ==(other);
	bool operator <(other);
	bool operator <=(other);
	bool operator >(other);
	bool operator >=(other);
	bool get isEven;
	bool get isMaxValue;
	bool get isMinValue;
	bool get isNegative;
	bool get isOdd;
	bool get isZero;
	int get hashCode;
	IntX abs();
	IntX clamp(lowerLimit, upperLimit);
	int get bitLength;
	int numberOfLeadingZeros();
	int numberOfTrailingZeros();
	IntX toSigned(int width);
	IntX toUnsigned(int width);
	List<int> toBytes();
	double toDouble();
	int toInt();
	Int32 toInt32();
	int toInt32_v();
	Int64 toInt64();
	String toString();
	String toHexString();
	String toRadixString(int radix);
}

class Int64 implements IntX {
	final int _l, _m, _h;
	static const int _BITS = 22;
	static const int _BITS01 = 44; // 2 * _BITS
	static const int _BITS2 = 20; // 64 - _BITS01
	static const int _MASK = 4194303; // (1 << _BITS) - 1
	static const int _MASK2 = 1048575; // (1 << _BITS2) - 1
	static const int _SIGN_BIT = 19; // _BITS2 - 1
	static const int _SIGN_BIT_MASK = 1 << _SIGN_BIT;
	static const Int64 MAX_VALUE = const Int64._bits(_MASK, _MASK, _MASK2 >> 1);
	static const Int64 MIN_VALUE = const Int64._bits(0, 0, _SIGN_BIT_MASK);
	static const Int64 ZERO = const Int64._bits(0, 0, 0);
	static const Int64 ONE = const Int64._bits(1, 0, 0);
	static const Int64 TWO = const Int64._bits(2, 0, 0);
	const Int64._bits(int this._l, int this._m, int this._h);
	static Int64 parseRadix(String s, int radix) {
		return _parseRadix(s, Int32._validateRadix(radix));
	}

	static Int64 _parseRadix(String s, int radix) {
		int i = 0;
		bool negative = false;
		if (s[0] == '-') {
			negative = true;
			i++;
		}
		int d0 = 0, d1 = 0, d2 = 0;  //  low, middle, high components.
		for (; i < s.length; i++) {
			int c = s.codeUnitAt(i);
			int digit = Int32._decodeDigit(c);
			if (digit < 0 || digit >= radix) throw new FormatException("Non-radix char code: $c");

			d0 = d0 * radix + digit;
			int carry = d0 >> _BITS;
			d0 = _MASK & d0;

			d1 = d1 * radix + carry;
			carry = d1 >> _BITS;
			d1 = _MASK & d1;

			d2 = d2 * radix + carry;
			d2 = _MASK2 & d2;
		}

		if (negative) return _negate(d0, d1, d2);

		return Int64._masked(d0, d1, d2);
	}

	static Int64 parseInt(String s) => _parseRadix(s, 10);
	static Int64 parseHex(String s) => _parseRadix(s, 16);

	factory Int64([int value=0]) {
		int v0 = 0, v1 = 0, v2 = 0;
		bool negative = false;
		if (value < 0) {
			negative = true;
			value = -value - 1;
		}
		v2 = value ~/ 17592186044416; // 2^44
		value -= v2 * 17592186044416;
		v1 = value ~/ 4194304; // 2^22
		value -= v1 * 4194304;
		v0 = value;

		if (negative) {
			v0 = ~v0;
			v1 = ~v1;
			v2 = ~v2;
		}
		return Int64._masked(v0, v1, v2);
	}

	factory Int64.fromBytes(List<int> bytes) {
		int top = bytes[7] & 0xff;
		top <<= 8;
		top |= bytes[6] & 0xff;
		top <<= 8;
		top |= bytes[5] & 0xff;
		top <<= 8;
		top |= bytes[4] & 0xff;

		int bottom = bytes[3] & 0xff;
		bottom <<= 8;
		bottom |= bytes[2] & 0xff;
		bottom <<= 8;
		bottom |= bytes[1] & 0xff;
		bottom <<= 8;
		bottom |= bytes[0] & 0xff;

		return new Int64.fromInts(top, bottom);
	}

	factory Int64.fromBytesBigEndian(List<int> bytes) {
		int top = bytes[0] & 0xff;
		top <<= 8;
		top |= bytes[1] & 0xff;
		top <<= 8;
		top |= bytes[2] & 0xff;
		top <<= 8;
		top |= bytes[3] & 0xff;

		int bottom = bytes[4] & 0xff;
		bottom <<= 8;
		bottom |= bytes[5] & 0xff;
		bottom <<= 8;
		bottom |= bytes[6] & 0xff;
		bottom <<= 8;
		bottom |= bytes[7] & 0xff;

		return new Int64.fromInts(top, bottom);
	}

	factory Int64.fromInts(int top, int bottom) {
		top &= 0xffffffff;
		bottom &= 0xffffffff;
		int d0 = _MASK & bottom;
		int d1 = ((0xfff & top) << 10) | (0x3ff & (bottom >> _BITS));
		int d2 = _MASK2 & (top >> 12);
		return  Int64._masked(d0, d1, d2);
	}

	static Int64 _promote(value) {
		if (value is Int64) return value;
		if (value is int) return new Int64(value);
		if (value is Int32) return value.toInt64();
		throw new ArgumentError.value(value);
	}

	Int64 operator +(other) {
		Int64 o = _promote(other);
		int sum0 = _l + o._l;
		int sum1 = _m + o._m + (sum0 >> _BITS);
		int sum2 = _h + o._h + (sum1 >> _BITS);
		return Int64._masked(sum0, sum1, sum2);
	}

	Int64 operator -(other) {
		Int64 o = _promote(other);
		return _sub(_l, _m, _h, o._l, o._m, o._h);
	}

	Int64 operator -() => _negate(_l, _m, _h);

	Int64 operator *(other) {
		Int64 o = _promote(other);

		int a0 = _l & 0x1fff;
		int a1 = (_l >> 13) | ((_m & 0xf) << 9);
		int a2 = (_m >> 4) & 0x1fff;
		int a3 = (_m >> 17) | ((_h & 0xff) << 5);
		int a4 = (_h & 0xfff00) >> 8;

		int b0 = o._l & 0x1fff;
		int b1 = (o._l >> 13) | ((o._m & 0xf) << 9);
		int b2 = (o._m >> 4) & 0x1fff;
		int b3 = (o._m >> 17) | ((o._h & 0xff) << 5);
		int b4 = (o._h & 0xfff00) >> 8;

		int p0 = a0 * b0; // << 0
		int p1 = a1 * b0; // << 13
		int p2 = a2 * b0; // << 26
		int p3 = a3 * b0; // << 39
		int p4 = a4 * b0; // << 52

		if (b1 != 0) {
			p1 += a0 * b1;
			p2 += a1 * b1;
			p3 += a2 * b1;
			p4 += a3 * b1;
		}
		if (b2 != 0) {
			p2 += a0 * b2;
			p3 += a1 * b2;
			p4 += a2 * b2;
		}
		if (b3 != 0) {
			p3 += a0 * b3;
			p4 += a1 * b3;
		}
		if (b4 != 0) {
			p4 += a0 * b4;
		}

		int c00 = p0 & 0x3fffff;
		int c01 = (p1 & 0x1ff) << 13;
		int c0 = c00 + c01;

		int c10 = p0 >> 22;
		int c11 = p1 >> 9;
		int c12 = (p2 & 0x3ffff) << 4;
		int c13 = (p3 & 0x1f) << 17;
		int c1 = c10 + c11 + c12 + c13;

		int c22 = p2 >> 18;
		int c23 = p3 >> 5;
		int c24 = (p4 & 0xfff) << 8;
		int c2 = c22 + c23 + c24;

		// Propagate high bits from c0 -> c1, c1 -> c2.
		c1 += c0 >> _BITS;
		c2 += c1 >> _BITS;

		return Int64._masked(c0, c1, c2);
	}

	Int64 operator %(other) => _divide(this, other, _RETURN_MOD);

	Int64 operator ~/(other) => _divide(this, other, _RETURN_DIV);

	Int64 remainder(other) => _divide(this, other, _RETURN_REM);

	Int64 operator &(other) {
		Int64 o = _promote(other);
		int a0 = _l & o._l;
		int a1 = _m & o._m;
		int a2 = _h & o._h;
		return Int64._masked(a0, a1, a2);
	}

	Int64 operator |(other) {
		Int64 o = _promote(other);
		int a0 = _l | o._l;
		int a1 = _m | o._m;
		int a2 = _h | o._h;
		return Int64._masked(a0, a1, a2);
	}

	Int64 operator ^(other) {
		Int64 o = _promote(other);
		int a0 = _l ^ o._l;
		int a1 = _m ^ o._m;
		int a2 = _h ^ o._h;
		return Int64._masked(a0, a1, a2);
	}

	Int64 operator ~() => Int64._masked(~_l, ~_m, ~_h);

	Int64 operator <<(int n) {
		if (n < 0) throw new ArgumentError.value(n);
		n &= 63;

		int res0, res1, res2;
		if (n < _BITS) {
		res0 = _l << n;
		res1 = (_m << n) | (_l >> (_BITS - n));
		res2 = (_h << n) | (_m >> (_BITS - n));
		} else if (n < _BITS01) {
		res0 = 0;
		res1 = _l << (n - _BITS);
		res2 = (_m << (n - _BITS)) | (_l >> (_BITS01 - n));
		} else {
		res0 = 0;
		res1 = 0;
		res2 = _l << (n - _BITS01);
		}

		return Int64._masked(res0, res1, res2);
	}

	Int64 operator >>(int n) {
		if (n < 0) throw new ArgumentError.value(n);
		n &= 63;

		int res0, res1, res2;

		int a2 = _h;
		bool negative = (a2 & _SIGN_BIT_MASK) != 0;
		if (negative && _MASK > _MASK2) a2 += (_MASK - _MASK2);

		if (n < _BITS) {
			res2 = _shiftRight(a2, n);
			if (negative) {
			res2 |= _MASK2 & ~(_MASK2 >> n);
			}
			res1 = _shiftRight(_m, n) | (a2 << (_BITS - n));
			res0 = _shiftRight(_l, n) | (_m << (_BITS - n));
		} else if (n < _BITS01) {
			res2 = negative ? _MASK2 : 0;
			res1 = _shiftRight(a2, n - _BITS);
			if (negative) {
			res1 |= _MASK & ~(_MASK >> (n - _BITS));
			}
			res0 = _shiftRight(_m, n - _BITS) | (a2 << (_BITS01 - n));
		} else {
			res2 = negative ? _MASK2 : 0;
			res1 = negative ? _MASK : 0;
			res0 = _shiftRight(a2, n - _BITS01);
			if (negative) {
				res0 |= _MASK & ~(_MASK >> (n - _BITS01));
			}
		}

		return Int64._masked(res0, res1, res2);
	}

	Int64 shiftRightUnsigned(int n) {
		if (n < 0) throw new ArgumentError.value(n);
		n &= 63;

		int res0, res1, res2;
		int a2 = _MASK2 & _h; // Ensure a2 is positive.
		if (n < _BITS) {
			res2 = a2 >> n;
			res1 = (_m >> n) | (a2 << (_BITS - n));
			res0 = (_l >> n) | (_m << (_BITS - n));
		} else if (n < _BITS01) {
			res2 = 0;
			res1 = a2 >> (n - _BITS);
			res0 = (_m >> (n - _BITS)) | (_h << (_BITS01 - n));
		} else {
			res2 = 0;
			res1 = 0;
			res0 = a2 >> (n - _BITS01);
		}

		return Int64._masked(res0, res1, res2);
	}

	bool operator ==(other) {
		Int64 o;
		if (other is Int64) {
		o = other;
		} else if (other is int) {
		if (_h == 0 && _m == 0) return _l == other;
		if ((_MASK & other) == other) return false;
			o = new Int64(other);
		} else if (other is Int32) {
			o = other.toInt64();
		}
		if (o != null) return _l == o._l && _m == o._m && _h == o._h;
		return false;
	}

	int compareTo(other) => _compareTo(other);

	int _compareTo(other) {
		Int64 o = _promote(other);
		int signa = _h >> (_BITS2 - 1);
		int signb = o._h >> (_BITS2 - 1);
		if (signa != signb) {
		return signa == 0 ? 1 : -1;
		}
		if (_h > o._h) {
		return 1;
		} else if (_h < o._h) {
		return -1;
		}
		if (_m > o._m) {
		return 1;
		} else if (_m < o._m) {
		return -1;
		}
		if (_l > o._l) {
		return 1;
		} else if (_l < o._l) {
		return -1;
		}
		return 0;
	}

	bool operator <(other) => _compareTo(other) < 0;
	bool operator <=(other) => _compareTo(other) <= 0;
	bool operator >(other) => this._compareTo(other) > 0;
	bool operator >=(other) => _compareTo(other) >= 0;

	bool get isEven => (_l & 0x1) == 0;
	bool get isMaxValue => (_h == _MASK2 >> 1) && _m == _MASK && _l == _MASK;
	bool get isMinValue => _h == _SIGN_BIT_MASK && _m == 0 && _l == 0;
	bool get isNegative => (_h & _SIGN_BIT_MASK) != 0;
	bool get isOdd => (_l & 0x1) == 1;
	bool get isZero => _h == 0 && _m == 0 && _l == 0;

	int get bitLength {
		if (isZero) return 0;
		int a0 = _l, a1 = _m, a2 = _h;
		if (isNegative) {
			a0 = _MASK & ~a0;
			a1 = _MASK & ~a1;
			a2 = _MASK2 & ~a2;
		}
		if (a2 != 0) return _BITS01 + a2.bitLength;
		if (a1 != 0) return _BITS + a1.bitLength;
		return a0.bitLength;
	}

	int get hashCode {
		// TODO(sra): Should we ensure that hashCode values match corresponding int?
		// i.e. should `new Int64(x).hashCode == x.hashCode`?
		int bottom = ((_m & 0x3ff) << _BITS) | _l;
		int top = (_h << 12) | ((_m >> 10) & 0xfff);
		return bottom ^ top;
	}

	Int64 abs() => this.isNegative ? -this : this;

	Int64 clamp(lowerLimit, upperLimit) {
		Int64 lower = _promote(lowerLimit);
		Int64 upper = _promote(upperLimit);
		if (this < lower) return lower;
		if (this > upper) return upper;
		return this;
	}

	int numberOfLeadingZeros() {
		int b2 = Int32._numberOfLeadingZeros(_h);
		if (b2 == 32) {
			int b1 = Int32._numberOfLeadingZeros(_m);
			if (b1 == 32) {
				return Int32._numberOfLeadingZeros(_l) + 32;
			} else {
				return b1 + _BITS2 - (32 - _BITS);
			}
		} else {
			return b2 - (32 - _BITS2);
		}
	}

	int numberOfTrailingZeros() {
		int zeros = Int32._numberOfTrailingZeros(_l);
		if (zeros < 32) return zeros;

		zeros = Int32._numberOfTrailingZeros(_m);
		if (zeros < 32) return _BITS + zeros;

		zeros = Int32._numberOfTrailingZeros(_h);
		if (zeros < 32) return _BITS01 + zeros;

		return 64;
	}

	Int64 toSigned(int width) {
		if (width < 1 || width > 64) throw new RangeError.range(width, 1, 64);
		if (width > _BITS01) {
		return Int64._masked(_l, _m, _h.toSigned(width - _BITS01));
		} else if (width > _BITS) {
		int m = _m.toSigned(width - _BITS);
		return m.isNegative
		? Int64._masked(_l, m, _MASK2)
		: Int64._masked(_l, m, 0);  // Masking for type inferrer.
		} else {
		int l = _l.toSigned(width);
		return l.isNegative
		? Int64._masked(l, _MASK, _MASK2)
		: Int64._masked(l, 0, 0);  // Masking for type inferrer.
		}
	}

	Int64 toUnsigned(int width) {
		if (width < 0 || width > 64) throw new RangeError.range(width, 0, 64);
		if (width > _BITS01) {
			int h = _h.toUnsigned(width - _BITS01);
			return Int64._masked(_l, _m, h);
		}
		if (width > _BITS) {
			int m = _m.toUnsigned(width - _BITS);
			return Int64._masked(_l, m, 0);
		}

		int l = _l.toUnsigned(width);
		return Int64._masked(l, 0, 0);
	}

	List<int> toBytes() {
		List<int> result = new List<int>(8);
		result[0] = _l & 0xff;
		result[1] = (_l >> 8) & 0xff;
		result[2] = ((_m << 6) & 0xfc) | ((_l >> 16) & 0x3f);
		result[3] = (_m >> 2) & 0xff;
		result[4] = (_m >> 10) & 0xff;
		result[5] = ((_h << 4) & 0xf0) | ((_m >> 18) & 0xf);
		result[6] = (_h >> 4) & 0xff;
		result[7] = (_h >> 12) & 0xff;
		return result;
	}

	double toDouble() => toInt().toDouble();

	int toInt() {
		int l = _l;
		int m = _m;
		int h = _h;
		if ((_h & _SIGN_BIT_MASK) != 0) {
			l = _MASK & ~_l;
			m = _MASK & ~_m;
			h = _MASK2 & ~_h;
			return -((1 + l) + (4194304 * m) + (17592186044416 * h));
		} else {
			return l + (4194304 * m) + (17592186044416 * h);
		}
	}

	Int32 toInt32() => new Int32(((_m & 0x3ff) << _BITS) | _l);
	int toInt32_v() => N.I(((_m & 0x3ff) << _BITS) | _l);
	Int64 toInt64() => this;
	String toString() => _toRadixString(10);

	// TODO(rice) - Make this faster by avoiding arithmetic.
	String toHexString() {
		if (isZero) return "0";
		Int64 x = this;
		String hexStr = "";
		while (!x.isZero) {
			int digit = x._l & 0xf;
			hexStr = "${_hexDigit(digit)}$hexStr";
			x = x.shiftRightUnsigned(4);
		}
		return hexStr;
	}

	String toRadixString(int radix) {
		return _toRadixString(Int32._validateRadix(radix));
	}

	String _toRadixString(int radix) {
		int d0 = _l;
		int d1 = _m;
		int d2 = _h;

		if (d0 == 0 && d1 == 0 && d2 == 0) return '0';

		String sign = '';
		if ((d2 & _SIGN_BIT_MASK) != 0) {
			sign = '-';
			d0 = 0 - d0;
			int borrow = (d0 >> _BITS) & 1;
			d0 &= _MASK;
			d1 = 0 - d1 - borrow;
			borrow = (d1 >> _BITS) & 1;
			d1 &= _MASK;
			d2 = 0 - d2 - borrow;
			d2 &= _MASK2;
		}

		int d4 = (d2 << 4) | (d1 >> 18);
		int d3 = (d1 >> 8) & 0x3ff;
		d2 = ((d1 << 2) | (d0 >> 20)) & 0x3ff;
		d1 = (d0 >> 10) & 0x3ff;
		d0 = d0 & 0x3ff;

		int fatRadix = _fatRadixTable[radix];

		String chunk1 = "", chunk2 = "", chunk3 = "";

		while (!(d4 == 0 && d3 == 0)) {
			int q = d4 ~/ fatRadix;
			int r = d4 - q * fatRadix;
			d4 = q;
			d3 += r << 10;

			q = d3 ~/ fatRadix;
			r = d3 - q * fatRadix;
			d3 = q;
			d2 += r << 10;

			q = d2 ~/ fatRadix;
			r = d2 - q * fatRadix;
			d2 = q;
			d1 += r << 10;

			q = d1 ~/ fatRadix;
			r = d1 - q * fatRadix;
			d1 = q;
			d0 += r << 10;

			q = d0 ~/ fatRadix;
			r = d0 - q * fatRadix;
			d0 = q;

			assert(chunk3 == "");
			chunk3 = chunk2;
			chunk2 = chunk1;
			chunk1 = (fatRadix + r).toRadixString(radix).substring(1);
		}
		int residue = (d2 << 20) + (d1 << 10) + d0;
		String leadingDigits = residue == 0 ? '' : residue.toRadixString(radix);
		return '$sign$leadingDigits$chunk1$chunk2$chunk3';
	}

	static const _fatRadixTable = const <int>[
		0,
		0,
		2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2
		* 2,
		3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3 * 3,
		4 * 4 * 4 * 4 * 4 * 4 * 4 * 4 * 4 * 4,
		5 * 5 * 5 * 5 * 5 * 5 * 5 * 5,
		6 * 6 * 6 * 6 * 6 * 6 * 6,
		7 * 7 * 7 * 7 * 7 * 7 * 7,
		8 * 8 * 8 * 8 * 8 * 8,
		9 * 9 * 9 * 9 * 9 * 9,
		10 * 10 * 10 * 10 * 10 * 10,
		11 * 11 * 11 * 11 * 11,
		12 * 12 * 12 * 12 * 12,
		13 * 13 * 13 * 13 * 13,
		14 * 14 * 14 * 14 * 14,
		15 * 15 * 15 * 15 * 15,
		16 * 16 * 16 * 16 * 16,
		17 * 17 * 17 * 17,
		18 * 18 * 18 * 18,
		19 * 19 * 19 * 19,
		20 * 20 * 20 * 20,
		21 * 21 * 21 * 21,
		22 * 22 * 22 * 22,
		23 * 23 * 23 * 23,
		24 * 24 * 24 * 24,
		25 * 25 * 25 * 25,
		26 * 26 * 26 * 26,
		27 * 27 * 27 * 27,
		28 * 28 * 28 * 28,
		29 * 29 * 29 * 29,
		30 * 30 * 30 * 30,
		31 * 31 * 31 * 31,
		32 * 32 * 32 * 32,
		33 * 33 * 33,
		34 * 34 * 34,
		35 * 35 * 35,
		36 * 36 * 36
	];

	String toDebugString() => "Int64[_l=$_l, _m=$_m, _h=$_h]";

	static Int64 _masked(int a0, int a1, int a2) => new Int64._bits(_MASK & a0, _MASK & a1, _MASK2 & a2);

	static Int64 _sub(int a0, int a1, int a2, int b0, int b1, int b2) {
		int diff0 = a0 - b0;
		int diff1 = a1 - b1 - ((diff0 >> _BITS) & 1);
		int diff2 = a2 - b2 - ((diff1 >> _BITS) & 1);
		return _masked(diff0, diff1, diff2);
	}

	static Int64 _negate(int b0, int b1, int b2) => _sub(0, 0, 0, b0, b1, b2);

	String _hexDigit(int digit) => "0123456789ABCDEF"[digit];

	// Work around dart2js bugs with negative arguments to '>>' operator.
	static int _shiftRight(int x, int n) {
		if (x >= 0) return x >> n;
		int shifted = x >> n;
		if (shifted >= 0x80000000) shifted -= 4294967296;
		return shifted;
	}

	// Implementation of '~/', '%' and 'remainder'.

	static Int64 _divide(Int64 a, other, int what) {
		Int64 b = _promote(other);
		if (b.isZero) throw new IntegerDivisionByZeroException();
		if (a.isZero) return ZERO;

		bool aNeg = a.isNegative;
		bool bNeg = b.isNegative;
		a = a.abs();
		b = b.abs();

		int a0 = a._l;
		int a1 = a._m;
		int a2 = a._h;

		int b0 = b._l;
		int b1 = b._m;
		int b2 = b._h;
		return _divideHelper(a0, a1, a2, aNeg, b0, b1, b2, bNeg, what);
	}

	static const _RETURN_DIV = 1;
	static const _RETURN_REM = 2;
	static const _RETURN_MOD = 3;

	static _divideHelper(int a0, int a1, int a2, bool aNeg,  int b0, int b1, int b2, bool bNeg,  int what) {
		int q0 = 0, q1 = 0, q2 = 0;  // result Q.
		int r0 = 0, r1 = 0, r2 = 0;  // result R.

		if (b2 == 0 && b1 == 0 && b0 < (1 << (30 - _BITS))) {
			q2 = a2 ~/ b0;
			int carry = a2 - q2 * b0;
			int d1 = a1 + (carry << _BITS);
			q1 = d1 ~/ b0;
			carry = d1 - q1 * b0;
			int d0 = a0 + (carry << _BITS);
			q0 = d0 ~/ b0;
			r0 = d0 - q0 * b0;
		} else {
			const double K2 = 17592186044416.0; // 2^44
			const double K1 = 4194304.0; // 2^22

			// Approximate double values for [a] and [b].
			double ad = a0 + K1 * a1 + K2 * a2;
			double bd = b0 + K1 * b1 + K2 * b2;
			// Approximate quotient.
			double qd = (ad / bd).floorToDouble();

			// Extract components of [qd] using double arithmetic.
			double q2d = (qd / K2).floorToDouble();
			qd = qd - K2 * q2d;
			double q1d = (qd / K1).floorToDouble();
			double q0d = qd - K1 * q1d;
			q2 = q2d.toInt();
			q1 = q1d.toInt();
			q0 = q0d.toInt();

			assert(q0 + K1 * q1 + K2 * q2 == (ad / bd).floorToDouble());
			assert(q2 == 0 || b2 == 0);  // Q and B can't both be big since Q*B <= A.

			// P = Q * B, using doubles to hold intermediates.
			// We don't need all partial sums since Q*B <= A.
			double p0d = q0d * b0;
			double p0carry = (p0d / K1).floorToDouble();
			p0d = p0d - p0carry * K1;
			double p1d = q1d * b0 + q0d * b1 + p0carry;
			double p1carry = (p1d / K1).floorToDouble();
			p1d = p1d - p1carry * K1;
			double p2d = q2d * b0 + q1d * b1 + q0d * b2 + p1carry;
			assert(p2d <= _MASK2);  // No partial sum overflow.

			// R = A - P
			int diff0 = a0 - p0d.toInt();
			int diff1 = a1 - p1d.toInt() - ((diff0 >> _BITS) & 1);
			int diff2 = a2 - p2d.toInt() - ((diff1 >> _BITS) & 1);
			r0 = _MASK & diff0;
			r1 = _MASK & diff1;
			r2 = _MASK2 & diff2;

			while ( r2 >= _SIGN_BIT_MASK || r2 > b2 || (r2 == b2 && (r1 > b1 || (r1 == b1 && r0 >= b0)))) {
				// Direction multiplier for adjustment.
				int m = (r2 & _SIGN_BIT_MASK) == 0 ? 1 : -1;
				// R = R - B  or  R = R + B
				int d0 = r0 - m * b0;
				int d1 = r1 - m * (b1 + ((d0 >> _BITS) & 1));
				int d2 = r2 - m * (b2 + ((d1 >> _BITS) & 1));
				r0 = _MASK & d0;
				r1 = _MASK & d1;
				r2 = _MASK2 & d2;

				// Q = Q + 1  or  Q = Q - 1
				d0 = q0 + m;
				d1 = q1 + m * ((d0 >> _BITS) & 1);
				d2 = q2 + m * ((d1 >> _BITS) & 1);
				q0 = _MASK & d0;
				q1 = _MASK & d1;
				q2 = _MASK2 & d2;
			}
		}

		// 0 <= R < B
		assert(Int64.ZERO <= new Int64._bits(r0, r1, r2));
		assert(r2 < b2 ||  // Handles case where B = -(MIN_VALUE)
		new Int64._bits(r0, r1, r2) < new Int64._bits(b0, b1, b2));

		assert(what == _RETURN_DIV || what == _RETURN_MOD || what == _RETURN_REM);
		if (what == _RETURN_DIV) {
			if (aNeg != bNeg) return _negate(q0, q1, q2);
			return Int64._masked(q0, q1, q2);  // Masking for type inferrer.
		}

		if (!aNeg) {
			return Int64._masked(r0, r1, r2);  // Masking for type inferrer.
		}

		if (what == _RETURN_MOD) {
			if (r0 == 0 && r1 == 0 && r2 == 0) return ZERO;
			return _sub(b0, b1, b2, r0, r1, r2);
		} else {
			return _negate(r0, r1, r2);
		}
	}
}

class Int32 implements IntX {
	static const Int32 MAX_VALUE = const Int32._internal(0x7FFFFFFF);
	static const Int32 MIN_VALUE = const Int32._internal(-0x80000000);
	static const Int32 ZERO = const Int32._internal(0);
	static const Int32 ONE = const Int32._internal(1);
	static const Int32 TWO = const Int32._internal(2);

	static const int _CC_0 = 48; // '0'.codeUnitAt(0)
	static const int _CC_9 = 57; // '9'.codeUnitAt(0)
	static const int _CC_a = 97; // 'a'.codeUnitAt(0)
	static const int _CC_z = 122; // 'z'.codeUnitAt(0)
	static const int _CC_A = 65; // 'A'.codeUnitAt(0)
	static const int _CC_Z = 90; // 'Z'.codeUnitAt(0)

	static int _decodeDigit(int c) {
		if (c >= _CC_0 && c <= _CC_9) return c - _CC_0;
		if (c >= _CC_a && c <= _CC_z) return c - _CC_a + 10;
		if (c >= _CC_A && c <= _CC_Z) return c - _CC_A + 10;
		return -1; // bad char code
	}

	static int _validateRadix(int radix) {
		if (2 <= radix && radix <= 36) return radix;
		throw new RangeError.range(radix, 2, 36, 'radix');
	}

	// TODO(rice) - Make this faster by converting several digits at once.
	static Int32 parseRadix(String s, int radix) {
		_validateRadix(radix);
		Int32 x = ZERO;
		for (int i = 0; i < s.length; i++) {
			int c = s.codeUnitAt(i);
			int digit = _decodeDigit(c);
			if (digit < 0 || digit >= radix) throw new FormatException("Non-radix code unit: $c");
			x = (x * radix) + digit;
		}
		return x;
	}

	static Int32 parseInt(String s) => new Int32(int.parse(s));
	static Int32 parseHex(String s) => parseRadix(s, 16);

	// Assumes i is <= 32-bit.
	static int _bitCount(int i) {
		i -= ((i >> 1) & 0x55555555);
		i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
		i = ((i + (i >> 4)) & 0x0F0F0F0F);
		i += (i >> 8);
		i += (i >> 16);
		return (i & 0x0000003F);
	}

	static int _numberOfLeadingZeros(int i) {
		i |= i >> 1;
		i |= i >> 2;
		i |= i >> 4;
		i |= i >> 8;
		i |= i >> 16;
		return _bitCount(~i);
	}

	static int _numberOfTrailingZeros(int i) => _bitCount((i & -i) - 1);

	final int _i;

	const Int32._internal(int i) : _i = i;

	Int32([int i = 0]) : _i = (i & 0x7fffffff) - (i & 0x80000000);

	int _toInt(val) {
		if (val is Int32) return val._i;
		if (val is int) return val;
		throw new ArgumentError(val);
	}

	IntX operator +(other) {
		if (other is Int64) return this.toInt64() + other;
		return new Int32(_i + _toInt(other));
	}

	IntX operator -(other) {
		if (other is Int64) return this.toInt64() - other;
		return new Int32(_i - _toInt(other));
	}

	Int32 operator -() => new Int32(-_i);

	IntX operator *(other) {
		if (other is Int64) return this.toInt64() * other;
		// TODO(rice) - optimize
		return (this.toInt64() * other).toInt32();
	}

	Int32 operator %(other) {
		// Result will be Int32
		if (other is Int64) return (this.toInt64() % other).toInt32();
		return new Int32(_i % _toInt(other));
	}

	Int32 operator ~/(other) {
		if (other is Int64) return (this.toInt64() ~/ other).toInt32();
		return new Int32(_i ~/ _toInt(other));
	}

	Int32 remainder(other) {
		if (other is Int64) {
			Int64 t = this.toInt64();
			return (t - (t ~/ other) * other).toInt32();
		}
		return this - (this ~/ other) * other;
	}

	Int32 operator &(other) {
		if (other is Int64) return (this.toInt64() & other).toInt32();
		return new Int32(_i & _toInt(other));
	}

	Int32 operator |(other) {
		if (other is Int64) return (this.toInt64() | other).toInt32();
		return new Int32(_i | _toInt(other));
	}

	Int32 operator ^(other) {
		if (other is Int64) return (this.toInt64() ^ other).toInt32();
		return new Int32(_i ^ _toInt(other));
	}

	Int32 operator ~() => new Int32(~_i);

	Int32 operator <<(int n) {
		if (n < 0) throw new ArgumentError(n);
		n &= 31;
		return new Int32(_i << n);
	}

	Int32 operator >>(int n) {
		if (n < 0) throw new ArgumentError(n);
		n &= 31;
		int value;
		if (_i >= 0) {
			value = _i >> n;
		} else {
			value = (_i >> n) | (0xffffffff << (32 - n));
		}
		return new Int32(value);
	}

	Int32 shiftRightUnsigned(int n) {
		if (n < 0) throw new ArgumentError(n);
		n &= 31;
		int value;
		if (_i >= 0) {
			value = _i >> n;
		} else {
			value = (_i >> n) & ((1 << (32 - n)) - 1);
		}
		return new Int32(value);
	}

	bool operator ==(other) {
		if (other is Int32) return _i == other._i;
		if (other is Int64) return this.toInt64() == other;
		if (other is int) return _i == other;
		return false;
	}

	int compareTo(other) {
		if (other is Int64) return this.toInt64().compareTo(other);
		return _i.compareTo(_toInt(other));
	}

	bool operator <(other) {
		if (other is Int64) return this.toInt64() < other;
		return _i < _toInt(other);
	}

	bool operator <=(other) {
		if (other is Int64) return this.toInt64() <= other;
		return _i <= _toInt(other);
	}

	bool operator >(other) {
		if (other is Int64) return this.toInt64() > other;
		return _i > _toInt(other);
	}

	bool operator >=(other) {
		if (other is Int64) return this.toInt64() >= other;
		return _i >= _toInt(other);
	}

	bool get isEven => (_i & 0x1) == 0;
	bool get isMaxValue => _i == 2147483647;
	bool get isMinValue => _i == -2147483648;
	bool get isNegative => _i < 0;
	bool get isOdd => (_i & 0x1) == 1;
	bool get isZero => _i == 0;
	int get bitLength => _i.bitLength;

	int get hashCode => _i;

	Int32 abs() => _i < 0 ? new Int32(-_i) : this;

	Int32 clamp(lowerLimit, upperLimit) {
		if (this < lowerLimit) {
			if (lowerLimit is IntX) return lowerLimit.toInt32();
			if (lowerLimit is int) return new Int32(lowerLimit);
			throw new ArgumentError(lowerLimit);
		}

		if (this > upperLimit) {
			if (upperLimit is IntX) return upperLimit.toInt32();
			if (upperLimit is int) return new Int32(upperLimit);
			throw new ArgumentError(upperLimit);
		}

		return this;
	}

	int numberOfLeadingZeros() => _numberOfLeadingZeros(_i);
	int numberOfTrailingZeros() => _numberOfTrailingZeros(_i);

	Int32 toSigned(int width) {
		if (width < 1 || width > 32) throw new RangeError.range(width, 1, 32);
		return new Int32(_i.toSigned(width));
	}

	Int32 toUnsigned(int width) {
		if (width < 0 || width > 32) throw new RangeError.range(width, 0, 32);
		return new Int32(_i.toUnsigned(width));
	}

	List<int> toBytes() {
		List<int> result = new List<int>(4);
		result[0] = _i & 0xff;
		result[1] = (_i >> 8) & 0xff;
		result[2] = (_i >> 16) & 0xff;
		result[3] = (_i >> 24) & 0xff;
		return result;
	}

	double toDouble() => _i.toDouble();
	int toInt() => _i;
	Int32 toInt32() => this;
	Int64 toInt64() => new Int64(_i);

	String toString() => _i.toString();
	String toHexString() => _i.toRadixString(16);
	String toRadixString(int radix) => _i.toRadixString(radix);

	int toInt32_v() => _i;
}

/* ## BODY ## */
