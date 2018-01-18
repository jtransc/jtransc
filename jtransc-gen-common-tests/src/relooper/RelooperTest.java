package relooper;

import com.jtransc.annotation.JTranscRelooper;
import com.jtransc.io.JTranscConsole;

import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class RelooperTest {
	static public void main(String[] args) {
		JTranscConsole.log("RelooperTest:");

		JTranscConsole.log(simpleIf(0, 1));
		JTranscConsole.log(simpleIf(1, 0));
		JTranscConsole.log(simpleIf(0, 0));

		JTranscConsole.log(composedIfAnd(0, 1));
		JTranscConsole.log(composedIfAnd(1, 0));
		JTranscConsole.log(composedIfAnd(0, 0));

		JTranscConsole.log(composedIfOr(0, 1));
		JTranscConsole.log(composedIfOr(1, 0));
		JTranscConsole.log(composedIfOr(0, 0));

		simpleDoWhile(0, 5);
		simpleWhile(0, 5);
		simpleFor(2, 5);
		JTranscConsole.log(Arrays.asList(split("hello world test", ' ', 2)));
		bufferEquals(ShortBuffer.allocate(1), ShortBuffer.allocate(1));

		JTranscConsole.log(demo(true, true, false));

		JTranscConsole.log(isDigit('0'));
		JTranscConsole.log(isDigit('5'));
		JTranscConsole.log(isDigit('9'));
		JTranscConsole.log(isDigit('a'));
		JTranscConsole.log(isDigit('f'));
		JTranscConsole.log(isDigit('A'));
		JTranscConsole.log(isDigit('E'));
		JTranscConsole.log(isDigit('-'));
		JTranscConsole.log(isDigit('g'));
		JTranscConsole.log(isDigit('G'));
		JTranscConsole.log(isDigit('z'));

		//JTranscConsole.log(dbCompareTo(DoubleBuffer.allocate(1), DoubleBuffer.allocate(1)));
		JTranscConsole.log(sequals("a", "a"));
		JTranscConsole.log(sequals("a", "b"));

		JTranscConsole.log(myswitch(0));
		JTranscConsole.log(myswitch(1));
		JTranscConsole.log(myswitch(2));
		JTranscConsole.log(myswitch(3));
		JTranscConsole.log(myswitch(4));

		JTranscConsole.log(switchCase(new Term(), 0, new Term()));

		floatComparisons(0f);
		floatComparisons(1f);
		floatComparisons(Float.NaN);
		floatComparisons(-Float.NaN);

		breakInsideIf(10);
	}

	@JTranscRelooper
	static public int simpleIf(int a, int b) {
		if (a < b) {
			return -1;
		} else {
			return +1;
		}
	}

	@JTranscRelooper
	static public int composedIfAnd(int a, int b) {
		if (a < b && a >= 0) {
			return -1;
		} else {
			return +1;
		}
	}

	@JTranscRelooper
	static public int composedIfOr(int a, int b) {
		if (a < b || a >= 0) {
			return -1;
		} else {
			return +1;
		}
	}

	@JTranscRelooper
	static public int simpleDoWhile(int a, int b) {
		b++;

		do {
			if (a % 2 == 0) {
				do {
					JTranscConsole.log(a);
					a++;
				} while (a < b);
			}
			a++;
		} while (a < b);

		return b;
	}

	@JTranscRelooper
	static public int simpleWhile(int a, int b) {
		b++;

		while (a < b) {
			JTranscConsole.log(a);
			a++;
		}
		JTranscConsole.log(a);
		JTranscConsole.log(b);

		return b;
	}

	@JTranscRelooper
	static public int simpleFor(int a, int b) {
		for (int n = 1; n < b; n++) {
			JTranscConsole.log(a + n);
		}
		return b;
	}

	@JTranscRelooper
	static private String[] split(String str, char ch, int limit) {
		ArrayList<String> out = new ArrayList<String>();
		int n = 0;
		int start = 0;
		for (; n < str.length(); n++) {
			if (str.charAt(n) == ch) {
				out.add(str.substring(start, n));
				start = n + 1;
				if (out.size() >= limit - 1) break;
			}
		}
		if (start < str.length()) out.add(str.substring(start));
		return out.toArray(new String[out.size()]);
	}

	@JTranscRelooper
	static private boolean bufferEquals(ShortBuffer t, Object other) {
		if (!(other instanceof ShortBuffer)) {
			return false;
		}
		ShortBuffer otherBuffer = (ShortBuffer) other;

		if (t.remaining() != otherBuffer.remaining()) {
			return false;
		}

		int myPosition = t.position();
		int otherPosition = otherBuffer.position();
		boolean equalSoFar = true;
		while (equalSoFar && (myPosition < t.limit())) {
			equalSoFar = t.get(myPosition++) == otherBuffer.get(otherPosition++);
		}

		return equalSoFar;
	}

	@JTranscRelooper
	static private boolean demo(boolean a, boolean b, boolean c) {
		boolean result = true;
		while (a && b != c) {
			a = !b;
			result = ((a ^ c) == b);
		}

		return result;
	}

	//@JTranscRelooper(debug = true)
	@JTranscRelooper
	static public boolean isDigit(char c) {
		return ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
	}

	//@JTranscRelooper
	//static public int dbCompareTo(DoubleBuffer base, DoubleBuffer otherBuffer) {
	//	int compareRemaining = (base.remaining() < otherBuffer.remaining()) ? base.remaining()
	//		: otherBuffer.remaining();
	//	int thisPos = base.position();
	//	int otherPos = otherBuffer.position();
	//	double thisDouble, otherDouble;
	//	while (compareRemaining > 0) {
	//		thisDouble = base.get(thisPos);
	//		otherDouble = otherBuffer.get(otherPos);
	//		// checks for double and NaN inequality
	//		if ((thisDouble != otherDouble)
	//			&& ((thisDouble == thisDouble) || (otherDouble == otherDouble))) {
	//			return thisDouble < otherDouble ? -1 : 1;
	//		}
	//		thisPos++;
	//		otherPos++;
	//		compareRemaining--;
	//	}
	//	return base.remaining() - otherBuffer.remaining();
	//}

	@JTranscRelooper
	static private boolean sequals(String l, String r) {
		//noinspection StringEquality
		if (l == r) return true;
		if (l == null) return false;
		if (r == null) return false;
		if (l.length() != r.length()) return false;
		if (l.hashCode() != r.hashCode()) return false;
		final int len = l.length();
		for (int n = 0; n < len; n++) if (l.charAt(n) != r.charAt(n)) return false;
		return true;
	}

	@JTranscRelooper
	static private boolean myswitch(int a) {
		JTranscConsole.log("myswitch: " + a);
		switch (a) {
			case 0:
				JTranscConsole.log(0);
				break;
			case 1:
				JTranscConsole.log(1);
			case 6:
			case 2:
				JTranscConsole.log(2);
				break;
			case 3:
				return false;
		}
		JTranscConsole.log("end myswitch");
		return true;
	}

	@JTranscRelooper
	@SuppressWarnings("ConstantConditions")
	static private void floatComparisons(float v) {
		JTranscConsole.log("floatComparisons:" + v);
		JTranscConsole.log(v < Float.NaN);
		JTranscConsole.log(v > Float.NaN);
		JTranscConsole.log(v <= Float.NaN);
		JTranscConsole.log(v >= Float.NaN);
		JTranscConsole.log(v == Float.NaN);
		JTranscConsole.log(v != Float.NaN);
	}

	@JTranscRelooper
	static private int switchCase(Term target, int distance, Term theFirst) {
		int type;
		Term next;
		boolean eat;
		switch (target.type) {
			case Term.CHAR:
			case Term.BITSET:
			case Term.BITSET2:
				type = Term.FIND;
				break;
			case Term.REG:
			case Term.REG_I:
			case Term.GROUP_IN:
				type = Term.FINDREG;
				break;
			default:
				throw new IllegalArgumentException("wrong target type: " + target.type);
		}
		//target = target;
		//distance = distance;
		if (target == theFirst) {
			next = target.next;
			eat = true; //eat the next
		} else {
			next = theFirst;
			eat = false;
		}
		return type + (eat ? 1 : 0) + next.type;
	}

	static class Term {
		public int type;
		public Term next;

		static final int CHAR = 0;
		static final int BITSET = 1;
		static final int BITSET2 = 2;
		static final int REG = 6;
		static final int REG_I = 7;
		static final int FIND = 8;
		static final int FINDREG = 9;
		static final int GROUP_IN = 15;
	}

	@JTranscRelooper(debug = true)
	public static void breakInsideIf(int dst) {
		int n = 0;
		for (; n < dst; n++) {
			if (n == 4) {
				JTranscConsole.log(n);
				break;
			}
		}
		JTranscConsole.log(n);
	}

	/*
	@JTranscRelooper(debug = true)
	public static String toString(double v) {
		if (Double.isNaN(v)) return "NaN";
		if (Double.isInfinite(v)) return (v < 0) ? "-Infinity" : "Infinity";
		if (v == 0.0) {
			long l = Double.doubleToRawLongBits(v);
			if ((l >>> 63) != 0) return "-0.0";
		}
		//return JTranscStringTools.toString(d);
		String out = _toString(v);
		boolean hasSymbols = false;
		for (int n = 0; n < out.length(); n++) {
			char c = out.charAt(n);
			if (!Character.isDigit(c) && c != '-') {
				hasSymbols = true;
				break;
			}
		}
		if (out.indexOf("e+") >= 0) out = replace(out, "e+", "E");
		if (out.indexOf("e-") >= 0) out = replace(out, "e-", "E-");
		return hasSymbols ? out : (out + ".0");
	}

	public static String _toString(double v) {
		return "" + v;
	}

	public static String replace(String value, String src, String dst) {
		return value;
	}
	*/
}