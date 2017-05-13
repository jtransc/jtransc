package {
	import Long;

	public class N {
		static public var MIN_INT32: int = -2147483648;
		static public var MAX_INT32: int = 2147483647;

		static public function resolveClass(className: String): {% CLASS java.lang.Class %} {
			throw new Error("Not implemented: N.resolveClass");
		}

		static public function strLitEscape(s: String): {% CLASS java.lang.String %} {
			throw new Error("Not implemented: N.strLitEscape");
		}

		static public function str(s: String): {% CLASS java.lang.String %} {
			throw new Error("Not implemented: N.str");
		}

		static public function istr(s: {% CLASS java.lang.String %}): String {
			throw new Error("Not implemented: N.istr");
		}

		static public function boxBool(v: Boolean): {% CLASS java.lang.Boolean %} { throw new Error("Not implemented"); }
		static public function boxByte(v: int): {% CLASS java.lang.Byte %} { throw new Error("Not implemented"); }
		static public function boxChar(v: int): {% CLASS java.lang.Character %} { throw new Error("Not implemented"); }
		static public function boxShort(v: int): {% CLASS java.lang.Short %} { throw new Error("Not implemented"); }
		static public function boxInt(v: int): {% CLASS java.lang.Integer %} { throw new Error("Not implemented"); }
		static public function boxLong(v: Long): {% CLASS java.lang.Long %} { throw new Error("Not implemented"); }
		static public function boxFloat(v: Number): {% CLASS java.lang.Float %} { throw new Error("Not implemented"); }
		static public function boxDouble(v: Number): {% CLASS java.lang.Double %} { throw new Error("Not implemented"); }

		static public function z2i(v: Boolean): int { return v ? 1 : 0; }
		static public function l2i(v: Long): int { throw new Error("Not implemented"); }
		static public function i2j(v: int): Long { throw new Error("Not implemented"); }

		static public function f2j(v: Number): Long { throw new Error("Not implemented"); }
		static public function d2j(v: Number): Long { throw new Error("Not implemented"); }

		static public function irem(l: int, r: int): int { throw new Error("Not implemented"); }
		static public function idiv(l: int, r: int): int { throw new Error("Not implemented"); }

		static public function lnew(high: int, low: int): Long { throw new Error("Not implemented"); }

		static public function lneg(l: Long): Long { throw new Error("Not implemented"); }

		static public function cmpl(l: Number, r: Number): int { throw new Error("Not implemented"); }
		static public function cmpg(l: Number, r: Number): int { throw new Error("Not implemented"); }

		static public function lcmp(l: Long, r: Long): int { throw new Error("Not implemented"); }
		static public function ladd(l: Long, r: Long): Long { throw new Error("Not implemented"); }
		static public function lxor(l: Long, r: Long): Long { throw new Error("Not implemented"); }
		static public function ldiv(l: Long, r: Long): Long { throw new Error("Not implemented"); }
		static public function lrem(l: Long, r: Long): Long { throw new Error("Not implemented"); }
		static public function lushr(l: Long, r: int): Long { throw new Error("Not implemented"); }

		static public function monitorEnter(v: *): void { }

		static public function monitorExit(v: *): void { }
	}
}