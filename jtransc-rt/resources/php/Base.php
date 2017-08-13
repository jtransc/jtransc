<?php

// JTransc {{ JTRANSC_VERSION }} : https://github.com/jtransc/jtransc

// php program.php
// hhvm -v Eval.EnableHipHopSyntax=true program.php
if (defined('HHVM_VERSION')) {
	// @TODO: Check minimum HHVM_VERSION
} else {
	if (version_compare(phpversion(), '7.1.0', '<')) die('Requires PHP 7.1 or higher but was ' . phpversion());
}

ob_implicit_flush(true);

// http://hhvm.com/blog/713/hhvm-optimization-tips

set_error_handler('exceptions_error_handler');

function exceptions_error_handler($severity, $message, $filename, $lineno) {
  //if (error_reporting() == 0) return;
  //if (error_reporting() & $severity) {
    throw new ErrorException($message, 0, $severity, $filename, $lineno);
  //}
}

const PHP_INT_BITS_SIZE = PHP_INT_SIZE * 8;
const PHP_INT_BITS_SIZE_M1 = PHP_INT_BITS_SIZE - 1;
const PHP_INT_BITS_SIZE_M8 = PHP_INT_BITS_SIZE - 8;
const PHP_INT_BITS_SIZE_M16 = PHP_INT_BITS_SIZE - 16;
const PHP_INT_BITS_SIZE_M24 = PHP_INT_BITS_SIZE - 24;
const PHP_INT_BITS_SIZE_M32 = PHP_INT_BITS_SIZE - 32;

final class Int32 {
	static public function sx32(int $v) { return ($v << PHP_INT_BITS_SIZE_M32) >> PHP_INT_BITS_SIZE_M32; }

