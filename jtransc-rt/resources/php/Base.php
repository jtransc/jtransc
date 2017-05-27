<?php

// JTransc {{ JTRANSC_VERSION }} : https://github.com/jtransc/jtransc

const PHP_INT_BITS_SIZE = PHP_INT_SIZE * 8;

class N {
	const MIN_INT32 = -2147483648;
	const MAX_INT32 = 2147483647;

	static function init() {
	}

	static public function iushr($a, $b) {
	    if ($b == 0) return $a;
        return ($a >> $b) & ~(1 << (8 * PHP_INT_SIZE - 1) >> ($b - 1));
	}
	static public function irem($a, $b) { return $a % $b; }
	static public function idiv($a, $b) { return floor($a / $b); } // intdiv

	static public function i($v) { return $v | 0; }

	static public function j2i($v) { return $v->low; }
	static public function z2i($v) { return $v ? 1 : 0; }
	static public function i2c($v) { return $v & 0xFFFF; }
	static public function i2b($v) { return ($v << (PHP_INT_BITS_SIZE - 8)) >> PHP_INT_BITS_SIZE; }
	static public function d2j($v) { return Int64::ofFloat($v); }

	static function lnew($h, $l) { return Int64::make($h, $l); }
	static function lsub($l, $r) { return Int64::sub($l, $r); }
	static function ladd($l, $r) { return Int64::add($l, $r); }
	static function lmul($l, $r) { return Int64::mul($l, $r); }
	static function ldiv($l, $r) { return Int64::div($l, $r); }
	static function lcmp($l, $r) { return Int64::compare($l, $r); }
	static function lshl($l, $r) { return Int64::shl($l, $r); }
	static function lshr($l, $r) { return Int64::shr($l, $r); }
	static function lushr($l, $r) { return Int64::ushr($l, $r); }

	static function cmp ($a, $b) { return ($a < $b) ? (-1) : (($a > $b) ? (1) : 0); }
	static function cmpl($a, $b) { return (is_nan($a) || is_nan($b)) ? (-1) : N::cmp($a, $b); }
	static function cmpg($a, $b) { return (is_nan($a) || is_nan($b)) ? (1) : N::cmp($a, $b); }

	static function strLitEscape($str) {
		return N::str($str);
	}

	static function monitorEnter($v) { }
	static function monitorExit($v) { }

	static function resolveClass($name) {
		return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N::str($name));
	}

	static public function getTime() {
		//return time() * 1000 + (microtime(false) / 1000);
		return (microtime(true) / 1000000);
	}


	// @TODO: Use native strings
	// @TODO: Unicode (use utf-8?)
	static function str($str) {
		$len = strlen($str);
		$chars = new JA_C($len);
		for ($n = 0; $n < $len; $n++) $chars->set($n, ord(substr($str, $n, 1)));
		return {% CONSTRUCTOR java.lang.String:([C)V %}($chars);
	}

	static function istr($str) {
		$out = '';
		$len = $str->{% METHOD java.lang.String:length %}();
		for ($n = 0; $n < $len; $n++) $out .= chr($str->{% METHOD java.lang.String:charAt %}($n));
		return $out;
	}

	static public function strArray($array) {
		return JA_L::create(array_map(function($v) { return N::str($v); }, $array), 'Ljava/lang/String;');
	}
}


class TypedBuffer {
	public $data = null;

	public function __construct($size) { $this->data = str_repeat(chr(0), $size); }
	public function getByte($n) { return ord($this->data[$n]); }
	public function getChar($n) { return unpack("S", substr($this->data, $n, 2))[0]; }
	public function getShort($n) { return unpack("s", substr($this->data, $n, 2))[0]; }
	public function getInt($n) { return unpack("i", substr($this->data, $n, 4))[0]; }
	public function getFloat($n) { return unpack("f", substr($this->data, $n, 4))[0]; }
	public function getDouble($n) { return unpack("d", substr($this->data, $n, 8))[0]; }
}

class JA_0 extends {% CLASS java.lang.Object %} {
	public $length = 0;
	public $desc = '';
	public $data = null;

	public function __construct($length, $desc, $default = 0) {
		//echo "Allocating $desc: $length with default value '$default'\n";
		//debug_print_backtrace();
		$this->length = $length;
		$this->desc = $desc;
		$this->data = array_fill(0, $length, $default);
	}

	public function set($index, $value) { $this->data[$index] = $value; }
	public function get($index) { return $this->data[$index]; }
}

