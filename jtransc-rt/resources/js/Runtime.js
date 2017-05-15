var _global = (typeof window !== "undefined") ? window : global;

if ('á'.charCodeAt(0) != 225) {
	throw 'Encoding must be UTF-8. Please add <META http-equiv="Content-Type" content="text/html; charset=utf-8" /> to the html';
}

// Polyfills
Array.prototype.includes = Array.prototype.includes || (function(searchElement /*, fromIndex*/ ) {
	var O = Object(this);
	var len = parseInt(O.length, 10) || 0;
	if (len === 0) return false;
	var n = parseInt(arguments[1], 10) || 0;
	var k;
	if (n >= 0) {
		k = n;
	} else {
		k = len + n;
		if (k < 0) k = 0;
	}
	for (;k < len; k++) if (searchElement === O[k]) return true;
	return false;
});

Array.prototype.map = Array.prototype.map || (function(callback, thisArg) {
	var T, A, k;
	var O = Object(this);
	var len = O.length >>> 0;
	if (arguments.length > 1) T = thisArg;
	A = new Array(len);
	k = 0;
	while (k < len) {
		var kValue, mappedValue;
		if (k in O) {
			kValue = O[k];
			mappedValue = callback.call(T, kValue, k, O);
			A[k] = mappedValue;
		}
		k++;
	}
	return A;
});

Array.prototype.contains = Array.prototype.contains || (function(searchElement) { return this.indexOf(searchElement) >= 0; });
Map.prototype.remove = Map.prototype.remove || (function(key) { this.delete(key); });

Math.imul = Math.imul || function(a, b) {
	var ah = (a >>> 16) & 0xffff;
	var al = a & 0xffff;
	var bh = (b >>> 16) & 0xffff;
	var bl = b & 0xffff;
	return ((al * bl) + (((ah * bl + al * bh) << 16) >>> 0)|0);
};

Math.clz32 = Math.clz32 || (function (x) { return (x >>>= 0) ? 31 - Math.floor(Math.log(x + 0.5) * Math.LOG2E) : 32; });
Math.fround = Math.fround || (function (array) { return function(x) { return array[0] = x, array[0]; }; })(new Float32Array(1));

String.prototype.reverse = String.prototype.reverse || (function() { return this.split("").reverse().join(""); });

String.prototype.startsWith = String.prototype.startsWith || (function(searchString, position){
	position = position || 0;
	return this.substr(position, searchString.length) === searchString;
});

String.prototype.endsWith = String.prototype.endsWith || (function(searchString, position) {
	if (position === undefined) position = subjectString.length;
	var subjectString = this.toString();
	position -= searchString.length;
	var lastIndex = subjectString.indexOf(searchString, position);
	return lastIndex !== -1 && lastIndex === position;
});

String.prototype.replaceAll = String.prototype.replaceAll || (function(search, replacement) {
	var target = this;
	return target.split(search).join(replacement);
});

String.prototype.trim = String.prototype.trim || (function () { return this.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, ''); });
String.prototype.quote = String.prototype.quote || (function () { return JSON.stringify(this); });

var onBrowser = typeof window != "undefined";
var onNodeJs = typeof window == "undefined";