	static public function compare(int $a, int $b): int {
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

	static public function ucompare(int $a, int $b): int {
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

	static public function mul(int $a, int $b) : int {
		if (PHP_INT_SIZE == 4) {
			$ah = ($a >> 16) & 0xffff;
			$al = $a & 0xffff;
			$bh = ($b >> 16) & 0xffff;
			$bl = $b & 0xffff;
			// the shift by 0 fixes the sign on the high part
			// the final |0 converts the unsigned value into a signed value
			return (($al * $bl) + ((($ah * $bl + $al * $bh) << 16))|0);
		} else {
			return ($a * $b)|0;
		}
	}

	static public function iushr(int $a, int $b) : int {
		if ($b == 0) return $a;
		return ($a >> $b) & ~(1 << (PHP_INT_BITS_SIZE - 1) >> ($b - 1));
	}
}

final class DivModResult {
	public $quotient;
	public $modulus;

	public function __construct(Int64 $quotient, Int64 $modulus) {
		$this->quotient = $quotient;
		$this->modulus = $modulus;
	}
}

final class Int64 {
	static public $MAX_INT64;
	static public $MIN_INT64;
	static public $zero;
	static public $one;
	static public $MIN_VALUE;
	static public $MAX_VALUE;

	public $high = 0;
	public $low = 0;

	public function __construct(int $high, int $low) {
		$this->high = Int32::sx32($high);
		$this->low = Int32::sx32($low);
		//var_dump($high);
		//var_dump($low);
	}

	static public function make(int $high, int $low) : Int64 {
		if ($high == 0) {
			if ($low == 0) return Int64::$zero;
			if ($low == 1) return Int64::$one;
		}
		return new Int64($high, $low);
	}

	static public function ofInt(int $value): Int64 {
		return Int64::make($value >> PHP_INT_BITS_SIZE_M1, $value);
	}

	static public function ofFloat(float $f) : Int64 {
		if ($f == 0) return Int64::$zero;

		$v0 = $f % 0x10000; $f = floor($f / 0x10000);
		$v1 = $f % 0x10000; $f = floor($f / 0x10000);
		$v2 = $f % 0x10000; $f = floor($f / 0x10000);
		$v3 = $f % 0x10000; $f = floor($f / 0x10000);

		$low = ($v0 & 0xFFFF) | ($v1 << 16);
		$high = ($v2 & 0xFFFF) | ($v3 << 16);

		return Int64::make($high, $low);
	}

	static public function ofString(string $sParam, int $ibase = 10) : Int64 {
		$base = Int64::ofInt($ibase);
		$current = Int64::ofInt(0);
		$multiplier = Int64::ofInt(1);
		$sIsNegative = false;

		$s = trim($sParam);
		if (substr($s, 0, 1) == '-') {
			$sIsNegative = true;
			$s = substr($s, 1, strlen($s));
		}
		$len = strlen($s);

		for ($i = 0; $i < $len; $i++) {
			$digitInt = ord(substr($s, $len - 1 - $i, 1)) - ord('0');
			if ($digitInt < 0 || $digitInt > 9) throw new Exception("NumberFormatError: Invalid digit");
			$digit = Int64::ofInt($digitInt);
			$current = Int64::add($current, Int64::mul($multiplier, $digit));
			$multiplier = Int64::mul($multiplier, $base);
		}
		return $sIsNegative ? Int64::neg($current) : $current;
	}

	static public function toInt(Int64 $a) : int {
		return $a->low;
	}

	static public function toFloat(Int64 $v) : float {
		if (Int64::isNeg($v)) {
			return Int64::eq($v, Int64::$MIN_INT64) ? -9223372036854775808.0 : -Int64::toFloat(Int64::neg($v));
		} else {
			$lowf = $v->low;
			$highf = $v->high;
			return $lowf + $highf * pow(2, 32);
		}
	}

	static public function isNeg(Int64 $a) : bool { return $a->high < 0; }
	static public function isZero(Int64 $a) : bool { return $a->high == 0 && $a->low == 0; }
	static public function isNotZero(Int64 $a) : bool { return $a->high != 0 || $a->low != 0; }

// Comparisons

	static private function Integer_compare(int $a, int $b) : int { return Int32::compare($a, $b); }
	static private function Integer_compareUnsigned(int $a, int $b) : int { return Int32::ucompare($a, $b); }

	static public function compare(Int64 $a, Int64 $b) : int {
		$v = $a->high - $b->high;
		if ($v == 0) $v = Int64::Integer_compareUnsigned($a->low, $b->low);
		return ($a->high < 0) ? (($b->high < 0) ? $v : -1) : (($b->high >= 0) ? $v : 1);
	}

	static public function ucompare(Int64 $a, Int64 $b) : int {
		$v = Int64::Integer_compareUnsigned($a->high, $b->high);
		return ($v != 0) ? $v : Int64::Integer_compareUnsigned($a->low, $b->low);
	}

	static public function eq(Int64 $a, Int64 $b) : bool { return ($a->high == $b->high) && ($a->low == $b->low); }
	static public function ne(Int64 $a, Int64 $b) : bool { return ($a->high != $b->high) || ($a->low != $b->low); }
	static public function lt(Int64 $a, Int64 $b) : bool { return Int64::compare($a, $b) < 0; }
	static public function le(Int64 $a, Int64 $b) : bool { return Int64::compare($a, $b) <= 0; }
	static public function gt(Int64 $a, Int64 $b) : bool { return Int64::compare($a, $b) > 0; }
	static public function ge(Int64 $a, Int64 $b) : bool { return Int64::compare($a, $b) >= 0; }

	// Strings
	public function toString(): string {
		$i = $this;
		if (Int64::isZero($i)) return "0";
		$str = "";
		$neg = false;
		if (Int64::isNeg($i)) {
			$neg = true;
			// $i = -$i; cannot negate here as --9223372036854775808 = -9223372036854775808
		}
		$ten = Int64::ofInt(10);
		while (Int64::isNotZero($i)) {
			$r = Int64::divMod($i, $ten);
			if (Int64::isNeg($r->modulus)) {
				$str = Int64::neg($r->modulus)->low . $str;
				$i = Int64::neg($r->quotient);
			} else {
				$str = $r->modulus->low . $str;
				$i = $r->quotient;
			}
		}
		if ($neg) $str = "-$str";
		return $str;
	}

	public function __toString(): string {
		return $this->toString();
	}

	static public function divMod(Int64 $dividend, Int64 $divisor) : DivModResult {
		if ($divisor->high == 0) {
			switch ($divisor->low) {
				case 0: throw new Exception("divide by zero");
				case 1: return new DivModResult(Int64::make($dividend->high, $dividend->low), Int64::ofInt(0));
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

	static public function neg(Int64 $x): Int64 {
		$high = (~$x->high)|0;
		$low = (-$x->low)|0;
		if ($low == 0) $high = ($high + 1)|0;
		return Int64::make($high, $low);
	}

	static public function add(Int64 $a, Int64 $b): Int64 {
		$high = ($a->high + $b->high)|0;
		$low  = ($a->low + $b->low)|0;
		if (Int64::Integer_compareUnsigned($low, $a->low) < 0) {
			$high = ($high + 1)|0;
		}
		return Int64::make($high, $low);
	}

	static public function sub(Int64 $a, Int64 $b) : Int64 {
		$high = ($a->high - $b->high)|0;
		$low = ($a->low - $b->low)|0;
		if (Int64::Integer_compareUnsigned($a->low, $b->low) < 0) {
			$high = ($high - 1)|0;
		}
		return Int64::make($high, $low);
	}

	static public function mul(Int64 $a, Int64 $b) : Int64 {
		$al = $a->low & 65535;
		$ah = Int32::iushr($a->low, 16);
		$bl = $b->low & 65535;
		$bh = Int32::iushr($b->low, 16);
		$p00 = Int32::mul($al, $bl);
		$p10 = Int32::mul($ah, $bl);
		$p01 = Int32::mul($al, $bh);
		$p11 = Int32::mul($ah, $bh);
		$low = $p00;
		$high = ($p11 + Int32::iushr($p01, 16) | 0) + Int32::iushr($p10, 16) | 0;
		$p01 = $p01 << 16;
		$low = $p00 + $p01 | 0;
		if (Int32::ucompare($low, $p01) < 0) $high = $high + 1 | 0;
		$p10 = $p10 << 16;
		$low = $low + $p10 | 0;
		if (Int32::ucompare($low, $p10) < 0) $high = $high + 1 | 0;
		$high = $high + (Int32::mul($a->low, $b->high) + Int32::mul($a->high, $b->low) | 0) | 0;
		return Int64::make($high, $low);

	}

	static public function div(Int64 $a, Int64 $b) : Int64 { return Int64::divMod($a, $b)->quotient; }
	static public function mod(Int64 $a, Int64 $b) : Int64 { return Int64::divMod($a, $b)->modulus; }
	static public function rem(Int64 $a, Int64 $b) : Int64 { return Int64::divMod($a, $b)->modulus; }

	// BIT-WISE
	static public function not(Int64 $x) : Int64 { return Int64::make(~$x->high, ~$x->low); }

	static public function and(Int64 $a, Int64 $b) : Int64 { return Int64::make($a->high & $b->high, $a->low & $b->low); }
	static public function or (Int64 $a, Int64 $b) : Int64 { return Int64::make($a->high | $b->high, $a->low | $b->low); }
	static public function xor(Int64 $a, Int64 $b) : Int64 { return Int64::make($a->high ^ $b->high, $a->low ^ $b->low); }

	static public function shl(Int64 $a, int $b) : Int64 {
		$b &= 63;
		if ($b == 0) {
			return Int64::make($a->high, $a->low);
		} else if ($b < 32) {
			return Int64::make($a->high << $b | Int32::iushr($a->low, 32 - $b), $a->low << $b);
		} else {
			return Int64::make($a->low << $b - 32, 0);
		}
	}

	static public function shr(Int64 $a, int $b) : Int64 {
		$b &= 63;
		if ($b == 0) {
			return Int64::make($a->high, $a->low);
		} else if ($b < 32) {
			return Int64::make($a->high >> $b, $a->high << 32 - $b | Int32::iushr($a->low, $b));
		} else {
			return Int64::make($a->high >> 31, $a->high >> $b - 32);
		}
	}

	static public function ushr(Int64 $a, int $b) : Int64 {
		$b &= 63;
		if ($b == 0) {
			return Int64::make($a->high, $a->low);
		} else if ($b < 32) {
			return Int64::make(Int32::iushr($a->high, $b), $a->high << 32 - $b | Int32::iushr($a->low, $b));
		} else {
			return Int64::make(0, Int32::iushr($a->high, $b - 32));
		}
	}

	static public function sign(Int64 $a) : int {
		if (Int64::isNeg($a)) return -1;
		if (Int64::isNotZero($a)) return +1;
		return 0;
	}

	static public function abs(Int64 $a) : Int64 {
		return Int64::isNeg($a) ? Int64::neg($a) : $a;
	}

	static public function getInternal(Int64 $value) : Int64 {
		return $value;
	}

}

Int64::$MAX_INT64 = new Int64(0x7FFFFFFF|0, 0xFFFFFFFF|0);
Int64::$MIN_INT64 = new Int64(0x80000000|0, 0x00000000|0);
Int64::$zero = new Int64(0, 0);
Int64::$one = new Int64(0, 1);
Int64::$MIN_VALUE = Int64::$MIN_INT64;
Int64::$MAX_VALUE = Int64::$MAX_INT64;

final class N {
	static public $DOUBLE_NAN;
	static public $DOUBLE_NEGATIVE_INFINITY;
	static public $DOUBLE_POSITIVE_INFINITY;

	static public $FLOAT_NAN;
	static public $FLOAT_NEGATIVE_INFINITY;
	static public $FLOAT_POSITIVE_INFINITY;

	const MIN_INT32 = -2147483648;
	const MAX_INT32 = 2147483647;

	static function init() {
	}

	static public function d2i(float $v) : int {
		if (is_finite($v)) {
			return $v|0;
		} else if (is_nan($v)) {
			return 0;
		} else if ($v >= 0) {
			return 2147483647;
		} else {
			return -2147483648;
		}
	}

	static public function f2i(float $v) : int {
		return N::d2i($v);
	}

	static public function utf16_to_utf8(string $str) : string {
		return mb_convert_encoding($str, 'UTF-8', 'UTF-16LE');
	}

	static public function utf8_to_utf16(string $str) : string {
		return mb_convert_encoding($str, 'UTF-16LE', 'UTF-8');
	}

	static public function FIXSHIFT(int $r) : int {
		if ($r < 0) {
			return (32 - ((-$r) & 0x1F)) & 0x1F;
		} else {
			return $r & 0x1F;
		}
	}

	static public function LFIXSHIFT(int $r) : int {
		if ($r < 0) {
			return (64 - ((-$r) & 0x3F)) & 0x3F;
		} else {
			return $r & 0x3F;
		}
	}

	static public function ishl(int $a, int $b) : int { return $a << N::FIXSHIFT($b); }
	static public function ishr(int $a, int $b) : int { return $a >> N::FIXSHIFT($b); }

	static public function iushr(int $a, int $b) : int {
	    $b = N::FIXSHIFT($b);
	    if ($b == 0) return $a;
        return ($a >> $b) & ~(1 << (PHP_INT_BITS_SIZE - 1) >> ($b - 1));
	}

	static public function irem(int $a, int $b) : int { return $a % $b; }
	static public function imul(int $a, int $b) : int { return Int32::mul($a, $b); }
	static public function idiv(int $a, int $b) : int {
		if ($a == PHP_INT_MIN && $b == -1) return -2147483648;
		return intdiv($a, $b);
	}

	static public function i(int $v) { return ($v << PHP_INT_BITS_SIZE_M32) >> PHP_INT_BITS_SIZE_M32; }

	static public function i2j(int $v) : Int64 { return Int64::ofInt($v); }
	static public function j2i(Int64 $v) : int { return $v->low; }
	static public function z2i($v) : int { return $v ? 1 : 0; }
	static public function i2c(int $v) : int { return $v & 0xFFFF; }
	static public function i2b(int $v) : int { return (($v & 0xFF) << (PHP_INT_BITS_SIZE_M8)) >> PHP_INT_BITS_SIZE_M8; }
	static public function i2s(int $v) : int { return (($v & 0xFFFF) << (PHP_INT_BITS_SIZE_M16)) >> PHP_INT_BITS_SIZE_M16; }
	static public function d2j(float $v) : Int64 { return Int64::ofFloat($v); }
	static public function j2d(Int64 $v) : float { return Int64::toFloat($v); }

	static public function sx8(int $v) : int { return ($v << PHP_INT_BITS_SIZE_M8) >> PHP_INT_BITS_SIZE_M8; }
	static public function sx16(int $v) : int { return ($v << PHP_INT_BITS_SIZE_M16) >> PHP_INT_BITS_SIZE_M16; }
	static public function sx32(int $v) : int { return ($v << PHP_INT_BITS_SIZE_M32) >> PHP_INT_BITS_SIZE_M32; }

	static function lnew (int   $h, int   $l) : Int64 { return Int64::make($h, $l); }
	static function lneg (Int64 $l) : Int64 { return Int64::neg($l); }
	static function lsub (Int64 $l, Int64 $r) : Int64 { return Int64::sub($l, $r); }
	static function ladd (Int64 $l, Int64 $r) : Int64 { return Int64::add($l, $r); }
	static function lmul (Int64 $l, Int64 $r) : Int64 { return Int64::mul($l, $r); }
	static function ldiv (Int64 $l, Int64 $r) : Int64 { return Int64::div($l, $r); }
	static function lrem (Int64 $l, Int64 $r) : Int64 { return Int64::rem($l, $r); }
	static function lshl (Int64 $l, int   $r) : Int64 { return Int64::shl($l, $r); }
	static function lshr (Int64 $l, int   $r) : Int64 { return Int64::shr($l, $r); }
	static function lushr(Int64 $l, int   $r) : Int64 { return Int64::ushr($l, $r); }
	static function land (Int64 $l, Int64 $r) : Int64 { return Int64::and($l, $r); }
	static function lxor (Int64 $l, Int64 $r) : Int64 { return Int64::xor($l, $r); }
	static function lor  (Int64 $l, Int64 $r) : Int64 { return Int64::or($l, $r); }

	static function lcmp (Int64 $l, Int64 $r) : int   { return Int64::compare($l, $r); }

	static function cmp ($a, $b) { return ($a < $b) ? (-1) : (($a > $b) ? (1) : 0); }
	static function cmpl($a, $b) { return (is_nan($a) || is_nan($b)) ? (-1) : N::cmp($a, $b); }
	static function cmpg($a, $b) { return (is_nan($a) || is_nan($b)) ? (1) : N::cmp($a, $b); }

	static function monitorEnter($v) { }
	static function monitorExit($v) { }

	static function resolveClass(string $name) {
		return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N::str($name));
	}

	static public function _getTime() : float {
		return floor(microtime(true) * 1000);

		//list($frac, $seconds) = explode(' ', microtime(false));
		//$ms = floor($frac * 1000);
		//$secs = floor($seconds * 1000);
		//return floor($secs + $ms);

		//$info = gettimeofday(false);
		//$ms1 = floor($info['sec'] * 1000);
		//$ms2 = floor($info['usec'] / 1000);
		//return floor($ms1 + $ms2);
	}

	static public function getTime() : float {
		$time = N::_getTime();
		//echo $time, ' :: ', Int64::ofString('' . $time)->toString(), ' :: ', N::d2j($time)->toString(), "\n";
		return $time;
	}

	static public $startTime;

	static public function nanoTime() : Int64 {
		$elapsedMs = N::_getTime() - N::$startTime;
		return N::lmul(N::d2j(round($elapsedMs * 1000)), Int64::ofInt(1000));
	}

	static function str(string $str): {% CLASS java.lang.String %} {
		// UTF-8 string
		$rstr = N::utf8_to_utf16($str);
		$len = (int)(strlen($rstr) / 2);
		$chars = new JA_C($len);
		$chars->data = TypedBuffer::fromString($rstr);
		//for ($n = 0; $n < $len; $n++) {
		//	$low = ord($rstr[$n * 2 + 0]) & 0xFF;
		//	$high = ord($rstr[$n * 2 + 1]) & 0xFF;
		//	$chars->set($n, ($high << 8) | $low);
		//}
		return {% CONSTRUCTOR java.lang.String:([C)V %}($chars);
	}

	static function istr({% CLASS java.lang.String%} $str) : string {
		if ($str == null) return null;
		//$out = '';
		//$len = $str->{% METHOD java.lang.String:length %}();
		//for ($n = 0; $n < $len; $n++) $out .= chr($str->{% METHOD java.lang.String:charAt %}($n));
		//return N::utf16_to_utf8($out);
		if ($str->_str == null) {
			$str->_str = N::utf16_to_utf8($str->{% FIELD java.lang.String:value %}->data->data);
		}
		return $str->_str;
	}

	static public function strArray(array $array) : JA_L {
		return JA_L::fromArray('[Ljava/lang/String;', array_map(function($v) { return N::str($v); }, $array));
	}

	static public function arraycopy({% CLASS java.lang.Object %} $src, int $srcPos, {% CLASS java.lang.Object %} $dst, int $dstPos, int $len) : void {
		if ($src instanceof JA_Typed) {
			$esize = $src->esize;
			$src->data->copyTo($dst->data, $srcPos * $esize, $dstPos * $esize, $len * $esize);
			//echo "$srcPos, $dstPos, $len, $esize\n";
		} else if ($src instanceof JA_Array) {
			$overlapping = ($src === $dst && $dstPos > $srcPos);
			if ($overlapping) {
				$n = $len;
				while (--$n >= 0) $dst->set($dstPos + $n, $src->get($srcPos + $n));
			} else {
				for ($n = 0; $n < $len; $n++) $dst->set($dstPos + $n, $src->get($srcPos + $n));
			}
		} else {
			throw new Exception("Invalid array");
		}
	}

	static public $tempBuffer;

	static public function longBitsToDouble(Int64 $v) : float {
		N::$tempBuffer->set32(0, $v->low);
		N::$tempBuffer->set32(4, $v->high);
		return N::$tempBuffer->getF64(0);
	}

	static public function doubleToLongBits(float $v) : Int64 {
		N::$tempBuffer->setF64(0, $v);
		$low = N::$tempBuffer->getS32(0);
		$high = N::$tempBuffer->getS32(4);
		return Int64::make($high, $low);
	}


	static public function intBitsToFloat(int $v) : float {
		N::$tempBuffer->set32(0, $v);
		return N::$tempBuffer->getF32(0);
	}

	static public function floatToIntBits(float $v) : int {
		N::$tempBuffer->setF32(0, $v);
		return N::$tempBuffer->getS32(0);
	}

	static public function  unboxBool  ({% CLASS java.lang.Boolean %}   $i) : bool   { return $i{% IMETHOD java.lang.Boolean:booleanValue %}(); }
	static public function  unboxByte  ({% CLASS java.lang.Byte %}      $i) : int    { return $i{% IMETHOD java.lang.Byte:byteValue %}(); }
	static public function  unboxShort ({% CLASS java.lang.Short %}     $i) : int    { return $i{% IMETHOD java.lang.Short:shortValue %}(); }
	static public function  unboxChar  ({% CLASS java.lang.Character %} $i) : int    { return $i{% IMETHOD java.lang.Character:charValue %}(); }
	static public function  unboxInt   ({% CLASS java.lang.Integer %}   $i) : int    { return $i{% IMETHOD java.lang.Integer:intValue %}(); }
	static public function  unboxLong  ({% CLASS java.lang.Long %}      $i) : Int64  { return $i{% IMETHOD java.lang.Long:longValue %}(); }
	static public function  unboxFloat ({% CLASS java.lang.Float %}     $i) : float  { return $i{% IMETHOD java.lang.Float:floatValue %}(); }
	static public function  unboxDouble({% CLASS java.lang.Double %}    $i) : float  { return $i{% IMETHOD java.lang.Double:doubleValue %}(); }

	static public function boxVoid  (         ) : {% CLASS java.lang.Object %}    { return null; }
	static public function boxBool  (bool   $v) : {% CLASS java.lang.Boolean %}   { return {% SMETHOD java.lang.Boolean:valueOf:(Z)Ljava/lang/Boolean; %}($v); }
	static public function boxByte  (int    $v) : {% CLASS java.lang.Byte %}      { return {% SMETHOD java.lang.Byte:valueOf:(B)Ljava/lang/Byte; %}($v); }
	static public function boxShort (int    $v) : {% CLASS java.lang.Short %}     { return {% SMETHOD java.lang.Short:valueOf:(S)Ljava/lang/Short; %}($v); }
	static public function boxChar  (int    $v) : {% CLASS java.lang.Character %} { return {% SMETHOD java.lang.Character:valueOf:(C)Ljava/lang/Character; %}($v); }
	static public function boxInt   (int    $v) : {% CLASS java.lang.Integer %}   { return {% SMETHOD java.lang.Integer:valueOf:(I)Ljava/lang/Integer; %}($v); }
	static public function boxLong  (Int64  $v) : {% CLASS java.lang.Long %}      { return {% SMETHOD java.lang.Long:valueOf:(J)Ljava/lang/Long; %}($v); }
	static public function boxFloat (float  $v) : {% CLASS java.lang.Float %}     { return {% SMETHOD java.lang.Float:valueOf:(F)Ljava/lang/Float; %}($v); }
	static public function boxDouble(float  $v) : {% CLASS java.lang.Double %}    { return {% SMETHOD java.lang.Double:valueOf:(D)Ljava/lang/Double; %}($v); }

	static public function fillSecureRandomBytes(TypedBuffer $buffer) {
		$buffer->putBytes(random_bytes($buffer->length), 0);
	}

	static public function checkcast($v, string $classname) {
		if ($v == null) return null;
		if (!is_a($v, $classname)) {
			throw new WrappedThrowable({% CONSTRUCTOR java.lang.ClassCastException:(Ljava/lang/String;)V %}(N::str("Class cast error. Object '$classname'")));
		}
		return $v;
	}
}

N::$startTime = N::_getTime();

N::$tempBuffer = TypedBuffer::alloc(16);

N::$DOUBLE_NAN = N::longBitsToDouble(Int64::make((int)0x7FF80000, (int)0x00000000));
N::$DOUBLE_NEGATIVE_INFINITY = -INF;
N::$DOUBLE_POSITIVE_INFINITY = +INF;

N::$FLOAT_NAN = N::intBitsToFloat((int)0x7FC00000);
N::$FLOAT_NEGATIVE_INFINITY = -INF;
N::$FLOAT_POSITIVE_INFINITY = +INF;

// @TODO: Critical Performance. All arrays uses this. So this must be as fast as possible. Specially aligned* methods.
final class TypedBuffer {
	public $length = 0;
	public $data = null;

	public function __construct(string $data) { $this->data = $data; $this->length = strlen($data); }

	static public function alloc(int $size) { return new TypedBuffer(str_repeat(chr(0), $size)); }
	static public function allocRepeat(string $base, int $size) { return new TypedBuffer(str_repeat($base, $size)); }
	static public function fromString(string $data) { return new TypedBuffer($data); }

	public function getAllBytes() { return $this->data; }
	public function getRangeBytes(int $start, int $len) { return substr($this->data, $start, $len); }

	public function putBytes(string $bytes, int $offset) : void {
		$len = strlen($bytes);
		for ($n = 0; $n < $len; $n++) $this->data[$offset + $n] = $bytes[$n];
	}

	public function copyTo(TypedBuffer $dstBuffer, int $srcPos, int $dstPos, int $len) : void {
		$overlapping = ($this === $dstBuffer && $dstPos > $srcPos);
		if ($overlapping) {
			$n = $len;
			while (--$n >= 0) $dstBuffer->data[$dstPos + $n] = $this->data[$srcPos + $n];
		} else {
			for ($n = 0; $n < $len; $n++) $dstBuffer->data[$dstPos + $n] = $this->data[$srcPos + $n];
		}
	}

	public function checkIndex(int $n) { if ($n > $this->length) throw new Exception("Index out of bounds $n of {$this->length}"); }

	public function getU8 (int $n) : int   { $this->checkIndex($n + 1); return (ord($this->data[$n]) & 0xFF); }
	public function getU16(int $n) : int   { $this->checkIndex($n + 2); return (ord($this->data[$n + 0]) | (ord($this->data[$n + 1]) << 8)) & 0xFFFF; }
	public function getU32(int $n) : int   { $this->checkIndex($n + 4); return (ord($this->data[$n + 0]) | (ord($this->data[$n + 1]) << 8) | (ord($this->data[$n + 2]) << 16) | (ord($this->data[$n + 3]) << 24)); }
	public function getF32(int $n) : float { $this->checkIndex($n + 4); return unpack("f", substr($this->data, $n, 4))[1]; }
	public function getF64(int $n) : float { $this->checkIndex($n + 8); return unpack("d", substr($this->data, $n, 8))[1]; }

	public function set8 (int $n, int $v) : void { $this->data[$n + 0] = chr(($v >> 0) & 0xFF); }
	public function set16(int $n, int $v) : void { $this->data[$n + 0] = chr(($v >> 0) & 0xFF); $this->data[$n + 1] = chr(($v >> 8) & 0xFF); }
	public function set32(int $n, int $v) : void { $this->data[$n + 0] = chr(($v >> 0) & 0xFF); $this->data[$n + 1] = chr(($v >> 8) & 0xFF); $this->data[$n + 2] = chr(($v >> 16) & 0xFF); $this->data[$n + 3] = chr(($v >> 24) & 0xFF); }
	public function setF32(int $n, float $v) : void { $s = pack('f', $v); for ($m = 0; $m < 4; $m++) $this->data[$n + $m] = $s[$m]; }
	public function setF64(int $n, float $v) : void { $s = pack('d', $v); for ($m = 0; $m < 8; $m++) $this->data[$n + $m] = $s[$m]; }

	public function getS8 (int $n) : int { return N::sx8($this->getU8($n)); }
	public function getS16(int $n) : int { return N::sx16($this->getU16($n)); }
	public function getS32(int $n) : int { return (int)$this->getU32($n); }

	// @TODO: Best performance required:
	public function alignedgetU8 (int $n) : int   { return $this->getU8 ($n * 1); }
	public function alignedgetU16(int $n) : int   { return $this->getU16($n * 2); }
	public function alignedgetU32(int $n) : int   { return $this->getU32($n * 4); }
	public function alignedgetS8 (int $n) : int   { return $this->getS8 ($n * 1); }
	public function alignedgetS16(int $n) : int   { return $this->getS16($n * 2); }
	public function alignedgetS32(int $n) : int   { return $this->getS32($n * 4); }
	public function alignedgetF32(int $n) : float { return $this->getF32($n * 4); }
	public function alignedgetF64(int $n) : float { return $this->getF64($n * 8); }
	public function alignedset8  (int $n, int   $v) : void { $this->set8  ($n * 1, $v); }
	public function alignedset16 (int $n, int   $v) : void { $this->set16 ($n * 2, $v); }
	public function alignedset32 (int $n, int   $v) : void { $this->set32 ($n * 4, $v); }
	public function alignedsetF32(int $n, float $v) : void { $this->setF32($n * 4, $v); }
	public function alignedsetF64(int $n, float $v) : void { $this->setF64($n * 8, $v); }
}

abstract class JA_0 extends {% CLASS java.lang.Object %} {
	public $length = 0;
	public $desc = '';

	public function __construct(int $length, string $desc) {
		$this->length = $length;
		$this->desc = $desc;
	}

	public function __toString() {
		return $this->desc;
	}
}

abstract class JA_Typed extends JA_0 {
	public $data = null;
	public $esize = 0;

	public function __construct(int $length, int $esize, string $repeat, string $desc) {
		parent::__construct($length, $desc);
		$this->esize = $esize;
		$this->data = TypedBuffer::allocRepeat($repeat, $length);
	}
}

class JA_Array extends JA_0 {
	public $data = null;

	public function __construct(int $length, string $desc, $default = null) {
		parent::__construct($length, $desc);
		$this->data = array_fill(0, $length, $default);
	}

	public function set(int $index, $value) : void { $this->data[$index] = $value; }
	public function get(int $index) { return $this->data[$index]; }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////

class JA_B extends JA_Typed {
	public function __construct($length, $desc = '[B') { parent::__construct($length, 1, "\0", $desc); }
	public function set(int $index, int $value) : void { $this->data->alignedset8($index, $value); }
	public function get(int $index) : int { return $this->data->alignedgetS8($index); }
}

class JA_Z extends JA_B {
	public function __construct(int $length) { parent::__construct($length, '[Z'); }
	public function set(int $index, int $value) : void { $this->data->alignedset8($index, (int)$value); }
	public function get(int $index) : int { return (boolean)$this->data->alignedgetS8($index); }
}

final class JA_C extends JA_Typed {
	public function __construct(int $length) { parent::__construct($length, 2, "\0\0", '[C'); }
	public function set(int $index, int $value) : void { $this->data->alignedset16($index, $value); }
	public function get(int $index) : int { return $this->data->alignedgetU16($index); }
}

final class JA_S extends JA_Typed {
	public function __construct(int $length) { parent::__construct($length, 2, "\0\0", '[S'); }
	public function set(int $index, int $value) : void { $this->data->alignedset16($index, $value); }
	public function get(int $index) : int { return $this->data->alignedgetS16($index); }
}

final class JA_I extends JA_Typed {
	public function __construct(int $length) { parent::__construct($length, 4, "\0\0\0\0", '[I'); }

	static public function T($array) {
		$len = count($array);
		$out = new JA_I($len);
		for ($n = 0; $n < $len; $n++) $out->set($n, $array[$n]);
		return $out;
	}

	public function set(int $index, int $value) : void { $this->data->alignedset32($index, $value); }
	public function get(int $index) : int { return $this->data->alignedgetS32($index); }
}

final class JA_F extends JA_Typed {
	public function __construct(int $length) { parent::__construct($length, 4, pack('f', 0.0), '[F'); }
	public function set(int $index, float $value) : void { $this->data->alignedsetF32($index, $value); }
	public function get(int $index) : float { return $this->data->alignedgetF32($index); }
}

final class JA_D extends JA_Typed {
	public function __construct(int $length) { parent::__construct($length, 8, pack('d', 0.0), '[D'); }
	public function set(int $index, float $value) : void { $this->data->alignedsetF64($index, $value); }
	public function get(int $index) : float { return $this->data->alignedgetF64($index); }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////

final class JA_J extends JA_Array {
	public function __construct(int $length) { parent::__construct($length, '[J', Int64::$zero); }
}

final class JA_L extends JA_Array {
	public function __construct(int $length, string $desc) { parent::__construct($length, $desc, null); }

	static function fromArray(string $desc, array $items) : JA_L {
		$count = count($items);
		$out = new JA_L($count, $desc);
		for ($n = 0; $n < $count; $n++) $out->set($n, $items[$n]);
		return $out;
	}

	static function T0(string $desc) : JA_L { return JA_L::fromArray($desc, []); }
	static function T1(string $desc, $a) : JA_L { return JA_L::fromArray($desc, [$a]); }
	static function T2(string $desc, $a, $b) : JA_L { return JA_L::fromArray($desc, [$a, $b]); }
	static function T3(string $desc, $a, $b, $c) : JA_L { return JA_L::fromArray($desc, [$a, $b, $c]); }
	static function T4(string $desc, $a, $b, $c, $d) : JA_L { return JA_L::fromArray($desc, [$a, $b, $c, $d]); }

	static function createMultiSure(string $desc, array $sizes) : JA_0 {
		return JA_L::_createMultiSure($desc, 0, $sizes);
	}

	static function _createMultiSure(string $desc, int $index, array $sizes) : JA_0 {
		if (substr($desc, 0, 1) != "[") return null;
		if ($index >= count($sizes) - 1) return JA_L::create($sizes[$index], $desc);
		$len = $sizes[$index];
		$o = new JA_L($len, $desc);
		$desc2 = substr($desc, 1);
		for ($n = 0; $n < $len; $n++) {
			$o->data[$n] = JA_L::_createMultiSure($desc2, $index + 1, $sizes);
		}
		return $o;
	}

	static function create(int $size, string $desc) {
		switch ($desc) {
			case "[Z": return new JA_Z($size);
			case "[B": return new JA_B($size);
			case "[C": return new JA_C($size);
			case "[S": return new JA_S($size);
			case "[I": return new JA_I($size);
			case "[J": return new JA_J($size);
			case "[F": return new JA_F($size);
			case "[D": return new JA_D($size);
			default: return new JA_L($size, $desc);
		}
	}
}

class WrappedThrowable extends Exception {
	public $t;

	public function __construct($t) { $this->t = $t; }

	//public function __toString() { return '' . $this->t; }
}

/* ## BODY ## */
