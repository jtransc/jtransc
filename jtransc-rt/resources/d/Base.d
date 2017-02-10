// JTransc {{ JTRANSC_VERSION }} : https://github.com/jtransc/jtransc

import std.conv;
import std.process;
import std.file;
import std.format;
import std.algorithm;
import std.stdio;
import std.datetime;
import std.math;
import std.random;
import std.system;
import core.stdc.string;
import core.stdc.stdlib;
//import core.stdc.stdio;
import core.thread;
import core.time;
import core.sync.mutex;
import core.atomic;

int slen(string s) { return cast(int)s.length; }
int slen(wstring s) { return cast(int)s.length; }
int alen(T)(T[] s) { return cast(int)s.length; }

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
	int bytesLength() { return length / itemLen; }
	public JA_B toByteArray  () { return new JA_B((cast(byte   *)ptr)[0..bytesLength / byte.sizeof]); }
	public JA_C toCharArray  () { return new JA_C((cast(wchar  *)ptr)[0..bytesLength / wchar.sizeof]); }
	public JA_S toShortArray () { return new JA_S((cast(short  *)ptr)[0..bytesLength / short.sizeof]); }
	public JA_I toIntArray   () { return new JA_I((cast(int    *)ptr)[0..bytesLength / int.sizeof]); }
	public JA_J toLongArray  () { return new JA_J((cast(long   *)ptr)[0..bytesLength / long.sizeof]); }
	public JA_F toFloatArray () { return new JA_F((cast(float  *)ptr)[0..bytesLength / float.sizeof]); }
	public JA_D toDoubleArray() { return new JA_D((cast(double *)ptr)[0..bytesLength / double.sizeof]); }
}

class JA_Template(U) : JA_0 {
	U[] data;
	wstring desc;

	this(int len, wstring desc) {
		this(new U[len], desc);
	}

	this(U[] data, wstring desc) {
		super(desc);
		this.data = data;
		this.desc = desc;
	}

	override byte* ptr() {
		return cast(byte*)cast(void*)data.ptr;
	}

	override int itemLen() { return U.sizeof; }
	override int length() { return alen(data); }

	U get(int index) { return data[index]; }
	void set(int index, U value) { data[index] = value; }

	U opIndex(int i) { return data[i]; }
	void opIndexAssign(U v, int i) { data[i] = v; }
}

class JA_I : JA_Template!(int) {
	this(int len) { super(len, "[I"); }
	this(int[] data) { super(data, "[I"); }

	static public JA_I T(int[] ints) {
		int len = alen(ints);
		auto o = new JA_I(len);
		o.data[0..len] = ints[0..len];
		return o;
	}
}

class JA_B : JA_Template!(byte) {
	this(int len, wstring desc = "[B") { super(len, desc); }
	this(byte[] data, wstring desc = "[B") { super(data, desc); }
}
class JA_Z : JA_B {
	this(int len) { super(len, "[Z"); }
	this(byte[] data) { super(data, "[Z"); }
}
class JA_S : JA_Template!(short) {
	this(int len) { super(len, "[S"); }
	this(short[] data) { super(data, "[S"); }
}
class JA_C : JA_Template!(wchar) {
	this(int len) {
		super(len, "[C");
		this.data[0..$] = 0;
	}
	this(wchar[] data) { super(data, "[C"); }
}
class JA_J : JA_Template!(long) {
	this(int len) { super(len, "[J"); }
	this(long[] data) { super(data, "[J"); }
}
class JA_F : JA_Template!(float) {
	this(int len) {
		super(len, "[F");
		this.data[0..$] = 0f;
	}
	this(float[] data) { super(data, "[F"); }
}
class JA_D : JA_Template!(double) {
	this(int len) {
		super(len, "[D");
		this.data[0..$] = 0.0;
	}
	this(double[] data) { super(data, "[D"); }
}

class JA_L : JA_Template!({% CLASS java.lang.Object %}) {
	this(int len, wstring desc) { super(len, desc); }
	this({% CLASS java.lang.Object %}[] data, wstring desc) { super(data, desc); }

