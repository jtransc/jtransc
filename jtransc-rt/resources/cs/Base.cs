// JTransc {{ JTRANSC_VERSION }} : https://github.com/jtransc/jtransc

using System;

#pragma warning disable 108, 109, 162, 219, 414, 675

class N {
	//public static readonly double DoubleNaN = 0.0d / 0.0;
	public static readonly float FloatNaN = intBitsToFloat(0x7FF80000);
	public static readonly double DoubleNaN = longBitsToDouble(0x7FF8000000000000);

	static public {% CLASS com.jtransc.JTranscWrapped %} wrap(object item) {
		if (item == null) return null;
		return {% CLASS com.jtransc.JTranscWrapped %}.wrap(item);
	}

	static public object unwrap({% CLASS java.lang.Object %} item) {
		if (item == null) return null;
		return {% CLASS com.jtransc.JTranscWrapped %}.unwrap(({% CLASS com.jtransc.JTranscWrapped %})item);
	}

	static public int iushr(int l, int r) {
		return (int)(((uint)l) >> r);
	}

	//static public int MIN_INT32 = Int32.MinValue;
	static readonly public int MIN_INT32 = unchecked((int)0x80000000);
	static readonly public int MAX_INT32 = unchecked((int)0x7FFFFFFF);
	static readonly public long MIN_INT64 = unchecked((long)0x8000000000000000L);
	static readonly public long MAX_INT64 = unchecked((long)0x7FFFFFFFFFFFFFFFL);

	static public void init() {
		//Console.WriteLine(Console.OutputEncoding.CodePage);
	}

	static public JA_L getStackTrace(int skip) {
		var st = new System.Diagnostics.StackTrace();

		var o = new JA_L(st.FrameCount, "[Ljava/lang/StackTraceElement;");

		for (int n = 0; n < st.FrameCount; n++) {
			var f = st.GetFrame(n);
			var clazz = (f != null) ? ("" + f.GetMethod().DeclaringType) : "DummyClass";
			var method = (f != null) ? ("" + f.GetMethod().Name) : "dummyMethod";
			var file = (f != null) ? ("" + f.GetFileName()) : "Dummy.java";
			var lineNumber = (f != null) ? f.GetFileLineNumber() : 0;
			o[n] = {% CONSTRUCTOR java.lang.StackTraceElement:(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V %}(
				N.str(clazz), N.str(method), N.str(file), lineNumber
			);
		}

		return o;
	}

	static public bool   unboxBool  ({% CLASS java.lang.Boolean %}   i) { return i.{% METHOD java.lang.Boolean:booleanValue %}(); }
	static public sbyte  unboxByte  ({% CLASS java.lang.Byte %}      i) { return i.{% METHOD java.lang.Byte:byteValue %}(); }
	static public short  unboxShort ({% CLASS java.lang.Short %}     i) { return i.{% METHOD java.lang.Short:shortValue %}(); }
	static public ushort unboxChar  ({% CLASS java.lang.Character %} i) { return i.{% METHOD java.lang.Character:charValue %}(); }
	static public int    unboxInt   ({% CLASS java.lang.Integer %}   i) { return i.{% METHOD java.lang.Integer:intValue %}(); }
	static public long   unboxLong  ({% CLASS java.lang.Long %}      i) { return i.{% METHOD java.lang.Long:longValue %}(); }
	static public float  unboxFloat ({% CLASS java.lang.Float %}     i) { return i.{% METHOD java.lang.Float:floatValue %}(); }
	static public double unboxDouble({% CLASS java.lang.Double %}    i) { return i.{% METHOD java.lang.Double:doubleValue %}(); }

	static public {% CLASS java.lang.Object %}    boxVoid  (        ) { return null; }
	static public {% CLASS java.lang.Boolean %}   boxBool  (bool   v) { return {% SMETHOD java.lang.Boolean:valueOf:(Z)Ljava/lang/Boolean; %}(v); }
	static public {% CLASS java.lang.Byte %}      boxByte  (sbyte  v) { return {% SMETHOD java.lang.Byte:valueOf:(B)Ljava/lang/Byte; %}(v); }
	static public {% CLASS java.lang.Short %}     boxShort (short  v) { return {% SMETHOD java.lang.Short:valueOf:(S)Ljava/lang/Short; %}(v); }
	static public {% CLASS java.lang.Character %} boxChar  (ushort v) { return {% SMETHOD java.lang.Character:valueOf:(C)Ljava/lang/Character; %}(v); }
	static public {% CLASS java.lang.Integer %}   boxInt   (int    v) { return {% SMETHOD java.lang.Integer:valueOf:(I)Ljava/lang/Integer; %}(v); }
	static public {% CLASS java.lang.Long %}      boxLong  (long   v) { return {% SMETHOD java.lang.Long:valueOf:(J)Ljava/lang/Long; %}(v); }
	static public {% CLASS java.lang.Float %}     boxFloat (float  v) { return {% SMETHOD java.lang.Float:valueOf:(F)Ljava/lang/Float; %}(v); }
	static public {% CLASS java.lang.Double %}    boxDouble(double v) { return {% SMETHOD java.lang.Double:valueOf:(D)Ljava/lang/Double; %}(v); }

	static public int z2i(bool v) { return v ? 1 : 0; }
	static public int l2i(long v) { return (int)v; }
	static public long d2j(double v) { return (long)v; }
	static public long i2j(int v) { return (long)v; }
	static public long f2j(float v) { return (long)v; }

	static public long ladd (long l, long r) { return l + r; }
	static public long lsub (long l, long r) { return l - r; }
	static public long lmul (long l, long r) { return l * r; }
	static public long lxor (long l, long r) { return l ^ r; }
	static public long lor  (long l, long r) { return l | r; }
	static public long land (long l, long r) { return l & r; }
	static public long lshl (long l, long r) { return l << (int)r; }
	static public long lshr (long l, long r) { return l >> (int)r; }
	static public long lushr(long l, long r) { return (long)((ulong)l >> (int)r); }
	static public int  lcmp (long l, long r) { return (l < r) ? -1 : ((l > r) ? 1 : 0); }

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

	static public int cmp (double a, double b) { return (a < b) ? (-1) : ((a > b) ? (+1) : 0); }
	static public int cmpl(double a, double b) { return (Double.IsNaN(a) || Double.IsNaN(b)) ? (-1) : N.cmp(a, b); }
	static public int cmpg(double a, double b) { return (Double.IsNaN(a) || Double.IsNaN(b)) ? (+1) : N.cmp(a, b); }

	static public void monitorEnter({% CLASS java.lang.Object %} obj) { System.Threading.Monitor.Enter(obj); }
	static public void monitorExit({% CLASS java.lang.Object %} obj) { System.Threading.Monitor.Exit(obj); }

	static public void arraycopy({% CLASS java.lang.Object %} src, int srcPos, {% CLASS java.lang.Object %} dest, int destPos, int length) {
		_arraycopy(src, srcPos, dest, destPos, length);
	}

	static private int _arraycopy({% CLASS java.lang.Object %} src, int srcPos, {% CLASS java.lang.Object %} dest, int destPos, int length) {
		if (src is JA_0) return ((JA_0)src).copyTo(((JA_0)dest), srcPos, destPos, length);
		throw new Exception("Not implemented arraycopy for " + src);
	}

	static public JA_L strArray(string[] strs) {
		int len = strs.Length;
		JA_L o = new JA_L(len, "[Ljava/lang/String;");
		for (int n = 0; n < len; n++) o[n] = N.str(strs[n]);
		return o;
	}

	static public {% CLASS java.lang.Class %} resolveClass(string name) {
		return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N.str(name));
	}

