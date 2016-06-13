var Int32 = function(value) {
	this.value = value | 0;
};

Int32.compare = function(a, b) {
	a |= 0;
	b |= 0;
	/*
	a |= 0;
	b |= 0;
	if (a < b) return -1;
	if (a > b) return +1;
	return 0;
	*/
	if(a == b) {
		return 0;
	} else if(a > b) {
		return 1;
	} else {
		return -1;
	}
};

Int32.ucompare = function(a, b) {
	//a >>>= 0;
	//b >>>= 0;
	//if (a < b) return -1;
	//if (a > b) return +1;
	//return 0;

	if(a < 0) {
		if(b < 0) {
			return ~b - ~a | 0;
		} else {
			return 1;
		}
	}
	if(b < 0) {
		return -1;
	} else {
		return a - b | 0;
	}
};

Int32.mul = function(a, b) { return Math.imul(a, b); }

var Int64 = function(high, low) {
	this.high = high | 0;
	this.low = low | 0;
};

// Building
var M2P32_DBL = Math.pow(2, 32);
var MAX_INT64 = new Int64(0x7FFFFFFF, 0xFFFFFFFF);
var MIN_INT64 = new Int64(0x80000000, 0x00000000);
Int64.zero = new Int64(0, 0);
Int64.one = new Int64(0, 1);
Int64.MIN_VALUE = MIN_INT64;
Int64.MAX_VALUE = MAX_INT64;
Int64.is = function(value) { return value instanceof Int64; };
Int64.make = function(high, low) {
	if (high == 0) {
		if (low == 0) return Int64.zero;
		if (low == 1) return Int64.one;
	}
	return new Int64(high, low);
};
Int64.ofInt = function(value) { return Int64.make(value >> 31, value | 0); };
Int64.ofFloat = function(f) {
	if (isNaN(f) || !isFinite(f)) throw "Number is NaN or Infinite";
	var noFractions = f - (f % 1);
	// 2^53-1 and -2^53: these are parseable without loss of precision
	if (noFractions > 9007199254740991) throw "Conversion overflow";
	if (noFractions < -9007199254740991) throw "Conversion underflow";

	var result = Int64.ofInt(0);
	var neg = noFractions < 0;
	var rest = neg ? -noFractions : noFractions;

	var i = 0;
	while (rest >= 1) {
		var curr = rest % 2;
		rest = rest / 2;
		if (curr >= 1) result = Int64.add(result, Int64.shl(Int64.ofInt(1), i));
		i++;
	}

	return neg ? Int64.neg(result) : result;
};

Int64.ofString = function(sParam) {
	var base = Int64.ofInt(10);
	var current = Int64.ofInt(0);
	var multiplier = Int64.ofInt(1);
	var sIsNegative = false;

	var s = String(sParam).trim();
	if (s.charAt(0) == "-") {
		sIsNegative = true;
		s = s.substring(1, s.length);
	}
	var len = s.length;

	for (var i = 0; i < len; i++) {
		var digitInt = s.charCodeAt(len - 1 - i) - '0'.code;

		if (digitInt < 0 || digitInt > 9) throw "NumberFormatError";

		var digit = Int64.ofInt(digitInt);
		if (sIsNegative) {
			current = Int64.sub(current, Int64.mul(multiplier, digit));
			if (!Int64.isNeg(current)) throw "NumberFormatError: Underflow";
		} else {
			current = Int64.add(current, Int64.mul(multiplier, digit));
			if (Int64.isNeg(current)) throw "NumberFormatError: Overflow";
		}
		multiplier = Int64.mul(multiplier, base);
	}
	return current;
};

Int64.prototype.toString = function() {
	var i = this;
	if (Int64.isZero(i)) return "0";
	var str = "";
	var neg = false;
	if(Int64.isNeg(i)) {
		neg = true;
		// i = -i; cannot negate here as --9223372036854775808 = -9223372036854775808
	}
	var ten = Int64.ofInt(10);
	while (Int64.isNotZero(i)) {
		var r = Int64.divMod(i, ten);
		if (Int64.isNeg(r.modulus)) {
			str = Int64.neg(r.modulus).low + str;
			i = Int64.neg(r.quotient);
		} else {
			str = r.modulus.low + str;
			i = r.quotient;
		}
	}
	if( neg ) str = "-" + str;
	return str;
};