	static JA_0 createMultiSure(int[] sizes, wstring desc) {
		if (!desc.startsWith('[')) return null;
		if (sizes.length == 1) return JA_L.create(sizes[0], desc);
		int len = sizes[0];
		auto o = new JA_L(len, desc);
		auto sizes2 = sizes[1..$];
		auto desc2 = desc[1..$];
		for (auto n = 0; n < len; n++) {
			o[n] = JA_L.createMultiSure(sizes2, desc2);
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
	//static immutable public int MIN_INT32 = -2147483648;
	static immutable public int MIN_INT32 = 0x80000000;
	static immutable public int MAX_INT32 = 0x7FFFFFFF;
	static immutable public long MIN_INT64 = 0x8000000000000000L;
	static immutable public long MAX_INT64 = 0x7FFFFFFFFFFFFFFFL;

	static public {% CLASS java.lang.Class %} resolveClass(wstring name) {
		return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N.str(name));
	}

	static public {% CLASS java.lang.String %} str(wstring str) {
		if (str is null) return null;
		int len = slen(str);
		auto array = new JA_C(len);
		array.data[0..$] = str[0..$];
		//array.data = str[0..$];
		return {% CONSTRUCTOR java.lang.String:([CII)V %}(array, 0, len);
	}

	static public {% CLASS java.lang.String %} str(string str) {
		if (str is null) return null;
		return N.str(to!wstring(str));
	}

	static public {% CLASS java.lang.String %} strLitEscape(wstring str) {
		if (str is null) return null;
		int len = slen(str);
		auto array = new JA_C(len);
		array.data[0..$] = str[0..$];
		return {% CONSTRUCTOR java.lang.String:([CZ)V %}(array, false);
	}

	static public wstring istr({% CLASS java.lang.Object %} jstrObj) {
		if (jstrObj is null) return null;
		auto jstr = cast({% CLASS java.lang.String %})jstrObj;
		return to!wstring(jstr.{% FIELD java.lang.String:value %}.data);
	}

	static public string istr2({% CLASS java.lang.Object %} jstrObj) {
		return to!string(N.istr(jstrObj));
	}

	static public int z2i(bool v) { return v ? 1 : 0; }

	static public int l2i(long v) { return cast(int)(v); }
	static public double l2d(long v) { return cast(double)(v); }

	static public long i2l(int v) { return v; }
	static public long i2j(int v) { return v; }

	static public long f2j(float v) { return cast(long)(v); }
	static public long d2j(double v) { return cast(long)(v); }

	static public long ladd(long l, long r) { return l + r; }
	static public long lsub(long l, long r) { return l - r; }
	static public long lmul(long l, long r) { return l * r; }
	static public long lxor(long l, long r) { return l ^ r; }
	static public long lor (long l, long r) { return l | r; }
	static public long land(long l, long r) { return l & r; }
	static public long lshl(long l, long r) { return l << r; }
	static public long lshr(long l, long r) { return l >> r; }
	static public long lushr(long l, long r) { return l >>> r; }
	static public int  lcmp(long l, long r) { return (l < r) ? -1 : ((l > r) ? 1 : 0); }

	static public int cmp(double a, double b) { return (a < b) ? (-1) : ((a > b) ? (+1) : 0); }
	static public int cmpl(double a, double b) { return (isNaN(a) || isNaN(b)) ? (-1) : N.cmp(a, b); }
	static public int cmpg(double a, double b) { return (isNaN(a) || isNaN(b)) ? (+1) : N.cmp(a, b); }

	static public bool   unboxBool  ({% CLASS java.lang.Boolean %}   i) { return i.{% METHOD java.lang.Boolean:booleanValue %}(); }
	static public byte   unboxByte  ({% CLASS java.lang.Byte %}      i) { return i.{% METHOD java.lang.Byte:byteValue %}(); }
	static public short  unboxShort ({% CLASS java.lang.Short %}     i) { return i.{% METHOD java.lang.Short:shortValue %}(); }
	static public wchar  unboxChar  ({% CLASS java.lang.Character %} i) { return i.{% METHOD java.lang.Character:charValue %}(); }
	static public int    unboxInt   ({% CLASS java.lang.Integer %}   i) { return i.{% METHOD java.lang.Integer:intValue %}(); }
	static public long   unboxLong  ({% CLASS java.lang.Long %}      i) { return i.{% METHOD java.lang.Long:longValue %}(); }
	static public float  unboxFloat ({% CLASS java.lang.Float %}     i) { return i.{% METHOD java.lang.Float:floatValue %}(); }
	static public double unboxDouble({% CLASS java.lang.Double %}    i) { return i.{% METHOD java.lang.Double:doubleValue %}(); }


	static public {% CLASS java.lang.Object %}    boxVoid  (        ) { return null; }
	static public {% CLASS java.lang.Boolean %}   boxBool  (bool   v) { return {% SMETHOD java.lang.Boolean:valueOf:(Z)Ljava/lang/Boolean; %}(v); }
	static public {% CLASS java.lang.Byte %}      boxByte  (byte   v) { return {% SMETHOD java.lang.Byte:valueOf:(B)Ljava/lang/Byte; %}(v); }
	static public {% CLASS java.lang.Short %}     boxShort (short  v) { return {% SMETHOD java.lang.Short:valueOf:(S)Ljava/lang/Short; %}(v); }
	static public {% CLASS java.lang.Character %} boxChar  (wchar  v) { return {% SMETHOD java.lang.Character:valueOf:(C)Ljava/lang/Character; %}(v); }
	static public {% CLASS java.lang.Integer %}   boxInt   (int    v) { return {% SMETHOD java.lang.Integer:valueOf:(I)Ljava/lang/Integer; %}(v); }
	static public {% CLASS java.lang.Long %}      boxLong  (long   v) { return {% SMETHOD java.lang.Long:valueOf:(J)Ljava/lang/Long; %}(v); }
	static public {% CLASS java.lang.Float %}     boxFloat (float  v) { return {% SMETHOD java.lang.Float:valueOf:(F)Ljava/lang/Float; %}(v); }
	static public {% CLASS java.lang.Double %}    boxDouble(double v) { return {% SMETHOD java.lang.Double:valueOf:(D)Ljava/lang/Double; %}(v); }

	static public void arraycopy({% CLASS java.lang.Object %} src, int srcPos, {% CLASS java.lang.Object %} dst, int dstPos, int length) {
		JA_0.copy(cast(JA_0)src, srcPos, cast(JA_0)dst, dstPos, length);
	}

	static public wstring[] istrArray(JA_L arrays) {
		wstring[] o;
		for (int n = 0; n < arrays.length; n++) o ~= N.istr(arrays[n]);
		return o;
	}

	static public string[] istrArray2(JA_L arrays) {
		string[] o;
		for (int n = 0; n < arrays.length; n++) o ~= N.istr2(arrays[n]);
		return o;
	}

	static public JA_L strArray(wstring[] strs) {
		int len = alen(strs);
		auto o = new JA_L(len, "[Ljava/lang/String;");
		for (int n = 0; n < len; n++) o[n] = N.str(strs[n]);
		return o;
	}
	static public JA_L strArray(string[] strs) {
		int len = alen(strs);
		auto o = new JA_L(len, "[Ljava/lang/String;");
		for (int n = 0; n < len; n++) o[n] = N.str(strs[n]);
		return o;
	}

	static public int idiv(int a, int b) {
    	if (a == 0) return 0;
    	if (b == 0) return 0;
    	if (a == N.MIN_INT32 && b == -1) return N.MIN_INT32;
    	return a / b;
    }

    static public int irem(int a, int b) {
    	if (a == 0) return 0;
    	if (b == 0) return 0;
    	if (a == N.MIN_INT32 && b == -1) return 0;
    	return a % b;
    }

    static public long ldiv(long a, long b) {
    	if (a == 0) return 0;
    	if (b == 0) return 0;
    	if (a == N.MIN_INT64 && b == -1) return N.MIN_INT64;
    	return a / b;
    }
    static public long lrem(long a, long b) {
    	if (a == 0) return 0;
    	if (b == 0) return 0;
    	if (a == N.MIN_INT64 && b == -1) return 0;
    	return a % b;
    }

    static public double longBitsToDouble(long p0) {
		return *cast(double *)&p0;
    }

    //static public double longBitsToDoubleConst(long p0)() {
	//	return *(cast(double *)&p0);
    //}

	static public long nanoTime() {
		return Clock.currTime.stdTime * 100;
	}

	static public long currentTimeMillis() {
		auto start = SysTime(DateTime(1970, 1, 1)).stdTime / 10000;
		auto now = Clock.currTime.stdTime / 10000;
		return now - start;
	}

	static public string getOS() {
		switch (std.system.os) {
			case std.system.OS.win32: return "windows";
			case std.system.OS.win64: return "windows-x64";
			case std.system.OS.linux: return "linux";
			case std.system.OS.osx: return "mac";
			case std.system.OS.freeBSD: return "linux";
			case std.system.OS.netBSD: return "linux";
			case std.system.OS.solaris: return "linux";
			case std.system.OS.android: return "android";
			case std.system.OS.otherPosix: return "linux";
			default: return "unknown";
		}
	}

	__gshared core.sync.mutex.Mutex prepareMutexMutex;

	static public void init() {
		//writefln("INIT FROM D!");
		prepareMutexMutex = new core.sync.mutex.Mutex();
	}

	static public void prepareMutex({% CLASS java.lang.Object %} obj) {
		synchronized (prepareMutexMutex) {
			if (obj.__d_mutex is null) obj.__d_mutex = new core.sync.mutex.Mutex();
		}
	}

	static public void monitorEnter({% CLASS java.lang.Object %} obj) {
		prepareMutex(obj);
		obj.__d_mutex.lock();
	}
	static public void monitorExit({% CLASS java.lang.Object %} obj) {
		prepareMutex(obj);
		obj.__d_mutex.unlock();
	}
}

T ensureNotNull(T)(T v) {
	if (v is null) throw new WrappedThrowable({% CONSTRUCTOR java.lang.NullPointerException:()V %});
	return v;
}

/* ## BODY ## */