class JA_B extends JA_0 {
	public function __construct($length, $desc = '[B') { parent::__construct($length, $desc, 0); }
}

class JA_Z extends JA_B {
	public function __construct($length) { parent::__construct($length, '[Z'); }
}

class JA_C extends JA_0 {
	public function __construct($length) { parent::__construct($length, '[C', 0); }
}

class JA_S extends JA_0 {
	public function __construct($length) { parent::__construct($length, '[S', 0); }
}

class JA_I extends JA_0 {
	public function __construct($length) { parent::__construct($length, '[I', 0); }

	static public function T($array) {
		$len = count($array);
		$out = new JA_I($len);
		for ($n = 0; $n < $len; $n++) $out->set($n, $array[$n]);
		return $out;
	}
}

class JA_J extends JA_0 {
	public function __construct($length) { parent::__construct($length, '[J', 0); }
}

class JA_F extends JA_0 {
	public function __construct($length) { parent::__construct($length, '[F', 0.0); }
}

class JA_D extends JA_0 {
	public function __construct($length) { parent::__construct($length, '[D', 0.0); }
}

class JA_L extends JA_0 {
	public function __construct($length, $desc) { parent::__construct($length, $desc, null); }

	static function create($items, $desc) {
		$count = sizeof($items);
		$out = new JA_L($count, $desc);
		for ($n = 0; $n < $count; $n++) $out->set($n, $items[$n]);
		return $out;
	}
}

class WrappedThrowable extends Exception {
}


class DivModResult {
	public $quotient;
	public $modulus;

	public function __constructor($quotient, $modulus) {
		$this->quotient = $quotient;
		$this->modulus = $modulus;
	}
}

class Int32 {
	static public function compare($a, $b) {
		$a |= 0;
		$b |= 0;
		if ($a == $b) {
			return 0;
		} else if ($a > $b) {
			return 1;
		} else {
			return -1;
		}
	}

	static public function ucompare($a, $b) {
		if ($a < 0) {
			if($b < 0) {
				return ~$b - ~$a | 0;
			} else {
				return 1;
			}
		}
		if ($b < 0) {
			return -1;
		} else {
			return $a - $b | 0;
		}
	}

	static public function mul($a, $b) {
		return $a * $b;
	}
}

class Int64 {
	static public $MAX_INT64;
	static public $MIN_INT64;
	static public $zero;
	static public $one;
	static public $MIN_VALUE;
	static public $MAX_VALUE;

	public $high;
	public $low;

	public function __constructor($high, $low) {
		$this->high = $high;
		$this->low = $low;
	}

	static public function make($high, $low) {
		if ($high == 0) {
			if ($low == 0) return Int64::$zero;
			if ($low == 1) return Int64::$one;
		}
		return new Int64($high, $low);
	}

	static public function ofInt($value) {
		return Int64::make($value >> 31, $value);
	}

	static public function ofFloat($f) {
		if (is_nan($f) || !is_finite($f)) throw new Exception("Number is NaN or Infinite");
		$noFractions = $f - ($f % 1);
		// 2^53-1 and -2^53: these are parseable without loss of precision
		if ($noFractions > 9007199254740991.0) throw new Exception("Conversion overflow");
		if ($noFractions < -9007199254740991.0) throw new Exception("Conversion underflow");

		$result = Int64::ofInt(0);
		$neg = $noFractions < 0;
		$rest = $neg ? -$noFractions : $noFractions;

		$i = 0;
		while ($rest >= 1) {
			$curr = $rest % 2;
			$rest = $rest / 2;
			if ($curr >= 1) $result = Int64::add($result, Int64::shl(Int64::ofInt(1), $i));
			$i++;
		}

		return $neg ? Int64::neg($result) : $result;
	}

	static public function ofString($sParam) {
		$base = Int64::ofInt(10);
		$current = Int64::ofInt(0);
		$multiplier = Int64::ofInt(1);
		$sIsNegative = false;

		$s = trim($sParam);
		if ($s->charAt(0) == '-') {
			$sIsNegative = true;
			$s = substr($s, 1, strlen($s));
		}
		$len = strlen(s);

		for ($i = 0; $i < $len; $i++) {
			$digitInt = intval(substr($s, $len - 1 - $i, 1));

			$digit = Int64::ofInt($digitInt);
			if ($sIsNegative) {
				$current = Int64::sub($current, Int64::mul($multiplier, $digit));
				if (!Int64::isNeg($current)) throw new Exception("NumberFormatError: Underflow");
			} else {
				$current = Int64::add($current, Int64::mul($multiplier, $digit));
				if (Int64::isNeg($current)) throw new Exception("NumberFormatError: Overflow");
			}
			$multiplier = Int64::mul($multiplier, $base);
		}
		return $current;
	}

