// JTransc {{ JTRANSC_VERSION }} : https://github.com/jtransc/jtransc

using System;

#pragma warning disable 108, 109, 162

class N {
	static public int MIN_INT32 = Int32.MinValue;

	static public void init() {
	}

	static public int z2i(bool v) { return v ? 1 : 0; }
	static public int l2i(long v) { return (int)v; }
	static public long d2j(double v) { return (long)v; }
	static public long i2j(int v) { return (long)v; }
	static public long f2j(float v) { return (long)v; }
	static public long lrem(long a, long b) { return a % b; }
	static public long ladd(long a, long b) { return a + b; }
	static public long ldiv(long a, long b) { return a / b; }
	static public long lxor(long a, long b) { return a ^ b; }
	static public long lushr(long a, long b) { return (long)((ulong)a >> (int)b); }
	static public int lcmp(long a, long b) {
		throw new Exception();
	}

	static public void monitorEnter({% CLASS java.lang.Object %} obj) {
	}

	static public void monitorExit({% CLASS java.lang.Object %} obj) {
	}

	static public JA_L strArray(string[] array) {
		throw new Exception();
	}

	static public {% CLASS java.lang.Class %} resolveClass(string s) {
		throw new Exception();
	}

	static public {% CLASS java.lang.String %} strLitEscape(string s) {
		throw new Exception();
	}

	static public double longBitsToDouble(long v) {
		throw new Exception();
	}

	static public float intBitsToFloat(int v) {
		throw new Exception();
	}
}

class JA_0 : {% CLASS java.lang.Object %} {
	public readonly int length;
	public JA_0(int length) {
		this.length = length;
	}
}

class JA_Template<T> : JA_0 {
	public T[] data;

	public JA_Template(T[] data) : base(data.Length) {
		this.data = data;
	}

	public JA_Template(int size) : base(size) {
		this.data = new T[size];
	}

	public T this[int i] { get { return data[i]; } set { data[i] = value; } }
}

class JA_B : JA_Template<sbyte>  { public JA_B(sbyte[] data) : base(data) { } public JA_B(int size) : base(size) { } }
class JA_Z : JA_B  { public JA_Z(sbyte[] data) : base(data) { } public JA_Z(int size) : base(size) { } }
class JA_C : JA_Template<ushort> { public JA_C(ushort[] data) : base(data) { } public JA_C(int size) : base(size) { } }
class JA_S : JA_Template<short>  { public JA_S(short[] data) : base(data) { } public JA_S(int size) : base(size) { } }
class JA_I : JA_Template<int>    {
	public JA_I(int[] data) : base(data) { } public JA_I(int size) : base(size) { }
	static public JA_I T(int[] data) {
		return new JA_I(data);
	}
}
class JA_J : JA_Template<long>   { public JA_J(long[] data) : base(data) { } public JA_J(int size) : base(size) { } }
class JA_F : JA_Template<float>  { public JA_F(float[] data) : base(data) { } public JA_F(int size) : base(size) { } }
class JA_D : JA_Template<double> { public JA_D(double[] data) : base(data) { } public JA_D(int size) : base(size) { } }
class JA_L : JA_Template<{% CLASS java.lang.Object %}> {
	string type;

	public JA_L({% CLASS java.lang.Object %}[] data, string type) : base(data) {
		this.type = type;
	}
	public JA_L(int size, string type) : base(size) {
		this.type = type;
	}
}

class WrappedThrowable : Exception {
	public {% CLASS java.lang.Object %} t;

	public WrappedThrowable({% CLASS java.lang.Object %} t) : base() {
		this.t = t;
	}
}

class JTranscBase {
	virtual public string toString() { return this.ToString(); }
}

/* ## BODY ## */