(function(_global) { "use strict";

////////////////////////////////////////////////////////////////////////////

var Int32 = function(value) { this.value = value | 0; };
Int32.compare = function(a, b) {
	a |= 0;
	b |= 0;
	if(a == b) {
		return 0;
	} else if(a > b) {
		return 1;
	} else {
		return -1;
	}
};

Int32.ucompare = function(a, b) {
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

////////////////////////////////////////////////////////////////////////////

var S = [];
var SS = [];

function __buildStrings() {
	var len = SS.length
	S.length = len;
	for (var n = 0; n < len; n++) S[n] = N.str(SS[n]);
}

var JA_0, JA_Z, JA_B, JA_C, JA_S, JA_I, JA_J, JA_F, JA_D, JA_L;

function __createJavaArrayBaseType() {
	var ARRAY = function() {
	};

	ARRAY.prototype = Object.create({% CLASS java.lang.Object %}.prototype);
	ARRAY.prototype.constructor = ARRAY;

	ARRAY.prototype['{% METHOD java.lang.Object:getClass %}'] = function() {
		return N.resolveClass(this.desc);
	};

	ARRAY.prototype['setArraySlice'] = function(startIndex, array) {
		var len = array.length;
		for (var n = 0; n < len; n++) this.data[startIndex + n] = array[n];
	};


	return ARRAY;
}

function __addArrayJavaMethods(ARRAY) {
	ARRAY.prototype['{% METHOD java.lang.Object:clone %}'] = ARRAY.prototype.clone;

	ARRAY.prototype['{% METHOD java.lang.Object:getClass %}'] = function() {
		return N.resolveClass(this.desc);
	};

	ARRAY.prototype['{% METHOD java.lang.Object:toString %}'] = function() {
		return N.str('ARRAY(' + this.desc + ')');
	};
}

function __createJavaArrayType(desc, type, elementBytesSize) {
	var ARRAY;
	ARRAY = function(size) {
		this.desc = desc;
		this.memorySize = size * elementBytesSize;
		this.data = new type((((this.memorySize + 7) & ~7) / elementBytesSize)|0);
		this.length = size;
		this.init();

		//console.log('Created array instance: [' + desc + ":" + type.name + ":" + size + "]");
	};

	ARRAY.prototype = Object.create(JA_0.prototype);
	ARRAY.prototype.constructor = ARRAY;

	if (desc == '[J') {
		ARRAY.prototype.init = function() {
			var zero = N.lnew(0, 0);
			for (var n = 0; n < this.length; n++) this.set(n, zero);
		};
		ARRAY.prototype.clone = function() {
			var out = new ARRAY(this.length);
			for (var n = 0; n < this.length; n++) out.set(n, this.get(n));
			return out;
		};
	} else {
		ARRAY.prototype.init = function() { };
		ARRAY.prototype.clone = function() {
			var out = new ARRAY(this.length);
			out.data.set(this.data);
			return out;
		};
	}

	ARRAY.fromTypedArray = function(typedArray) {
		var out = new ARRAY(typedArray.length);
		out.data.set(typedArray);
		return out;
	};

	ARRAY.T = ARRAY.fromTypedArray;

	ARRAY.wrapBuffer = function(arrayBuffer) {
		var out = new ARRAY(0);
		out.data = new type(arrayBuffer);
		out.length = out.data.length;
		return out;
	};

	ARRAY.prototype.get = function(index) { return this.data[index]; };
	ARRAY.prototype.set = function(index, value) { this.data[index] = value; };

	ARRAY.prototype.getBuffer = function() {
		return this.data.buffer;
	};

	ARRAY.prototype.toArray = function() {
    	var out = new Array(this.length);
    	for (var n = 0; n < out.length; n++) out[n] = this.get(n);
    	return out;
    };

	//override fun genStmSetArrayLiterals(stm: AstStm.SET_ARRAY_LITERALS) // Use typedarrays
	//ARRAY.prototype['setTypedArraySlice'] = function(startIndex, array) {
	//	this.data.set(array, startIndex);
	//};

	// @TODO: Check performance
	//ARRAY.prototype['setArraySlice'] = function(startIndex, array) {
	//	this.data.set(new type(array), startIndex);
	//};

	__addArrayJavaMethods(ARRAY);

	return ARRAY;
}

function __createGenericArrayType() {
	var ARRAY = function(size, desc) {
		this.desc = desc;
		this.data = new Array(size);
		this.length = size;
		for (var n = 0; n < size; n++) this.data[n] = null;
	};

	ARRAY.prototype = Object.create(JA_0.prototype);
	ARRAY.prototype.constructor = ARRAY;

	ARRAY.copyOfRange = function(jarray, start, end, desc) {
		if (desc === undefined) desc = jarray.desc;
		var size = end - start;
		var out = new ARRAY(size, desc);
		var outData = out.data;
		var jarrayData = jarray.data;
		for (var n = 0; n < size; n++) outData[n] = jarrayData[start + n];
		return out;
	};

	ARRAY.fromArray = function(array, desc) {
		if (array == null) return null;
		var out = new JA_L(array.length, desc);
		for (var n = 0; n < out.length; n++) out.set(n, array[n]);
		return out;
	};

	ARRAY.fromArrayOrEmpty = function(array, desc) {
		return ARRAY.fromArray(array ? array : [], desc);
	};

	ARRAY.prototype.get = function(index) {
		return this.data[index];
	};

	ARRAY.prototype.set = function(index, value) {
		this.data[index] = value;
	};

	ARRAY.prototype.clone = function() {
		var out = new JA_L(this.length, this.desc);
		for (var n = 0; n < this.length; n++) out.set(n, this.get(n));
		return out;
	};

	ARRAY.prototype.toArray = function() {
		return this.data;
	};

	__addArrayJavaMethods(ARRAY);

	return ARRAY;
}

function __createJavaArrays() {
	JA_0 = __createJavaArrayBaseType();
	JA_Z = __createJavaArrayType('[Z', Int8Array, 1);    // Bool Array
	JA_B = __createJavaArrayType('[B', Int8Array, 1);    // Byte Array
	JA_C = __createJavaArrayType('[C', Uint16Array, 2);  // Character Array
	JA_S = __createJavaArrayType('[S', Int16Array, 2);   // Short Array
	JA_I = __createJavaArrayType('[I', Int32Array, 4);   // Int Array
	JA_J = __createJavaArrayType('[J', Array);        // Long Array
	JA_F = __createJavaArrayType('[F', Float32Array, 4); // Float Array
	JA_D = __createJavaArrayType('[D', Float64Array, 8); // Double Array

	JA_L =__createGenericArrayType(); // Generic Array

	JA_L.createMultiSure = function(sizes, desc) {
		if (!desc.startsWith('[')) return null;
		if (sizes.length == 1) return JA_L.create(sizes[0], desc);
		var out = new JA_L(sizes[0], desc);
		var sizes2 = sizes.slice(1);
		var desc2 = desc.substr(1);
		for (var n = 0; n < out.length; n++) {
			out.set(n, JA_L.createMultiSure(sizes2, desc2));
		}
		return out;
	};

	 JA_L.create = function(size, desc) {
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
	};

	JA_L.fromArray1 = function(items, desc) {
		if (items == null) return null;
		var out = JA_L.create(items.length, desc);
		for (var n = 0; n < items.length; n++) out.set(n, items[n]);
		return out;
	}

	JA_L.fromArray2 = function(items, desc) {
		if (items == null) return null;
		var out = new JA_L(items.length, desc);
		for (var n = 0; n < items.length; n++) out.set(n, JA_L.fromArray1(items[n], desc.substr(1)));
		return out;
	};
}

var N = function() {
};

var __reints = (function() {
	var buffer = new ArrayBuffer(8);
	var doubleArray = new Float64Array(buffer);
	var floatArray = new Float32Array(buffer);
    var intArray = new Int32Array(buffer);
    return {
    	intBitsToFloat: function(v) {
    		intArray[0] = v;
    		return floatArray[0];
    	},
    	floatToIntBits: function(v) {
    		floatArray[0] = v;
    		return intArray[0];
    	},
    	doubleToLongBits: function(v) {
    		doubleArray[0] = v;
    		return N.lnew(intArray[1], intArray[0]);
    	},
		longBitsToDouble: function(v) {
			intArray[0] = N.llow(v);
			intArray[1] = N.lhigh(v);
			return doubleArray[0];
		},
		isLittleEndian: function() {
           return new Int16Array(new Uint8Array([1,0]).buffer)[0] == 1;
		}
    };
})();

N.MIN_INT32 = -2147483648;

N.isLittleEndian = __reints.isLittleEndian();
N.intBitsToFloat = __reints.intBitsToFloat;
N.floatToIntBits = __reints.floatToIntBits;
N.doubleToLongBits = __reints.doubleToLongBits;
N.longBitsToDouble = __reints.longBitsToDouble;

N.i = function(v) { return v | 0; }

N.z2i = function(v) { return v | 0; }

///////////////////////
// Conversions
///////////////////////
N.i2z = function(v) { return v != 0; }
N.i2b = function(v) { return ((v << 24) >> 24); }
N.i2s = function(v) { return ((v << 16) >> 16); }
N.i2c = function(v) { return v & 0xFFFF; }
N.i2i = function(v) { return v | 0; }
N.i2j = function(v) { return Int64.ofInt(v); }
N.i2f = function(v) { return +v; }
N.i2d = function(v) { return +v; }
N.f2j = function(v) { return Int64.ofFloat(v); }
N.d2j = function(v) { return Int64.ofFloat(v); }


///////////////////////
// Integer
///////////////////////
N.ishl = function(a, b) { return (a << b) | 0; };
N.ishr = function(a, b) { return (a >> b) | 0; };
N.iushr = function(a, b) { return (a >>> b) | 0; };

N.idiv = function(a, b) { return Math.floor(a / b) | 0; };
N.irem = function(a, b) { return (a % b) | 0; };

///////////////////////
// Long
///////////////////////
N.linit = function() {
};
N.lnew = function(high, low) { return Int64.make(high, low); };
N.lnewFloat = function(float) { return Int64.ofFloat(float); };
N.ltoFloat = function(v) { return Int64.toFloat(v); };
N.llow  = function(v) { return v.low; }
N.lhigh = function(v) { return v.high; }
N.ladd  = function(a, b) { return Int64.add(a, b); }
N.lsub  = function(a, b) { return Int64.sub(a, b); }
N.lmul  = function(a, b) { return Int64.mul(a, b); }
N.ldiv  = function(a, b) { return Int64.div(a, b); }
N.lrem  = function(a, b) { return Int64.rem(a, b); }
N.llcmp = function(a, b) { return Int64.compare(a, b); } // Deprecated
N.lcmp  = function(a, b) { return Int64.compare(a, b); }
N.lxor  = function(a, b) { return Int64.xor(a, b); }
N.land  = function(a, b) { return Int64.and(a, b); }
N.lor   = function(a, b) { return Int64.or(a, b); }
N.lshl  = function(a, b) { return Int64.shl(a, b); }
N.lshr  = function(a, b) { return Int64.shr(a, b); }
N.lushr = function(a, b) { return Int64.ushr(a, b); }
N.lneg  = function(a) { return Int64.neg(a); }
N.linv  = function(a) { return Int64.not(a); }

N.l2i   = function(v) { return Int64.toInt(v); }
N.l2f   = function(v) { return Int64.toFloat(v); }
N.l2d   = function(v) { return Int64.toFloat(v); }

N.cmp  = function(a, b) { return (a < b) ? -1 : ((a > b) ? 1 : 0); }
N.cmpl = function(a, b) { return (isNaN(a) || isNaN(b)) ? -1 : N.cmp(a, b); }
N.cmpg = function(a, b) { return (isNaN(a) || isNaN(b)) ? 1 : N.cmp(a, b); }


N.getTime = function() { return Date.now(); };
N.hrtime = function() {
	if (onBrowser) {
		if (typeof performance != 'undefined') {
			return N.lnewFloat(performance.now() * 1000000.0);
		} else {
			return N.lmul(N.lnewFloat(Date.now()), N.i2j(1000000));
		}
	} else if (onNodeJs) {
		var hr = process.hrtime()
		return N.ladd(N.lmul(N.i2j(hr[0]), N.i2j(1000000000)), N.i2j(hr[1]));
	} else {
		throw 'Unsupported high resolution time';
	}
};

// @TODO: optimize this again!
N.is = function(i, clazz) {
	if (i instanceof clazz) return true;
	if (i == null) return false;
	if (typeof i.$JS$CLASS_ID$ === 'undefined') return false;
	return (typeof clazz.$$instanceOf[i.$JS$CLASS_ID$] !== "undefined");
};

N.isClassId = function(i, classId) {
	if (i == null) return false;
	if (!i.$$CLASS_IDS) return false;
	return i.$$CLASS_IDS.indexOf(classId) >= 0;
};

N.istr = function(str) {
	if (str == null) return null;
	if (str instanceof {% CLASS java.lang.String %}) return str._str;
	return '' + str;
}

N.ichar = function(i) {
	return String.fromCharCode(i);
}

N.str = function(str) {
	if (str == null) return null;
	if (str instanceof {% CLASS java.lang.String %}) return str;
	var out = new {% CLASS java.lang.String %}();
	out._str = '' + str;
	return out;
}

N.strLit = function(str) {
	// Check cache!
	return N.str(str);
};

N.strLitEscape = function(str) {
	// Check cache!
	return str;
};

N.strArray = function(strs) {
	if (strs == null) return null;
	var out = new JA_L(strs.length, '[Ljava/lang/String;');
	for (var n = 0; n < strs.length; n++) {
		out.set(n, N.str(strs[n]));
	}
	return out;
};

N.strArrayOrEmpty = function(strs) {
	var out = N.strArray(strs);
	return out ? out : [];
};

N.istrArray = function(strs) {
	if (strs == null) return null;
	return strs.data.map(function(s) { return N.istr(s); });
};

N.iteratorToArray = function(it) {
	if (it == null) return null;
	var out = [];
	while (it["{% METHOD java.util.Iterator:hasNext:()Z %}"]()) {
		out.push(it["{% METHOD java.util.Iterator:next:()Ljava/lang/Object; %}"]());
	}
	return out;
};

N.imap = function(map) {
	if (map == null) return null;
	var obj = {};
	N.iteratorToArray(map["{% METHOD java.util.Map:entrySet %}"]()["{% METHOD java.util.Set:iterator %}"]()).forEach(function(item) {
		var key = item["{% METHOD java.util.Map$Entry:getKey %}"]();
		var value = item["{% METHOD java.util.Map$Entry:getValue %}"]();
		obj[N.unbox(key)] = N.unbox(value);
	});
	return obj;
};

N.args = function() {
	return onNodeJs ? process.argv.slice(2) : [];
};

N.byteArrayToString = N.intArrayToString = N.charArrayToString = function(array, offset, length, encoding) {
	if (offset === undefined) offset = 0;
	if (length === undefined) length = array.length - offset;
	if (encoding === undefined) encoding = 'UTF-8';
	// @TODO: Handle encodings!
	var out = '';
	for (var n = offset; n < offset + length; n++) {
		out += String.fromCharCode(array.get(n));
	}
	return out;
};

N.stringToCharArray = function(str) {
	var out = new JA_C(str.length);
	for (var n = 0; n < str.length; n++) out.set(n, str.charCodeAt(n));
	return out;
};

N.resolveClass = function(name) {
	return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N.str(name));
};

N.createStackTraceElement = function(declaringClass, methodName, fileName, lineNumber) {
	var out = {% CONSTRUCTOR java.lang.StackTraceElement:(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V %}(
		N.str(declaringClass),
		N.str(methodName),
		N.str(fileName),
		lineNumber | 0
	);
	return out;
};

function stackTrace() {
    var err = new Error();
    return err.stack.split('\n').slice(3);
}

N.getStackTrace = function(count) {
	var traces = stackTrace()
	var out = new JA_L(traces.length, '[Ljava/lang/StackTraceElement;');
	for (var n = 0; n < traces.length; n++) {
		out.set(n, N.createStackTraceElement('JS', 'js', traces[n], 0));
	}
	return out;
};

N._arraycopyArray = function(srcData, srcPos, destData, destPos, length, overlapping) {
	if (overlapping) {
		for (var n = length - 1; n >= 0; n--) destData[destPos + n] = srcData[srcPos + n];
	} else {
		for (var n = 0; n < length; n++) destData[destPos + n] = srcData[srcPos + n];
	}
}

N._arraycopyGeneric = function(srcData, srcPos, destData, destPos, length, overlapping) {
	if (overlapping) {
		for (var n = length - 1; n >= 0; n--) destData[destPos + n] = srcData[srcPos + n];
	} else {
		for (var n = 0; n < length; n++) destData[destPos + n] = srcData[srcPos + n];
	}
}

N._arraycopyTyped_B = function(srcData, srcPos, destData, destPos, length, overlapping) {
	destData.set(new Int8Array(srcData.buffer, srcPos * 1, length), destPos);
}

N._arraycopyTyped_C = function(srcData, srcPos, destData, destPos, length, overlapping) {
	destData.set(new Uint16Array(srcData.buffer, srcPos * 2, length), destPos);
}

N._arraycopyTyped_S = function(srcData, srcPos, destData, destPos, length, overlapping) {
	destData.set(new Int16Array(srcData.buffer, srcPos * 2, length), destPos);
}

N._arraycopyTyped_I = function(srcData, srcPos, destData, destPos, length, overlapping) {
	destData.set(new Int32Array(srcData.buffer, srcPos * 4, length), destPos);
}

N._arraycopyTyped_F = function(srcData, srcPos, destData, destPos, length, overlapping) {
	destData.set(new Float32Array(srcData.buffer, srcPos * 4, length), destPos);
}

N._arraycopyTyped_D = function(srcData, srcPos, destData, destPos, length, overlapping) {
	destData.set(new Float64Array(srcData.buffer, srcPos * 8, length), destPos);
}

N._arraycopyTyped_J = function(srcData, srcPos, destData, destPos, length, overlapping) {
	if (overlapping) {
		for (var n = length - 1; n >= 0; n--) destData[destPos + n] = srcData[srcPos + n];
	} else {
		for (var n = 0; n < length; n++) destData[destPos + n] = srcData[srcPos + n];
	}
}

N.arraycopy = function(src, srcPos, dest, destPos, length) {
	if (length < 0 || srcPos < 0 || destPos < 0 || srcPos + length > src.length || destPos + length > dest.length) {
		N.throwRuntimeException('N.arraycopy out of bounds');
	}

	var srcData = src.data;
	var destData = dest.data;
	var overlapping = src == dest && (destPos > srcPos);

	if (src instanceof JA_L) {
		N._arraycopyArray(srcData, srcPos, destData, destPos, length, overlapping);
	} else if (src instanceof JA_Z) {
		N._arraycopyTyped_B(srcData, srcPos, destData, destPos, length, overlapping);
	} else if (src instanceof JA_B) {
		N._arraycopyTyped_B(srcData, srcPos, destData, destPos, length, overlapping);
	} else if (src instanceof JA_S) {
		N._arraycopyTyped_S(srcData, srcPos, destData, destPos, length, overlapping);
	} else if (src instanceof JA_C) {
		N._arraycopyTyped_C(srcData, srcPos, destData, destPos, length, overlapping);
	} else if (src instanceof JA_I) {
		N._arraycopyTyped_I(srcData, srcPos, destData, destPos, length, overlapping);
	} else if (src instanceof JA_F) {
		N._arraycopyTyped_F(srcData, srcPos, destData, destPos, length, overlapping);
	} else if (src instanceof JA_D) {
		N._arraycopyTyped_D(srcData, srcPos, destData, destPos, length, overlapping);
	} else if (src instanceof JA_J) {
		N._arraycopyTyped_J(srcData, srcPos, destData, destPos, length, overlapping);
	} else {
		N._arraycopyGeneric(srcData, srcPos, destData, destPos, length, overlapping);
	}
};

N.isInstanceOfClass = function(obj, javaClass) {
	if (obj == null) return false;
	if (javaClass == null) return false;
	var clazz = jtranscClasses[N.istr(javaClass._name)];
	if (clazz == null) return false;
	return N.is(obj, clazz);
};

N.identityHashCode = function(p0) {
	return (p0 != null) ? p0.$JS$ID$ : 0;
};

N.fillSecureRandomBytes = function(array) {
	var buf;

	if (onNodeJs) {
		buf = require('crypto').randomBytes(256);
	} else {
		buf = new Uint8Array(array.length);
		window.crypto.getRandomValues(buf);
	}

	for (var n = 0; n < array.length; n++) array.set(n, buf[n]);
};

N.boxVoid = function(value) { return null; }
N.boxBool = function(value) { return {% SMETHOD java.lang.Boolean:valueOf:(Z)Ljava/lang/Boolean; %}(value); }
N.boxByte = function(value) { return {% SMETHOD java.lang.Byte:valueOf:(B)Ljava/lang/Byte; %}(value); }
N.boxShort = function(value) { return {% SMETHOD java.lang.Short:valueOf:(S)Ljava/lang/Short; %}(value); }
N.boxChar = function(value) { return {% SMETHOD java.lang.Character:valueOf:(C)Ljava/lang/Character; %}(value); }
N.boxInt = function(value) { return {% SMETHOD java.lang.Integer:valueOf:(I)Ljava/lang/Integer; %}(value); }
N.boxLong = function(value) { return {% SMETHOD java.lang.Long:valueOf:(J)Ljava/lang/Long; %}(value); }
N.boxFloat = function(value) { return {% SMETHOD java.lang.Float:valueOf:(F)Ljava/lang/Float; %}(value); }
N.boxDouble = function(value) { return {% SMETHOD java.lang.Double:valueOf:(D)Ljava/lang/Double; %}(value); }
N.boxString = function(value) { return (value != null) ? N.str(value) : null; }
N.boxWrapped = function(value) { return N.wrap(value); }

N.unboxVoid      = function(value) { return null; }
N.unboxBool      = function(value) { return value["{% FIELD java.lang.Boolean:value:Z %}"]; }
N.unboxByte      = function(value) { return value["{% FIELD java.lang.Byte:value:B %}"]; }
N.unboxShort     = function(value) { return value["{% FIELD java.lang.Short:value:S %}"]; }
N.unboxChar      = function(value) { return value["{% FIELD java.lang.Character:value:C %}"]; }
N.unboxInt       = function(value) { return value["{% FIELD java.lang.Integer:value:I %}"]; }
N.unboxLong      = function(value) { return value["{% FIELD java.lang.Long:value:J %}"]; }
N.unboxFloat     = function(value) { return value["{% FIELD java.lang.Float:value:F %}"]; }
N.unboxDouble    = function(value) { return value["{% FIELD java.lang.Double:value:D %}"]; }
N.unboxString    = function(value) { return N.istr(value); }
N.unboxWrapped   = function(value) { return value._wrapped; }

N.unboxByteArray = function(value) {
	return value.data;
};

N.unbox = function(value, throwOnInvalid) {
	if (N.is(value, {% CLASS java.lang.Boolean %})) return N.unboxBool(value);
	if (N.is(value, {% CLASS java.lang.Byte %})) return N.unboxByte(value);
	if (N.is(value, {% CLASS java.lang.Short %})) return N.unboxShort(value);
	if (N.is(value, {% CLASS java.lang.Character %})) return N.unboxChar(value);
	if (N.is(value, {% CLASS java.lang.Integer %})) return N.unboxInt(value);
	if (N.is(value, {% CLASS java.lang.Long %})) return N.unboxLong(value);
	if (N.is(value, {% CLASS java.lang.Float %})) return N.unboxFloat(value);
	if (N.is(value, {% CLASS java.lang.Double %})) return N.unboxDouble(value);
	if (N.is(value, {% CLASS java.lang.String %})) return N.unboxString(value);
	if (value instanceof JA_B) return N.unboxByteArray(value);
	if (N.is(value, {% CLASS com.jtransc.JTranscWrapped %})) return N.unboxWrapped(value);
	if (throwOnInvalid) throw 'Was not able to unbox "' + value + '"';
	return value;
}

N.wrap = function(value) {
	var out = new {% CLASS com.jtransc.JTranscWrapped %}();
	out._wrapped = value;
	return out;
}

N.createRuntimeException = function(msg) {
	return {% CONSTRUCTOR java.lang.RuntimeException:(Ljava/lang/String;)V %}(N.str(msg));
};

N.throwRuntimeException = function(msg) {
	throw N.createRuntimeException(msg);
	//throw msg;
};

N.boxWithType = function(clazz, value) {
	if (value instanceof JA_0) return value;
	if (value instanceof {% CLASS java.lang.Object %}) return value;

	var clazzName = N.istr(clazz['{% FIELD java.lang.Class:name %}']);

	switch (clazzName) {
		case 'void'   : return N.boxVoid();
		case 'boolean': return N.boxBool(value);
		case 'byte'   : return N.boxByte(value);
		case 'short'  : return N.boxShort(value);
		case 'char'   : return N.boxChar(value);
		case 'int'    : return N.boxInt(value);
		case 'long'   : return N.boxLong(value);
		case 'float'  : return N.boxFloat(value);
		case 'double' : return N.boxDouble(value);
	}

	console.log("WARNING: Don't know how to unbox class '" + clazzName + "' with value '" + value + "'", value);
	return value;
};

N.unboxWithTypeWhenRequired = function(clazz, value) {
	var clazzName = N.istr(clazz['{% FIELD java.lang.Class:name %}']);

	switch (clazzName) {
		case 'void'   :
		case 'boolean':
		case 'byte'   :
		case 'short'  :
		case 'char'   :
		case 'int'    :
		case 'long'   :
		case 'float'  :
		case 'double' :
			return N.unbox(value);
	}

	return value;
};

N.unboxArray = function(array) {
	return array.map(function(it) { return N.unbox(it); });
};

N.boxArray = function(array) {
	return JA_L.fromArray(array.map(function(it) { return N.box(it); }));
};

N.box = function(v) {
	if (v instanceof {% CLASS java.lang.Object %}) return v; // already boxed!
	if (v instanceof Int64) return N.boxLong(v);
	if (typeof v == 'string') return N.str(v);
	if ((v|0) == v) return N.boxInt(v);
	if (+(v) == v) return N.boxFloat(v);
	if ((v == null) || N.is(v, {% CLASS java.lang.Object %})) return v;
	return N.wrap(v);
};

N.isNegativeZero = function(x) {
	return x === 0 && 1 / x === -Infinity;
};

N.sort = function(array, start, end, comparator) {
	var slice = array.slice(start, end);
	if (comparator === undefined) {
		slice.sort();
	} else {
		slice.sort(function(a, b) {
			return comparator["{% METHOD java.util.Comparator:compare:(Ljava/lang/Object;Ljava/lang/Object;)I %}"](a, b);
		});
	}
	for (var n = 0; n < slice.length; n++) array[start + n] = slice[n];
};

N.getByteArray = function(v) {
	if (v instanceof JA_B) return v;
	var length = v.byteLength || v.length
	if (v.buffer) v = v.buffer;
	var out = new JA_B(length);
	out.data = new Int8Array(v);
	return out;
};

N.clone = function(obj) {
	if (obj == null) return null;
	var temp = Object.create(obj);
	temp['{% FIELD java.lang.Object:$$id %}'] = 0;
	return temp;
};

N.methodWithoutBody = function(name) {
	throw 'Method not implemented: native or abstract: ' + name;
};

N.EMPTY_FUNCTION = function() { }

var java_lang_Object_base = function() { };
java_lang_Object_base.prototype.toString = function() {
	return this ? N.istr(this['{% METHOD java.lang.Object:toString %}']()) : null;
};

/* ## BODY ## */

})(_global);