	static public function toInt($a) {
		return $a->low;
	}

	static public function toFloat($v) {
		if (Int64::isNeg($v)) {
			return Int64::eq($v, Int64::$MIN_INT64) ? -9223372036854775808.0 : -Int64::toFloat(Int64::neg($v));
		} else {
			$lowf = $v->low;
			$highf = $v->high;
			return $lowf + $highf * pow(2, 32);
		}
	}

	static public function isNeg($a) { return $a->high < 0; }
	static public function isZero($a) { return $a->high == 0 && $a->low == 0; }
	static public function isNotZero($a) { return $a->high != 0 || $a->low != 0; }

// Comparisons

	static private function Integer_compare($a, $b) { return Int32::compare($a, $b); }
	static private function Integer_compareUnsigned($a, $b): int { return Int32::ucompare($a, $b); }

	static public function compare($a, $b) {
		$v = $a->high - $b->high;
		if ($v == 0) $v = Int64::Integer_compareUnsigned($a->low, $b->low);
		return ($a->high < 0) ? (($b->high < 0) ? $v : -1) : (($b->high >= 0) ? $v : 1);
	}

	static public function ucompare($a, $b) {
		$v = Int64::Integer_compareUnsigned($a->high, $b->high);
		return ($v != 0) ? $v : Int64::Integer_compareUnsigned($a->low, $b->low);
	}

	static public function eq($a, $b) { return ($a->high == $b->high) && ($a->low == $b->low); }
	static public function ne($a, $b) { return ($a->high != $b->high) || ($a->low != $b->low); }
	static public function lt($a, $b) { return Int64::compare($a, $b) < 0; }
	static public function le($a, $b) { return Int64::compare($a, $b) <= 0; }
	static public function gt($a, $b) { return Int64::compare($a, $b) > 0; }
	static public function ge($a, $b) { return Int64::compare($a, $b) >= 0; }

	// Strings
	public function toString(): String {
		$i = $this;
		if (Int64::isZero($i)) return "0";
		$str = "";
		$neg = false;
		if (Int64::isNeg($i)) {
			$neg = true;
			// i = -i; cannot negate here as --9223372036854775808 = -9223372036854775808
		}
		$ten = Int64::ofInt(10);
		while (Int64::isNotZero(i)) {
			$r = Int64::divMod($i, $ten);
			if (Int64::isNeg($r->modulus)) {
				$str = Int64::neg($r->modulus)->low + $str;
				$i = Int64::neg($r->quotient);
			} else {
				$str = $r->modulus->low + $str;
				$i = $r->quotient;
			}
		}
		if ($neg) $str = "-" . $str;
		return $str;
	}

	static public function divMod($dividend, $divisor) {
		if ($divisor->high == 0) {
			switch ($divisor->low) {
				case 0: throw new Exception("divide by zero");
				case 1: return new DivModResult(Int64::make(dividend.high, dividend.low), Int64::ofInt(0));
			}
		}
		$divSign = Int64::isNeg($dividend) != Int64::isNeg($divisor);
		$modulus = Int64::isNeg($dividend) ? Int64::neg($dividend) : Int64::make($dividend->high, $dividend->low);
		$divisor = Int64::abs($divisor);

		$quotient = Int64::ofInt(0);
		$mask = Int64::ofInt(1);
		while (!Int64::isNeg($divisor)) {
			$cmp = Int64::ucompare($divisor, $modulus);
			$divisor = Int64::shl($divisor, 1);
			$mask = Int64::shl($mask, 1);
			if ($cmp >= 0) break;
		}
		while (Int64::ne($mask, Int64::ofInt(0))) {
			if (Int64::ucompare($modulus, $divisor) >= 0) {
				$quotient = Int64::or($quotient, $mask);
				$modulus = Int64::sub($modulus, $divisor);
			}
			$mask = Int64::ushr($mask, 1);
			$divisor = Int64::ushr($divisor, 1);
		}
		if ($divSign) $quotient = Int64::neg($quotient);
		if (Int64::isNeg($dividend)) $modulus = Int64::neg($modulus);
		return new DivModResult($quotient, $modulus);
	}

