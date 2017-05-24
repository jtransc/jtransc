package {
import Int64;
import flash.utils.ByteArray;
import flash.crypto.generateRandomBytes;

public class N {
	static public var MIN_INT32: int = -2147483648;
	static public var MAX_INT32: int = 2147483647;

	static public function resolveClass(name: String): {% CLASS java.lang.Class %} {
		return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N.str(name));
	}

	static public function ichar(i: int): String { return String.fromCharCode(i); }

	static public function strLitEscape(s: String): {% CLASS java.lang.String %} { return str(s); }

	static public function str(s: String): {% CLASS java.lang.String %} {
		if (s == null) {
			return null;
		} else {
			var out: {% CLASS java.lang.String %} = new {% CLASS java.lang.String %}();
			out.__initFromAs3(s);
			return out;
		}
	}

	static public function istr(s: {% CLASS java.lang.String %}): String { return (s != null) ? s._str : null; }

	static public function unboxBool(v: {% CLASS java.lang.Boolean %}): Boolean { return v{% IFIELD java.lang.Boolean:value %}; }
	static public function unboxByte(v: {% CLASS java.lang.Byte %}): int { return v{% IFIELD java.lang.Byte:value %}; }
	static public function unboxChar(v: {% CLASS java.lang.Character %}): int { return v{% IFIELD java.lang.Character:value %}; }
	static public function unboxShort(v: {% CLASS java.lang.Short %}): int { return v{% IFIELD java.lang.Short:value %}; }
	static public function unboxInt(v: {% CLASS java.lang.Integer %}): int { return v{% IFIELD java.lang.Integer:value %}; }
	static public function unboxLong(v: {% CLASS java.lang.Long %}): Int64 { return v{% IFIELD java.lang.Long:value %}; }
	static public function unboxFloat(v: {% CLASS java.lang.Float %}): Number { return v{% IFIELD java.lang.Float:value %}; }
	static public function unboxDouble(v: {% CLASS java.lang.Double %}): Number { return v{% IFIELD java.lang.Double:value %}; }

	static public function boxBool(v: Boolean): {% CLASS java.lang.Boolean %} { return {% SMETHOD java.lang.Boolean:valueOf:(Z)Ljava/lang/Boolean;  %}(v); }
	static public function boxByte(v: int): {% CLASS java.lang.Byte %}        { return {% SMETHOD java.lang.Byte:valueOf:(B)Ljava/lang/Byte;  %}(v); }
	static public function boxChar(v: int): {% CLASS java.lang.Character %}   { return {% SMETHOD java.lang.Character:valueOf:(C)Ljava/lang/Character;  %}(v); }
	static public function boxShort(v: int): {% CLASS java.lang.Short %}      { return {% SMETHOD java.lang.Short:valueOf:(S)Ljava/lang/Short;  %}(v); }
	static public function boxInt(v: int): {% CLASS java.lang.Integer %}      { return {% SMETHOD java.lang.Integer:valueOf:(I)Ljava/lang/Integer;  %}(v); }
	static public function boxLong(v: Int64): {% CLASS java.lang.Long %}       { return {% SMETHOD java.lang.Long:valueOf:(J)Ljava/lang/Long;  %}(v); }
	static public function boxFloat(v: Number): {% CLASS java.lang.Float %}   { return {% SMETHOD java.lang.Float:valueOf:(F)Ljava/lang/Float;  %}(v); }
	static public function boxDouble(v: Number): {% CLASS java.lang.Double %} { return {% SMETHOD java.lang.Double:valueOf:(D)Ljava/lang/Double;  %}(v); }

	static public function z2i(v: Boolean): int { return v ? 1 : 0; }
	static public function j2i(v: Int64): int { return v.low; }
	static public function i2j(v: int): Int64 { return Int64.ofInt(v); }

	static public function f2j(v: Number): Int64 { return Int64.ofFloat(v); }
	static public function d2j(v: Number): Int64 { return Int64.ofFloat(v); }

	static public function irem(l: int, r: int): int { return l % r; }
	static public function idiv(l: int, r: int): int { return l / r; }

	static public function lnew(high: int, low: int): Int64 { return Int64.make(high, low); }
	static public function lnewFloat(v: Number): Int64 { return Int64.ofFloat(v); }


	static public function lneg(l: Int64): Int64 { return Int64.neg(l); }

	static public function cmp(a: Number, b: Number): int { return (a < b) ? -1 : ((a > b) ? 1 : 0); }
	static public function cmpl(a: Number, b: Number): int { return (isNaN(a) || isNaN(b)) ? -1 : N.cmp(a, b); }
	static public function cmpg(a: Number, b: Number): int { return (isNaN(a) || isNaN(b)) ? 1 : N.cmp(a, b); }

	static public function lcmp(l: Int64, r: Int64): int { return Int64.compare(l, r); }

	static public function lxor(l: Int64, r: Int64): Int64 { return Int64.xor(l, r); }
	static public function land(l: Int64, r: Int64): Int64 { return Int64.and(l, r); }
	static public function lor(l: Int64, r: Int64): Int64 { return Int64.or(l, r); }

	static public function ladd(l: Int64, r: Int64): Int64 { return Int64.add(l, r); }
	static public function lsub(l: Int64, r: Int64): Int64 { return Int64.sub(l, r); }
	static public function lmul(l: Int64, r: Int64): Int64 { return Int64.mul(l, r); }
	static public function ldiv(l: Int64, r: Int64): Int64 { return Int64.div(l, r); }
	static public function lrem(l: Int64, r: Int64): Int64 { return Int64.rem(l, r); }

	static public function lushr(l: Int64, r: int): Int64 { return Int64.ushr(l, r); }
	static public function lshr(l: Int64, r: int): Int64 { return Int64.shr(l, r); }
	static public function lshl(l: Int64, r: int): Int64 { return Int64.shl(l, r); }

	//static public function iushr(l: int, r: int): int { return l >>> r; }


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

		var overlapping: Boolean = src == dest && (destPos > srcPos);

		(src as JA_0).arraycopy(srcPos, dest as JA_0, destPos, length, overlapping);
	}

	static private var tempArray: ByteArray = new ByteArray();

	{
		tempArray.length = 4096;
	}

	static public function floatToIntBits(v: Number): int {
		tempArray.position = 0;
		tempArray.writeFloat(v);
		tempArray.position = 0;
		return tempArray.readInt();
	}

	static public function intBitsToFloat(v: int): Number {
		tempArray.position = 0;
		tempArray.writeInt(v);
		tempArray.position = 0;
		return tempArray.readFloat();
	}

	static public function doubleToLongBits(v: Number): Int64 {
		tempArray.position = 0;
		tempArray.writeDouble(v);
		tempArray.position = 0;
		var high: int = tempArray.readInt();
		var low: int = tempArray.readInt();
		return Int64.make(high, low);
	}

	static public function longBitsToDouble(v: Int64): Number {
		tempArray.position = 0;
		tempArray.writeInt(v.high);
		tempArray.writeInt(v.low);
		tempArray.position = 0;
		return tempArray.readDouble();
	}

	static public function lhigh(v: Int64): int { return v.high; }
	static public function llow(v: Int64): int { return v.low; }

	static public function ineg(v: int): int {
		//return int(~uint(v)) + 1;
		return -v;
	}

	static public function fillSecureRandomBytes(data: ByteArray): void {
		var out: ByteArray = flash.crypto.generateRandomBytes(data.length);
		data.position = 0;
		data.writeBytes(out);
	}

	static public var NaN: Number = longBitsToDouble(Int64.make(0x7FF80000, 0x00000000));
	static public function CHECK_CAST(a: *, clazz: *): * {
		if (a == null) return null;
		var out: * = a as clazz;
		if (out == null) {
			throw new WrappedThrowable({% CONSTRUCTOR java.lang.ClassCastException:(Ljava/lang/String;)V %}(N.str("Class cast error")));
		}
		return out;
	}
}
}