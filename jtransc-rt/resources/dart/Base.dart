import 'dart:typed_data';

// JTransc {{ JTRANSC_VERSION }} : https://github.com/jtransc/jtransc

class N {
	static void init() {
	}

	static int32 iushr(int32 l, int32 r) {
		//return l.shiftRightUnsigned(r);
		return (l & 0xffffffff) >> r;
	}

	static int64 lnew(int32 high, int32 low) {
		return new int64.fromInts(high, low);
	}

	static String charArrayToString(JA_C array) {
		return new String.fromCharCodes(array.data);
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

	static strArray(Array strs) {
		int len = strs.length;
		JA_L o = new JA_L(len, "[Ljava/lang/String;");
		for (int n = 0; n < len; n++) o[n] = N.str(strs[n]);
		return o;
	}
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
	JA_B(int32 length, [string desc = '[Z']) : super(length, desc) { }
}

class JA_C extends JA_0 {
	Int32List data;
	JA_C(int32 length) : super(length, '[C') { data = new Int32List(length); }
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

class JA_L extends JA_0 {
	List data;
	JA_L(int32 length, string desc) : super(length, desc) { data = new List.filled(length, null); }
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
	//print(N.istr(N.strLitEscape("HELLO")));
	Bootstrap.Main(new List(0));
	//print("hello from dart!");
	//print(new JA_B(10));
}