	static public function neg($x) {
		$high = ~$x->high;
		$low = -$x->low;
		if ($low == 0) $high = $high + 1;
		return Int64::make($high, $low);
	}

	static public function add($a, $b) {
		$high = $a->high + $b->high;
		$low = $a->low + $b->low;
		if (Int64::Integer_compareUnsigned($low, $a->low) < 0) {
			$high = $high + 1;
		}
		return Int64::make($high, $low);
	}

	static public function sub($a, $b) {
		$high = $a->high - $b->high;
		$low = $a->low - $b->low;
		if (Int64::Integer_compareUnsigned($a->low, $b->low) < 0) {
			$high = $high - 1;
		}
		return Int64::make($high, $low);
	}

	static public function mul($a, $b) {
		$al = $a->low & 65535;
		$ah = N::iushr($a->low, 16);
		$bl = $b->low & 65535;
		$bh = N::iushr($b->low, 16);
		$p00 = Int32::mul($al, $bl);
		$p10 = Int32::mul($ah, $bl);
		$p01 = Int32::mul($al, $bh);
		$p11 = Int32::mul($ah, $bh);
		$low = $p00;
		$high = ($p11 + N::iushr($p01, 16) | 0) + N::iushr($p10, 16) | 0;
		$p01 = $p01 << 16;
		$low = $p00 + $p01 | 0;
		if (Int32::ucompare($low, $p01) < 0) $high = $high + 1 | 0;
		$p10 = $p10 << 16;
		$low = $low + $p10 | 0;
		if (Int32::ucompare($low, $p10) < 0) $high = $high + 1 | 0;
		$high = $high + (Int32::mul($a->low, $b->high) + Int32::mul($a->high, $b->low) | 0) | 0;
		return Int64::make($high, $low);

	}

	static public function div($a, $b) { return Int64::divMod($a, $b)->quotient; }
	static public function mod($a, $b) { return Int64::divMod($a, $b)->modulus; }
	static public function rem($a, $b) { return Int64::divMod($a, $b)->modulus; }

	// BIT-WISE
	static public function not($x) { return Int64::make(~$x->high, ~$x->low); }

	static public function and($a, $b) { return Int64::make($a->high & $b->high, $a->low & $b->low); }
	static public function or($a, $b) { return Int64::make($a->high | $b->high, $a->low | $b->low); }
	static public function xor($a, $b) { return Int64::make($a->high ^ $b->high, $a->low ^ $b->low); }
	static public function shl($a, $b) {
		$b &= 63;
		if ($b == 0) {
			return Int64::make($a->high, $a->low);
		} else if ($b < 32) {
			return Int64::make($a->high << $b | N::iushr($a->low, 32 - $b), $a->low << $b);
		} else {
			return Int64::make($a->low << $b - 32, 0);
		}
	}

	static public function shr($a, $b) {
		$b &= 63;
		if ($b == 0) {
			return Int64::make($a->high, $a->low);
		} else if (b < 32) {
			return Int64::make($a->high >> $b, $a->high << 32 - $b | N::iushr($a->low, $b));
		} else {
			return Int64::make($a->high >> 31, $a->high >> $b - 32);
		}
	}

	static public function ushr($a, $b) {
		$b &= 63;
		if ($b == 0) {
			return Int64::make($a->high, $a->low);
		} else if ($b < 32) {
			return Int64::make(N::iushr($a->high, $b), $a->high << 32 - $b | N::iushr($a->low, $b));
		} else {
			return Int64::make(0, N::iushr($a->high, $b - 32));
		}
	}

	static public function sign($a) {
		if (Int64::isNeg($a)) return -1;
		if (Int64::isNotZero($a)) return +1;
		return 0;
	}

	static public function abs($a) {
		return Int64::isNeg($a) ? Int64::neg($a) : $a;
	}

	static public function getInternal($value) {
		return $value;
	}

}

Int64::$MAX_INT64 = new Int64(0x7FFFFFFF, 0xFFFFFFFF);
Int64::$MIN_INT64 = new Int64(0x80000000, 0x00000000);
Int64::$zero = new Int64(0, 0);
Int64::$one = new Int64(0, 1);
Int64::$MIN_VALUE = Int64::$MIN_INT64;
Int64::$MAX_VALUE = Int64::$MAX_INT64;

/* ## BODY ## */
