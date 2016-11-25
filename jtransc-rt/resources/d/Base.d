// https://github.com/jtransc/jtransc

import std.conv;
import std.algorithm;
import std.stdio;
import core.stdc.string;
import std.math;
import std.random;

int ilen(string s) {
	return cast(int)s.length;
}

int ilen(wstring s) {
	return cast(int)s.length;
}

int ilen(T)(T[] s) {
	return cast(int)s.length;
}

abstract class JA_0 : {% CLASS java.lang.Object %} {
	wstring desc;

	this(wstring desc) { this.desc = desc; }

	abstract int itemLen();
	abstract int length();
	abstract byte* ptr();
	static public void copy(JA_0 src, int srcOffset, JA_0 dst, int dstOffset, int len) {
		if (src.itemLen != dst.itemLen) return;
		int itemLen = src.itemLen;
		auto srcP = src.ptr + (srcOffset * itemLen);
		auto dstP = dst.ptr + (dstOffset * itemLen);
		core.stdc.string.memmove(cast(void*)dstP, cast(void*)srcP, (len * itemLen));
	}
}

class JA_Template(U) : JA_0 {
	U[] data;
	wstring desc;

	this(int len, wstring desc) {
		super(desc);
		this.data = new U[len];
		this.desc = desc;
	}

	override byte* ptr() {
		return cast(byte*)cast(void*)data.ptr;
	}

	override int itemLen() { return U.sizeof; }
	override int length() { return ilen(data); }

	U get(int index) { return data[index]; }
	void set(int index, U value) { data[index] = value; }

	U opIndex(int i) { return data[i]; }
	void opIndexAssign(U v, int i) { data[i] = v; }
}

class JA_I : JA_Template!(int) {
	this(int len) { super(len, "[I"); }

	static public JA_I T(int[] ints) {
		int len = ilen(ints);
		auto o = new JA_I(len);
		o.data[0..len] = ints[0..len];
		return o;
	}
}

class JA_Z : JA_Template!(bool) {
	this(int len) { super(len, "[Z"); }
}
class JA_B : JA_Template!(byte) {
	this(int len) { super(len, "[B"); }
}
class JA_S : JA_Template!(short) {
	this(int len) { super(len, "[S"); }
}
class JA_C : JA_Template!(wchar) {
	this(int len) { super(len, "[C"); }
}
class JA_J : JA_Template!(long) {
	this(int len) { super(len, "[J"); }
}
class JA_F : JA_Template!(float) {
	this(int len) { super(len, "[F"); }
}
class JA_D : JA_Template!(double) {
	this(int len) { super(len, "[D"); }
}

class JA_L : JA_Template!({% CLASS java.lang.Object %}) {
	this(int len, wstring desc) { super(len, desc); }

	static JA_L createMultiSure(int[] sizes, wstring desc) {
		if (!desc.startsWith('[')) return null;
		if (sizes.length == 1) return cast(JA_L)(JA_L.create(sizes[0], desc));
		auto o = new JA_L(sizes[0], desc);
		auto sizes2 = sizes[1..$];
		auto desc2 = desc[1..$];
		for (auto n = 0; n < o.length; n++) {
			o.set(n, JA_L.createMultiSure(sizes2, desc2));
		}
		return o;
	}

