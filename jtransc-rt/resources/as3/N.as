package {
	import Long;

	public class N {
		static public var MIN_INT32: int = -2147483648;
		static public var MAX_INT32: int = 2147483647;

		static public function resolveClass(name: String): {% CLASS java.lang.Class %} {
			return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N.str(name));
		}

		static public function ichar(i: int): String { return String.fromCharCode(i); }

		static public function strLitEscape(s: String): {% CLASS java.lang.String %} { return str(s); }

		static public function str(s: String): {% CLASS java.lang.String %} {
			var out: {% CLASS java.lang.String %} = new {% CLASS java.lang.String %}();
			out.__initFromAs3(s);
			return out;
		}

		static public function istr(s: {% CLASS java.lang.String %}): String { return (s != null) ? s._str : null; }

		static public function boxBool(v: Boolean): {% CLASS java.lang.Boolean %} { throw new Error("Not implemented"); }
		static public function boxByte(v: int): {% CLASS java.lang.Byte %} { throw new Error("Not implemented"); }
		static public function boxChar(v: int): {% CLASS java.lang.Character %} { throw new Error("Not implemented"); }
		static public function boxShort(v: int): {% CLASS java.lang.Short %} { throw new Error("Not implemented"); }
		static public function boxInt(v: int): {% CLASS java.lang.Integer %} { throw new Error("Not implemented"); }
		static public function boxLong(v: Long): {% CLASS java.lang.Long %} { throw new Error("Not implemented"); }
		static public function boxFloat(v: Number): {% CLASS java.lang.Float %} { throw new Error("Not implemented"); }
		static public function boxDouble(v: Number): {% CLASS java.lang.Double %} { throw new Error("Not implemented"); }

		static public function z2i(v: Boolean): int { return v ? 1 : 0; }
		static public function l2i(v: Long): int { return v.low; }
		static public function i2j(v: int): Long { return Long.ofInt(v); }

		static public function f2j(v: Number): Long { return Long.ofFloat(v); }
		static public function d2j(v: Number): Long { return Long.ofFloat(v); }

		static public function irem(l: int, r: int): int { return l % r; }
		static public function idiv(l: int, r: int): int { return l / r; }

		static public function lnew(high: int, low: int): Long { return Long.make(high, low); }

		static public function lneg(l: Long): Long { return Long.neg(l); }

		static public function cmpl(l: Number, r: Number): int { throw new Error("Not implemented"); }
		static public function cmpg(l: Number, r: Number): int { throw new Error("Not implemented"); }

		static public function lcmp(l: Long, r: Long): int { throw new Error("Not implemented"); }

		static public function lxor(l: Long, r: Long): Long { return Long.xor(l, r); }
		static public function land(l: Long, r: Long): Long { return Long.and(l, r); }
		static public function lor(l: Long, r: Long): Long { return Long.or(l, r); }

		static public function ladd(l: Long, r: Long): Long { return Long.add(l, r); }
		static public function lsub(l: Long, r: Long): Long { return Long.sub(l, r); }
		static public function lmul(l: Long, r: Long): Long { return Long.mul(l, r); }
		static public function ldiv(l: Long, r: Long): Long { return Long.div(l, r); }
		static public function lrem(l: Long, r: Long): Long { return Long.rem(l, r); }

		static public function lushr(l: Long, r: int): Long { return Long.ushr(l, r); }
		static public function lshr(l: Long, r: int): Long { return Long.shr(l, r); }
		static public function lshl(l: Long, r: int): Long { return Long.shl(l, r); }


		static public function monitorEnter(v: *): void { }

		static public function monitorExit(v: *): void { }

		static public function charArrayToString(data: JA_C, start: int, len: int): String {
			var out: String = "";
			for (var n: int = 0; n < len; n++) out += String.fromCharCode(data.data[start + n]);
			return out;
		}

		static public function stringToCharArray(str: String): JA_C {
			var out: JA_C = new JA_C(str.length);
			for (var n: int = 0; n < str.length; n++) out.data[n] = str.charCodeAt(n);
			return out;
		}

		static public function strArray(array: Array): JA_L {
			var out: JA_L = new JA_L(array.length, '[Ljava/lang/String;');
			for (var n:int = 0; n < array.length; n++) out.data[n] = N.str(array[n]);
			return out;
		}

		static public function arraycopy(src: {% CLASS java.lang.Object %}, srcPos: int, dest: {% CLASS java.lang.Object %}, destPos: int, length: int): void {
			if (src == null) throw 'N.arraycopy src==null';
			if (dest == null) throw 'N.arraycopy dest==null';
			(src as JA_0).arraycopy(srcPos, dest as JA_0, destPos, length);
		}
	}
}