Int64.toInt = function(a) { return a.low; };
Int64.toFloat = function(v) {
	if (Int64.isNeg(v)) {
		return Int64.eq(v, MIN_INT64) ? Int64.ofFloat(-9223372036854775808.0) : -Int64.toFloat(Int64.neg(v));
	} else {
		var lowf = v.low;
		var highf = v.high;
		return lowf + highf * M2P32_DBL;
	}
};

Int64.isNeg = function(a) { return a.high < 0; };
Int64.isZero = function(a) { return a.high == 0 && a.low == 0; };
Int64.isNotZero = function(a) { return a.high != 0 || a.low != 0; };

// Comparisons

Int64.compare = function(a, b) {
	var v = a.high - b.high | 0;
	if (v == 0) v = Int32.ucompare(a.low, b.low);
	return (a.high < 0) ? ((b.high < 0) ? v : -1) : ((b.high >= 0) ? v : 1);
};

Int64.ucompare = function(a, b) {
	var v = Int32.ucompare(a.high, b.high);
	return (v != 0) ? v : Int32.ucompare(a.low, b.low);
};

Int64.eq  = function(a, b) { return (a.high == b.high) && (a.low == b.low); };
Int64.ne  = function(a, b) { return (a.high != b.high) || (a.low != b.low); };
Int64.neq = function(a, b) { return (a.high != b.high) || (a.low != b.low); };
Int64.lt  = function(a, b) { return Int64.compare(a, b) < 0; };
Int64.le  = function(a, b) { return Int64.compare(a, b) <= 0; };
Int64.gt  = function(a, b) { return Int64.compare(a, b) > 0; };
Int64.ge  = function(a, b) { return Int64.compare(a, b) >= 0; };

// Strings

Int64.prototype.toString = function() {
	var i = this;
	if(Int64.eq(i,Int64.ofInt(0))) {
		return "0";
	}
	var str = "";
	var neg = false;
	if(Int64.isNeg(i)) {
		neg = true;
	}
	var ten = Int64.ofInt(10);
	while(Int64.neq(i,Int64.ofInt(0))) {
		var r = Int64.divMod(i,ten);
		if(Int64.isNeg(r.modulus)) {
			str = Int64.neg(r.modulus).low + str;
			i = Int64.neg(r.quotient);
		} else {
			str = r.modulus.low + str;
			i = r.quotient;
		}
	}
	if(neg) {
		str = "-" + str;
	}
	return str;
};



// Arithmetic

Int64.divMod = function(dividend, divisor) {
	if(divisor.high == 0) {
		switch(divisor.low) {
		case 0:
			throw new js__$Boot_HaxeError("divide by zero");
			break;
		case 1:
			return { quotient : Int64.make(dividend.high,dividend.low), modulus : Int64.ofInt(0)};
		}
	}
	var divSign = Int64.isNeg(dividend) != Int64.isNeg(divisor);
	var modulus = Int64.isNeg(dividend)?Int64.neg(dividend):Int64.make(dividend.high,dividend.low);
	if(Int64.isNeg(divisor)) {
		divisor = Int64.neg(divisor);
	} else {
		divisor = divisor;
	}
	var quotient = Int64.ofInt(0);
	var mask = Int64.ofInt(1);
	while(!Int64.isNeg(divisor)) {
		var cmp = Int64.ucompare(divisor,modulus);
		divisor = Int64.shl(divisor,1);
		mask = Int64.shl(mask,1);
		if(cmp >= 0) {
			break;
		}
	}
	while(Int64.neq(mask,Int64.ofInt(0))) {
		if(Int64.ucompare(modulus,divisor) >= 0) {
			quotient = Int64.or(quotient,mask);
			modulus = Int64.sub(modulus,divisor);
		}
		mask = Int64.ushr(mask,1);
		divisor = Int64.ushr(divisor,1);
	}
	if(divSign) quotient = Int64.neg(quotient);
	if(Int64.isNeg(dividend)) modulus = Int64.neg(modulus);
	return { quotient : quotient, modulus : modulus};
};