	static JA_0 create(int size, wstring desc) {
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

class WrappedThrowable : Throwable {
	public {% CLASS java.lang.Object %} t;
	this({% CLASS java.lang.Object %} t) {
		super("WrappedThrowable:" ~ t.toString());
		this.t = t;
	}
}

class N {
	static immutable public int MIN_INT32 = -2147483648;

	static public {% CLASS java.lang.Class %} resolveClass(wstring name) {
		return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N.str(name));
	}

	static public {% CLASS java.lang.String %} str(wstring str) {
		int len = ilen(str);
		auto array = new JA_C(len);
		for (int n = 0; n < len; n++) array[n] = str[n];
		return {% CONSTRUCTOR java.lang.String:([CII)V %}(array, 0, len);
	}

	static public {% CLASS java.lang.String %} str(string str) { return N.str(to!wstring(str)); }
	static public {% CLASS java.lang.String %} strLitEscape(wstring str) { return N.str(str); }

	static public wstring istr({% CLASS java.lang.Object %} jstrObj) {
		auto jstr = cast({% CLASS java.lang.String %})jstrObj;
		return to!wstring(jstr.{% FIELD java.lang.String:value %}.data);
	}

	static public int z2i(bool v) { return v ? 1 : 0; }

	static public int l2i(long v) { return to!int(v); }
	static public double l2d(long v) { return to!double(v); }

	static public long i2l(int v) { return v; }
	static public long i2j(int v) { return v; }

	static public long f2j(float v) { return to!long(v); }
	static public long d2j(double v) { return to!long(v); }

	static public long ladd(long l, long r) { return l + r; }
	static public long lsub(long l, long r) { return l - r; }
	static public long lmul(long l, long r) { return l * r; }
	static public long ldiv(long l, long r) { return l / r; }
	static public long lrem(long l, long r) { return l % r; }
	static public long lxor(long l, long r) { return l ^ r; }
	static public long lor (long l, long r) { return l | r; }
	static public long land(long l, long r) { return l & r; }
	static public long lshl(long l, long r) { return l << r; }
	static public long lshr(long l, long r) { return l >> r; }
	static public long lushr(long l, long r) { return l >>> r; }
	static public int  lcmp(long l, long r) { return (l < r) ? -1 : ((l > r) ? 1 : 0); }

	static public int cmp(double a, double b) { return (a < b) ? (-1) : ((a > b) ? (+1) : 0); }
	static public int cmpl(double a, double b) { return (isNaN(a) || isNaN(a)) ? (-1) : N.cmp(a, b); }
	static public int cmpg(double a, double b) { return (isNaN(a) || isNaN(a)) ? (+1) : N.cmp(a, b); };

	static public bool   unboxBool({% CLASS java.lang.Boolean %} i) { throw new Throwable("unboxBool"); }
	static public byte   unboxByte({% CLASS java.lang.Byte %} i) { throw new Throwable("unboxByte"); }
	static public char   unboxChar({% CLASS java.lang.Character %} i) { throw new Throwable("unboxChar"); }
	static public short  unboxShort({% CLASS java.lang.Short %} i) { throw new Throwable("unboxShort"); }
	static public int    unboxInt({% CLASS java.lang.Integer %} i) { throw new Throwable("unboxInt"); }
	static public long   unboxLong({% CLASS java.lang.Long %} i) { throw new Throwable("unboxLong"); }
	static public float  unboxFloat({% CLASS java.lang.Float %} i) { throw new Throwable("unboxFloat"); }
	static public double unboxDouble({% CLASS java.lang.Double %} i) { throw new Throwable("unboxDouble"); }


	static public {% CLASS java.lang.Boolean %}   boxBool  (bool   i) { throw new Throwable("unboxBool"); }
	static public {% CLASS java.lang.Byte %}      boxByte  (byte   i) { throw new Throwable("unboxByte"); }
	static public {% CLASS java.lang.Character %} boxChar  (char   i) { throw new Throwable("unboxChar"); }
	static public {% CLASS java.lang.Short %}     boxShort (short  i) { throw new Throwable("unboxShort"); }
	static public {% CLASS java.lang.Integer %}   boxInt   (int    i) { throw new Throwable("unboxInt"); }

	static public {% CLASS java.lang.Boolean %}   boxBool  (int    i) { throw new Throwable("unboxBool"); }
	static public {% CLASS java.lang.Byte %}      boxByte  (int    i) { throw new Throwable("unboxByte"); }
	static public {% CLASS java.lang.Character %} boxChar  (int    i) { throw new Throwable("unboxChar"); }
	static public {% CLASS java.lang.Short %}     boxShort (int    i) { throw new Throwable("unboxShort"); }

	static public {% CLASS java.lang.Long %}      boxLong  (long   i) { throw new Throwable("unboxLong"); }
	static public {% CLASS java.lang.Float %}     boxFloat (float  i) { throw new Throwable("unboxFloat"); }
	static public {% CLASS java.lang.Double %}    boxDouble(double i) { throw new Throwable("unboxDouble"); }

	static public void arraycopy({% CLASS java.lang.Object %} src, int srcPos, {% CLASS java.lang.Object %} dst, int dstPos, int length) {
		JA_0.copy(cast(JA_0)src, srcPos, cast(JA_0)dst, dstPos, length);
	}

	static public JA_L strArray(wstring[] strs) {
		int len = ilen(strs);
		auto o = new JA_L(len, "[Ljava/lang/String;");
		for (int n = 0; n < len; n++) o[n] = N.str(strs[n]);
		return o;
	}
	static public JA_L strArray(string[] strs) {
		int len = ilen(strs);
		auto o = new JA_L(len, "[Ljava/lang/String;");
		for (int n = 0; n < len; n++) o[n] = N.str(strs[n]);
		return o;
	}

	static public void init() {
		//writefln("INIT FROM D!");
	}
}

T ensureNotNull(T)(T v) {
	debug {
		if (v is null) throw new WrappedThrowable({% CONSTRUCTOR java.lang.NullPointerException:()V %});
	}
	return v;
}

/* ## BODY ## */
