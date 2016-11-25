abstract class JA_0 : {% CLASS java.lang.Object %} {
	abstract int length();
}

class JA_Template(T) : JA_0 {
	T[] data;

	this(int len) {
		data = new T[len];
	}

	override int length() {
		return data.length;
	}

	T get(int index) { return data[index]; }
	void set(int index, T value) { data[index] = value; }

	T opIndex(int i) { return data[i]; }
	void opIndexAssign(int i, T v) { data[i] = v; }
}

alias JA_Z = JA_Template!(bool);
alias JA_B = JA_Template!(byte);
alias JA_C = JA_Template!(ushort);
alias JA_S = JA_Template!(short);
alias JA_I = JA_Template!(int);
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
	static public {% CLASS java.lang.Class %} resolveClass(wstring str) {
		// @TODO!
		return null;
	}

	static public {% CLASS java.lang.String %} strLitEscape(wstring str) {
		// @TODO!
		return null;
	}

	static public int z2i(bool v) { return v ? 1 : 0; }
}

/* ## BODY ## */