Int64.neg = function(x) {
	var high = ~x.high | 0;
	var low = -x.low | 0;
	if(low == 0) high = high + 1 | 0;
	return Int64.make(high,low);
};

Int64.add = function(a, b) {
	var high = a.high + b.high | 0;
	var low = a.low + b.low | 0;
	if(Int32.ucompare(low,a.low) < 0) {
		high = high + 1 | 0;
	}
	return Int64.make(high,low);
};

Int64.sub = function(a, b) {
	var high = a.high - b.high | 0;
	var low = a.low - b.low | 0;
	if(Int32.ucompare(a.low,b.low) < 0) {
		high = high - 1 | 0;
	}
	return Int64.make(high,low);
};

Int64.mul = function(a, b) {
	var al = a.low & 65535;
	var ah = a.low >>> 16;
	var bl = b.low & 65535;
	var bh = b.low >>> 16;
	var p00 = Int32.mul(al,bl);
	var p10 = Int32.mul(ah,bl);
	var p01 = Int32.mul(al,bh);
	var p11 = Int32.mul(ah,bh);
	var low = p00;
	var high = (p11 + (p01 >>> 16) | 0) + (p10 >>> 16) | 0;
	p01 = p01 << 16;
	low = p00 + p01 | 0;
	if(Int32.ucompare(low,p01) < 0) high = high + 1 | 0;
	p10 = p10 << 16;
	low = low + p10 | 0;
	if(Int32.ucompare(low,p10) < 0) high = high + 1 | 0;
	high = high + (Int32.mul(a.low,b.high) + Int32.mul(a.high,b.low) | 0) | 0;
	return Int64.make(high,low);
};

Int64.div = function(a, b) { return Int64.divMod(a, b).quotient; };
Int64.mod = function(a, b) { return Int64.divMod(a, b).modulus; };
Int64.rem = function(a, b) { return Int64.divMod(a, b).modulus; };

// BIT-WISE
Int64.not = function(x) { return Int64.make(~x.high, ~x.low); }
Int64.and = function(a, b) { return Int64.make(a.high & b.high, a.low & b.low); }
Int64.or = function(a, b) { return Int64.make(a.high | b.high, a.low | b.low); }
Int64.xor = function(a, b) { return Int64.make(a.high ^ b.high, a.low ^ b.low); }
Int64.shl = function(a, b) {
	b &= 63;
	if(b == 0) {
		return Int64.make(a.high,a.low);
	} else if(b < 32) {
		return Int64.make(a.high << b | a.low >>> 32 - b,a.low << b);
	} else {
		return Int64.make(a.low << b - 32,0);
	}
}
Int64.shr = function(a, b) {
	b &= 63;
	if(b == 0) {
		return Int64.make(a.high,a.low);
	} else if(b < 32) {
		return Int64.make(a.high >> b,a.high << 32 - b | a.low >>> b);
	} else {
		return Int64.make(a.high >> 31,a.high >> b - 32);
	}
}
Int64.ushr = function(a, b) {
	b &= 63;
	if(b == 0) {
		return Int64.make(a.high,a.low);
	} else if(b < 32) {
		return Int64.make(a.high >>> b,a.high << 32 - b | a.low >>> b);
	} else {
		return Int64.make(0,a.high >>> b - 32);
	}
}

Int64.sign = function(a) {
	if (Int64.isNeg(a)) return -1;
	if (Int64.isNotZero(a)) return +1;
	return 0;
};

Int64.abs = function(a) {
	return (Int64.sign(a) < 0) ? Int64.neg(a) : a;
};

_global.Int64 = Int64;