	static public string istr({% CLASS java.lang.String %} s) {
		if (s == null) return null;
		JA_C chars = s.{% FIELD java.lang.String:value %};
		int len = chars.length;
		char[] cchars = new char[len];
		for (int n = 0; n < len; n++) cchars[n] = (char)chars[n];
		return new string(cchars);
	}

	static public {% CLASS java.lang.String %} str(string s) {
		if (s == null) return null;
		char[] c = s.ToCharArray();
		int len = c.Length;
		ushort[] shorts = new ushort[len];
		for (int n = 0; n < len; n++) shorts[n] = (ushort)c[n];
		return {% CONSTRUCTOR java.lang.String:([C)V %}(new JA_C(shorts));
	}

	static public {% CLASS java.lang.String %} strLitEscape(string s) { return str(s); }

#if UNSAFE
	static public unsafe double longBitsToDouble(long v) { return unchecked(*((double*)&v)); }
	static public unsafe long doubleToLongBits(double v) { return unchecked(*((long*)&v)); }
	static public unsafe float intBitsToFloat(int v) { return unchecked(*((float*)&v)); }
	static public unsafe int floatToIntBits(float v) { return unchecked(*((int*)&v)); }
#else
	//static public double longBitsToDouble(long v) { return BitConverter.Int64BitsToDouble(v); } // Requires FW >= 4.0
	//static public long doubleToLongBits(double v) { return BitConverter.DoubleToInt64Bits(v); } // Requires FW >= 4.0
	static public double longBitsToDouble(long v) { return BitConverter.ToDouble(BitConverter.GetBytes(v), 0); } // Compatible with FW 2.0
	static public long doubleToLongBits(double v) { return BitConverter.ToInt64(BitConverter.GetBytes(v), 0); } // Compatible with FW 2.0
	static public float intBitsToFloat(int v) { return BitConverter.ToSingle(BitConverter.GetBytes(v), 0); }
	static public int floatToIntBits(float v) { return BitConverter.ToInt32(BitConverter.GetBytes(v), 0); }
#endif

	static public double getTime() { return (double)CurrentTimeMillis(); }
	private static readonly DateTime Jan1st1970 = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
    public static long CurrentTimeMillis() { return (long) (DateTime.UtcNow - Jan1st1970).TotalMilliseconds; }

}

class JA_0 : {% CLASS java.lang.Object %} {
	public readonly int length;
	public readonly string desc;
	public JA_0(int length, string desc) {
		this.length = length;
		this.desc = desc;
	}
	virtual public int copyTo(JA_0 target, int src, int dst, int len) {
		return 0;
	}
}

class JA_Template<T> : JA_0 {
	public T[] data;

