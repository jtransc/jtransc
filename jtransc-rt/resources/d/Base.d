import std.conv;

abstract class JA_0 : {% CLASS java.lang.Object %} {
	abstract int length();
}

class JA_Template(U) : JA_0 {
	U[] data;
	wstring desc;

	this(int len, wstring desc = "") {
		this.data = new U[len];
		this.desc = desc;
	}

	override int length() { return to!int(data.length); }

	U get(int index) { return data[index]; }
	void set(int index, U value) { data[index] = value; }

	U opIndex(int i) { return data[i]; }
	void opIndexAssign(U v, int i) { data[i] = v; }
}

class JA_I : JA_Template!(int) {
	this(int len, wstring desc = "") { super(len, desc); }

	static public JA_I T(int[] ints) {
		throw new Throwable("JA_I.T not implemented");
	}
}

class JA_S : JA_Template!(short) {
	this(int len, wstring desc = "") { super(len, desc); }
	void opIndexAssign(int v, int i) { data[i] = to!short(v); }
}

class JA_C : JA_Template!(ushort) {
	this(int len, wstring desc = "") { super(len, desc); }
	void opIndexAssign(int v, int i) { data[i] = to!ushort(v); }
}

alias JA_Z = JA_Template!(bool);
alias JA_B = JA_Template!(byte);
alias JA_J = JA_Template!(long);
alias JA_F = JA_Template!(float);
alias JA_D = JA_Template!(double);
alias JA_L = JA_Template!({% CLASS java.lang.Object %});

class WrappedThrowable : Throwable {
	{% CLASS java.lang.Object %} t;
	this({% CLASS java.lang.Object %} t) {
		super("WrappedThrowable");
		this.t = t;
	}
}

class N {
	static public int MIN_INT32 = -2147483648;

	static public {% CLASS java.lang.Class %} resolveClass(wstring str) {
		throw new Throwable("resolveClass");
	}

	static public {% CLASS java.lang.String %} strLitEscape(wstring str) {
		throw new Throwable("strLitEscape");
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
}
/* ## BODY ## */