	public JA_Template(T[] data, string desc) : base(data.Length, desc) {
		this.data = data;
	}

	public JA_Template(int size, string desc) : base(size, desc) {
		this.data = new T[size];
	}

	override public int copyTo(JA_0 target, int src, int dst, int len) {
		Array.Copy(this.data, src, ((JA_Template<T>)target).data, dst, len);
		return len;
	}

	public void setArraySlice(int index, T[] data) {
		Array.Copy(data, 0, this.data, index, data.Length);
	}

	public T this[int i] { get { return data[i]; } set { data[i] = value; } }
}

class JA_B : JA_Template<sbyte>  {
	public JA_B(sbyte[] data, string desc = "[B") : base(data, desc) { }
	public JA_B(int size, string desc = "[B") : base(size, desc) { }
	public byte[] u() { return (byte[])(Array)this.data; }
}
class JA_C : JA_Template<ushort> { public JA_C(ushort[] data, string desc = "[C") : base(data, desc) { } public JA_C(int size, string desc = "[C") : base(size, desc) { } }
class JA_S : JA_Template<short>  { public JA_S(short[]  data, string desc = "[S") : base(data, desc) { } public JA_S(int size, string desc = "[S") : base(size, desc) { } }
class JA_I : JA_Template<int>    { public JA_I(int[]    data, string desc = "[I") : base(data, desc) { } public JA_I(int size, string desc = "[I") : base(size, desc) { } static public JA_I T(int[] data) { return new JA_I(data); } }
class JA_J : JA_Template<long>   { public JA_J(long[]   data, string desc = "[J") : base(data, desc) { } public JA_J(int size, string desc = "[J") : base(size, desc) { } }
class JA_F : JA_Template<float>  { public JA_F(float[]  data, string desc = "[F") : base(data, desc) { } public JA_F(int size, string desc = "[F") : base(size, desc) { } }
class JA_D : JA_Template<double> { public JA_D(double[] data, string desc = "[D") : base(data, desc) { } public JA_D(int size, string desc = "[D") : base(size, desc) { } }
class JA_L : JA_Template<{% CLASS java.lang.Object %}> {
	public JA_L({% CLASS java.lang.Object %}[] data, string desc) : base(data, desc) { } public JA_L(int size, string desc) : base(size, desc) { }
	static public JA_0 createMultiSure(string desc, params int[] sizes) {
		return _createMultiSure(desc, 0, sizes);
	}

	static public JA_0 _createMultiSure(string desc, int index, int[] sizes) {
		if (!desc.StartsWith("[")) return null;
		if (index >= sizes.Length - 1) return JA_L.create(sizes[index], desc);
		int len = sizes[index];
		JA_L o = new JA_L(len, desc);
		string desc2 = desc.Substring(1);
		for (int n = 0; n < len; n++) {
			o[n] = JA_L._createMultiSure(desc2, index + 1, sizes);
		}
		return o;
	}

	static JA_0 create(int size, string desc) {
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

class JA_Z : JA_B  { public JA_Z(sbyte[] data, string desc = "[Z") : base(data, desc) { } public JA_Z(int size, string desc = "[Z") : base(size, desc) { } }

class WrappedThrowable : Exception {
	public {% CLASS java.lang.Throwable %} t;

	public WrappedThrowable({% CLASS java.lang.Throwable %} t) : base() {
		this.t = ({% CLASS java.lang.Throwable %})t;
	}

	public WrappedThrowable({% CLASS java.lang.Object %} t) : base() {
		this.t = ({% CLASS java.lang.Throwable %})t;
	}
}

/* ## BODY ## */
