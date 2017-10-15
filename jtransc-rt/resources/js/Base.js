var _global = (typeof window !== "undefined") ? window : global;

var DEBUG_VERSION = {{ debug != false }};

if ('á'.charCodeAt(0) != 225) {
	throw new Error('Encoding must be UTF-8. Please add <META http-equiv="Content-Type" content="text/html; charset=utf-8" /> to the html');
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
	for (;k < len; ++k) if (searchElement === O[k]) return true;
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
		++k;
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

class Int32 {
	constructor(value) {
		this.value = value | 0;
	}

	static compare(a, b) {
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

	static ucompare(a, b) {
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

	static mul(a, b) { return Math.imul(a, b); }
}


class Int64 {
	constructor(high, low) {
		this.high = high | 0;
		this.low = low | 0;
	}

	static is(value) { return value instanceof Int64; };
	static make(high, low) {
		if (high == 0) {
			if (low == 0) return Int64.zero;
			if (low == 1) return Int64.one;
		}
		return new Int64(high, low);
	};
	static ofInt(value) { return Int64.make(value >> 31, value | 0); };
	static ofFloat(f) {
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
			++i;
		}

		return neg ? Int64.neg(result) : result;
	};

	toString() {
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

	static toInt(a) { return a.low; };
	static toFloat(v) {
		if (Int64.isNeg(v)) {
			return Int64.eq(v, MIN_INT64) ? Int64.ofFloat(-9223372036854775808.0) : -Int64.toFloat(Int64.neg(v));
		} else {
			var lowf = v.low;
			var highf = v.high;
			return lowf + highf * M2P32_DBL;
		}
	};

	static isNeg(a) { return a.high < 0; };
	static isZero(a) { return a.high == 0 && a.low == 0; };
	static isNotZero(a) { return a.high != 0 || a.low != 0; };

	// Comparisons

	static compare(a, b) {
		var v = a.high - b.high | 0;
		if (v == 0) v = Int32.ucompare(a.low, b.low);
		return (a.high < 0) ? ((b.high < 0) ? v : -1) : ((b.high >= 0) ? v : 1);
	};

	static ucompare(a, b) {
		var v = Int32.ucompare(a.high, b.high);
		return (v != 0) ? v : Int32.ucompare(a.low, b.low);
	};

	static eq (a, b) { return (a.high == b.high) && (a.low == b.low); };
	static ne (a, b) { return (a.high != b.high) || (a.low != b.low); };
	static neq(a, b) { return (a.high != b.high) || (a.low != b.low); };
	static lt (a, b) { return Int64.compare(a, b) < 0; };
	static le (a, b) { return Int64.compare(a, b) <= 0; };
	static gt (a, b) { return Int64.compare(a, b) > 0; };
	static ge (a, b) { return Int64.compare(a, b) >= 0; };

	// Strings

	toString() {
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

	static divMod(dividend, divisor) {
		if(divisor.high == 0) {
			switch(divisor.low) {
			case 0:
				throw new Error("divide by zero");
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

	static neg(x) {
		var high = ~x.high | 0;
		var low = -x.low | 0;
		if(low == 0) high = high + 1 | 0;
		return Int64.make(high,low);
	};

	static add(a, b) {
		var high = a.high + b.high | 0;
		var low = a.low + b.low | 0;
		if(Int32.ucompare(low,a.low) < 0) {
			high = high + 1 | 0;
		}
		return Int64.make(high,low);
	};

	static sub(a, b) {
		var high = a.high - b.high | 0;
		var low = a.low - b.low | 0;
		if(Int32.ucompare(a.low,b.low) < 0) {
			high = high - 1 | 0;
		}
		return Int64.make(high,low);
	};

	static mul(a, b) {
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

	static div(a, b) { return Int64.divMod(a, b).quotient; };
	static mod(a, b) { return Int64.divMod(a, b).modulus; };
	static rem(a, b) { return Int64.divMod(a, b).modulus; };

	// BIT-WISE
	static not(x) { return Int64.make(~x.high, ~x.low); }
	static and(a, b) { return Int64.make(a.high & b.high, a.low & b.low); }
	static or(a, b) { return Int64.make(a.high | b.high, a.low | b.low); }
	static xor(a, b) { return Int64.make(a.high ^ b.high, a.low ^ b.low); }
	static shl(a, b) {
		b &= 63;
		if(b == 0) {
			return Int64.make(a.high,a.low);
		} else if(b < 32) {
			return Int64.make(a.high << b | a.low >>> 32 - b,a.low << b);
		} else {
			return Int64.make(a.low << b - 32,0);
		}
	}
	static shr(a, b) {
		b &= 63;
		if(b == 0) {
			return Int64.make(a.high,a.low);
		} else if(b < 32) {
			return Int64.make(a.high >> b,a.high << 32 - b | a.low >>> b);
		} else {
			return Int64.make(a.high >> 31,a.high >> b - 32);
		}
	}
	static ushr(a, b) {
		b &= 63;
		if(b == 0) {
			return Int64.make(a.high,a.low);
		} else if(b < 32) {
			return Int64.make(a.high >>> b,a.high << 32 - b | a.low >>> b);
		} else {
			return Int64.make(0,a.high >>> b - 32);
		}
	}

	static sign(a) {
		if (Int64.isNeg(a)) return -1;
		if (Int64.isNotZero(a)) return +1;
		return 0;
	};

	static abs(a) {
		return (Int64.sign(a) < 0) ? Int64.neg(a) : a;
	};

}

// Building
var M2P32_DBL = Math.pow(2, 32);
var MAX_INT64 = new Int64(0x7FFFFFFF, 0xFFFFFFFF);
var MIN_INT64 = new Int64(0x80000000, 0x00000000);

Int64.zero = new Int64(0, 0);
Int64.one = new Int64(0, 1);
Int64.MIN_VALUE = MIN_INT64;
Int64.MAX_VALUE = MAX_INT64;


////////////////////////////////////////////////////////////////////////////

var S = [];
var SS = [];

function __buildStrings() {
	var len = SS.length
	S.length = len;
	for (var n = 0; n < len; ++n) S[n] = N.str(SS[n]);
}

var JA_0, JA_Z, JA_B, JA_C, JA_S, JA_I, JA_J, JA_F, JA_D, JA_L;

function __createJavaArrayBaseType() {
	var ARRAY = function() {
	};

	ARRAY.prototype = Object.create({% CLASS java.lang.Object %}.prototype);
	ARRAY.prototype.constructor = ARRAY;

	ARRAY.prototype{% IMETHOD java.lang.Object:getClass %} = function(_jc) {
		return N.resolveClass(_jc, this.desc);
	};

	ARRAY.prototype['setArraySlice'] = function(startIndex, array) {
		var len = array.length;
		for (var n = 0; n < len; ++n) this.data[startIndex + n] = array[n];
	};


	return ARRAY;
}

function __addArrayJavaMethods(ARRAY) {
	ARRAY.prototype{% IMETHOD java.lang.Object:clone %} = ARRAY.prototype.clone;

	ARRAY.prototype{% IMETHOD java.lang.Object:getClass %} = function(_jc) {
		return N.resolveClass(_jc, this.desc);
	};

	ARRAY.prototype{% IMETHOD java.lang.Object:toString %} = function(_jc) {
		return N.str('ARRAY(' + this.desc + ')');
	};
}

function __createJavaArrayGetSet(ARRAY) {
	ARRAY.prototype.checkIndex = function(index) {
		if (index < 0 || index >= this.data.length) {
			N.throwRuntimeException('Out of bounds ' + index + " !in [0, " + this.data.length + "]");
		}
	};

	if (DEBUG_VERSION) {
		ARRAY.prototype.get = function(index) {
			this.checkIndex(index);
			return this.data[index];
		};
		ARRAY.prototype.set = function(index, value) {
			this.checkIndex(index);
			this.data[index] = value;
		};
	} else {
		ARRAY.prototype.get = function(index) { return this.data[index]; };
		ARRAY.prototype.set = function(index, value) { this.data[index] = value; };
	}
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
			for (var n = 0; n < this.length; ++n) this.set(n, zero);
		};
		ARRAY.prototype.clone = function() {
			var out = new ARRAY(this.length);
			for (var n = 0; n < this.length; ++n) out.set(n, this.get(n));
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

	__createJavaArrayGetSet(ARRAY);

	ARRAY.prototype.getBuffer = function() {
		return this.data.buffer;
	};

	ARRAY.prototype.toArray = function() {
    	var out = new Array(this.length);
    	for (var n = 0; n < out.length; ++n) out[n] = this.get(n);
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

	//JA_0 dest, int srcPos, int destPos, int length
	if (desc == '[J') {
		ARRAY.prototype.copyTo = function(dest, srcPos, destPos, length, overlapping) {
			var srcData = this.data;
			var destData = dest.data;
			if (overlapping) {
				for (var n = length - 1; n >= 0; n--) destData[destPos + n] = srcData[srcPos + n];
			} else {
				for (var n = 0; n < length; ++n) destData[destPos + n] = srcData[srcPos + n];
			}
		};
	} else {
		ARRAY.prototype.copyTo = function(dest, srcPos, destPos, length, overlapping) {
			dest.data.set(new type(this.data.buffer, srcPos * elementBytesSize, length), destPos);
		};
	}

	__addArrayJavaMethods(ARRAY);

	return ARRAY;
}

function __createGenericArrayType() {
	var ARRAY = function(size, desc) {
		this.desc = desc;
		this.data = new Array(size);
		this.length = size;
		for (var n = 0; n < size; ++n) this.data[n] = null;
	};

	ARRAY.prototype = Object.create(JA_0.prototype);
	ARRAY.prototype.constructor = ARRAY;

	ARRAY.copyOfRange = function(jarray, start, end, desc) {
		if (desc === undefined) desc = jarray.desc;
		var size = end - start;
		var out = new ARRAY(size, desc);
		var outData = out.data;
		var jarrayData = jarray.data;
		for (var n = 0; n < size; ++n) outData[n] = jarrayData[start + n];
		return out;
	};

	ARRAY.fromArray = function(desc, array) {
		if (array == null) return null;
		var out = new JA_L(array.length, desc);
		for (var n = 0; n < out.length; ++n) out.set(n, array[n]);
		return out;
	};

	ARRAY.T0 = function(desc) { return this.fromArray(desc, []); }
	ARRAY.T1 = function(desc, a) { return this.fromArray(desc, [a]); }
	ARRAY.T2 = function(desc, a, b) { return this.fromArray(desc, [a, b]); }
	ARRAY.T3 = function(desc, a, b, c) { return this.fromArray(desc, [a, b, c]); }
	ARRAY.T4 = function(desc, a, b, c, d) { return this.fromArray(desc, [a, b, c, d]); }

	__createJavaArrayGetSet(ARRAY);

	ARRAY.prototype.clone = function() {
		var out = new JA_L(this.length, this.desc);
		for (var n = 0; n < this.length; ++n) out.set(n, this.get(n));
		return out;
	};

	ARRAY.prototype.toArray = function() {
		return this.data;
	};

	//JA_0 dest, int srcPos, int destPos, int length
	ARRAY.prototype.copyTo = function(dest, srcPos, destPos, length, overlapping) {
		var srcData = this.data;
		var destData = dest.data;
		if (overlapping) {
			for (var n = length - 1; n >= 0; n--) destData[destPos + n] = srcData[srcPos + n];
		} else {
			for (var n = 0; n < length; ++n) destData[destPos + n] = srcData[srcPos + n];
		}
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
	JA_F = __createJavaArrayType('[F', Float32Array, 4); // Float Array
	JA_D = __createJavaArrayType('[D', Float64Array, 8); // Double Array

	// Specially handled
	JA_J = __createJavaArrayType('[J', Array, 1);        // Long Array

	JA_L =__createGenericArrayType(); // Generic Array

	JA_L.createMultiSure = function(sizes, desc) {
		if (!desc.startsWith('[')) return null;
		if (sizes.length == 1) return JA_L.create(sizes[0], desc);
		var out = new JA_L(sizes[0], desc);
		var sizes2 = sizes.slice(1);
		var desc2 = desc.substr(1);
		for (var n = 0; n < out.length; ++n) {
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
		for (var n = 0; n < items.length; ++n) out.set(n, items[n]);
		return out;
	}

	JA_L.fromArray2 = function(items, desc) {
		if (items == null) return null;
		var out = new JA_L(items.length, desc);
		for (var n = 0; n < items.length; ++n) out.set(n, JA_L.fromArray1(items[n], desc.substr(1)));
		return out;
	};
}

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

class N {
	static i(v) { return v | 0; }

	static z2i(v) { return v | 0; }

	///////////////////////
	// Conversions
	///////////////////////
	static i2z(v) { return v != 0; }
	static i2b(v) { return ((v << 24) >> 24); }
	static i2s(v) { return ((v << 16) >> 16); }
	static i2c(v) { return v & 0xFFFF; }
	static i2i(v) { return v | 0; }
	static i2j(v) { return Int64.ofInt(v); }
	static i2f(v) { return +v; }
	static i2d(v) { return +v; }
	static d2j(v) {
		if (isFinite(v)) {
			return Int64.ofFloat(v);
		} else {
			if (isNaN(v)) {
				return Int64.zero;
			} else if (v >= 0) {
				return MAX_INT64;
			} else {
				return MIN_INT64;
			}
		}
	}
	static d2i(v) {
		if (isFinite(v)) {
			return v | 0;
		} else {
			if (isNaN(v)) {
				return 0;
			} else if (v >= 0) {
				return 2147483647;
			} else {
				return -2147483648;
			}
		}
	}

	static f2j(v) { return N.d2j(v); }
	static f2i(v) { return N.d2i(v); }

	///////////////////////
	// Integer
	///////////////////////
	static ishl(a, b) { return (a << b) | 0; };
	static ishr(a, b) { return (a >> b) | 0; };
	static iushr(a, b) { return (a >>> b) | 0; };

	static idiv(a, b) { return Math.floor(a / b) | 0; };
	static irem(a, b) { return (a % b) | 0; };

	///////////////////////
	// Long
	///////////////////////
	static linit(_jc) {};
	static lnew(high, low) { return Int64.make(high, low); };
	static lnewFloat(v) { return Int64.ofFloat(v); };
	static ltoFloat(v) { return Int64.toFloat(v); };
	static llow (v) { return v.low; }
	static lhigh(v) { return v.high; }
	static ladd (a, b) { return Int64.add(a, b); }
	static lsub (a, b) { return Int64.sub(a, b); }
	static lmul (a, b) { return Int64.mul(a, b); }
	static ldiv (a, b) { return Int64.div(a, b); }
	static lrem (a, b) { return Int64.rem(a, b); }
	static llcmp(a, b) { return Int64.compare(a, b); } // Deprecated
	static lcmp (a, b) { return Int64.compare(a, b); }
	static lxor (a, b) { return Int64.xor(a, b); }
	static land (a, b) { return Int64.and(a, b); }
	static lor  (a, b) { return Int64.or(a, b); }
	static lshl (a, b) { return Int64.shl(a, b); }
	static lshr (a, b) { return Int64.shr(a, b); }
	static lushr(a, b) { return Int64.ushr(a, b); }
	static lneg (a) { return Int64.neg(a); }
	static linv (a) { return Int64.not(a); }

	static j2i(v) { return Int64.toInt(v); }
	static j2f(v) { return Int64.toFloat(v); }
	static j2d(v) { return Int64.toFloat(v); }

	static cmp  (a, b) { return (a < b) ? -1 : ((a > b) ? 1 : 0); }
	static cmpl (a, b) { return (isNaN(a) || isNaN(b)) ? -1 : N.cmp(a, b); }
	static cmpg (a, b) { return (isNaN(a) || isNaN(b)) ? 1 : N.cmp(a, b); }

	static getTime() { return Date.now(); };
	static hrtime() {
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
	static is(i, clazz) {
		if (i instanceof clazz) return true;
		if (i == null) return false;
		if (typeof i.__JT__CLASS_ID === 'undefined') return false;
		return i.__JT__CLASS_IDS.indexOf(clazz.__JT__CLASS_ID) >= 0;
	};

	static checkCast(i, clazz) {
		if (i == null) return null;
		if (clazz === null) throw new Error('Internal error N.checkCast');
		if (!N.is(i, clazz)) {
			//throw NewWrappedError({% CONSTRUCTOR java.lang.ClassCastException:(Ljava/lang/String;)V %}(N.str('Invalid conversion ' + i + ' != ' + clazz)));
			throw NewWrappedError({% CONSTRUCTOR java.lang.ClassCastException:(Ljava/lang/String;)V %}(N.str('Invalid conversion')));
		}
		return i;
	};

	static isClassId(i, classId) {
		if (i == null) return false;
		if (!i.__JT__CLASS_IDS) return false;
		return i.__JT__CLASS_IDS.indexOf(classId) >= 0;
	};

	static istr(str) {
		if (str == null) return null;
		if (str instanceof {% CLASS java.lang.String %}) return str._str;
		return '' + str;
	}

	static ichar(i) {
		return String.fromCharCode(i);
	}

	static str(str) {
		if (str == null) return null;
		if (str instanceof {% CLASS java.lang.String %}) return str;
		var out = new {% CLASS java.lang.String %}();
		out._str = '' + str;
		return out;
	}

	static strLit(str) {
		// Check cache!
		return N.str(str);
	};

	static strLitEscape(str) {
		// Check cache!
		return str;
	};

	static strArray(strs) {
		if (strs == null) return null;
		var out = new JA_L(strs.length, '[Ljava/lang/String;');
		for (var n = 0; n < strs.length; ++n) {
			out.set(n, N.str(strs[n]));
		}
		return out;
	};

	static strArrayOrEmpty(strs) {
		var out = N.strArray(strs);
		return out ? out : [];
	};

	static istrArray(strs) {
		if (strs == null) return null;
		return strs.data.map(function(s) { return N.istr(s); });
	};

	static async iteratorToArray(_jc, it) {
		if (it == null) return null;
		var out = [];
		while (await it{% IMETHOD java.util.Iterator:hasNext:()Z %}(_jc)) {
			out.push(await it{% IMETHOD java.util.Iterator:next:()Ljava/lang/Object; %}(_jc));
		}
		return out;
	};

	static async imap(_jc, map) {
		if (map == null) return null;
		var obj = {};
		let array = await(N.iteratorToArray(_jc, await await map{% IMETHOD java.util.Map:entrySet %}(){% IMETHOD java.util.Set:iterator %}(_jc)))

		for (let n = 0; n < array.length; n++) {
			let item = array[n];
			var key = await item{% IMETHOD java.util.Map$Entry:getKey %}(_jc);
			var value = await item{% IMETHOD java.util.Map$Entry:getValue %}(_jc);
			obj[N.unbox(key)] = N.unbox(value);
		}
		return obj;
	};

	static args() {
		return onNodeJs ? process.argv.slice(2) : [];
	};

	static byteArrayToString(array, offset, length, encoding) {
		if (offset === undefined) offset = 0;
		if (length === undefined) length = array.length - offset;
		if (encoding === undefined) encoding = 'UTF-8';
		// @TODO: Handle encodings!
		var out = '';
		for (var n = offset; n < offset + length; ++n) {
			out += String.fromCharCode(array.get(n));
		}
		return out;
	};

	static intArrayToString(array, offset, length, encoding) {
		return N.byteArrayToString(array, offset, length, encoding);
	}

	static charArrayToString(array, offset, length, encoding) {
		return N.byteArrayToString(array, offset, length, encoding);
	}

	static stringToCharArray(str) {
		var out = new JA_C(str.length);
		for (var n = 0; n < str.length; ++n) out.set(n, str.charCodeAt(n));
		return out;
	};

	// @TODO: Make this sync
	static resolveClass(_jc, name) {
		return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(_jc, N.str(name));
	};

	static createStackTraceElement(declaringClass, methodName, fileName, lineNumber) {
		var out = {% CONSTRUCTOR java.lang.StackTraceElement:(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V %}(
			N.str(declaringClass),
			N.str(methodName),
			N.str(fileName),
			lineNumber | 0
		);
		return out;
	};

	static getStackTrace(error, count) {
		//var traces = stackTrace()
		var traces = error.stack.split('\n').slice(count);
		var out = new JA_L(traces.length, '[Ljava/lang/StackTraceElement;');
		for (var n = 0; n < traces.length; ++n) {
			out.set(n, N.createStackTraceElement('JS', 'js', traces[n], 0));
		}
		return out;
	};

	static arraycopy(src, srcPos, dest, destPos, length) {
		//if (length < 0 || srcPos < 0 || destPos < 0 || srcPos + length > src.length || destPos + length > dest.length) N.throwRuntimeException('N.arraycopy out of bounds');
		var overlapping = src == dest && (destPos > srcPos);
		src.copyTo(dest, srcPos, destPos, length, overlapping);
	};

	static isInstanceOfClass(obj, javaClass) {
		if (obj == null) return false;
		if (javaClass == null) return false;
		var clazz = jtranscClasses[N.istr(javaClass._name)];
		if (clazz == null) return false;
		return N.is(obj, clazz);
	};

	static identityHashCode(p0) {
		return (p0 != null) ? p0.$JS$ID$ : 0;
	};

	static fillSecureRandomBytes(array) {
		var buf;

		if (onNodeJs) {
			buf = require('crypto').randomBytes(256);
		} else {
			buf = new Uint8Array(array.length);
			window.crypto.getRandomValues(buf);
		}

		for (var n = 0; n < array.length; ++n) array.set(n, buf[n]);
	};

	static boxVoid    (_jc, value) { return null; }
	static boxBool    (_jc, value) { return {% SMETHOD java.lang.Boolean:valueOf:(Z)Ljava/lang/Boolean; %}(_jc, value); }
	static boxByte    (_jc, value) { return {% SMETHOD java.lang.Byte:valueOf:(B)Ljava/lang/Byte; %}(_jc, value); }
	static boxShort   (_jc, value) { return {% SMETHOD java.lang.Short:valueOf:(S)Ljava/lang/Short; %}(_jc, value); }
	static boxChar    (_jc, value) { return {% SMETHOD java.lang.Character:valueOf:(C)Ljava/lang/Character; %}(_jc, value); }
	static boxInt     (_jc, value) { return {% SMETHOD java.lang.Integer:valueOf:(I)Ljava/lang/Integer; %}(_jc, value); }
	static boxLong    (_jc, value) { return {% SMETHOD java.lang.Long:valueOf:(J)Ljava/lang/Long; %}(_jc, value); }
	static boxFloat   (_jc, value) { return {% SMETHOD java.lang.Float:valueOf:(F)Ljava/lang/Float; %}(_jc, value); }
	static boxDouble  (_jc, value) { return {% SMETHOD java.lang.Double:valueOf:(D)Ljava/lang/Double; %}(_jc, value); }
	static boxString  (_jc, value) { return (value != null) ? N.str(value) : null; }
	static boxWrapped (_jc, value) { return N.wrap(value); }

	static unboxVoid      (value) { return null; }
	static unboxBool      (value) { return value["{% FIELD java.lang.Boolean:value:Z %}"]; }
	static unboxByte      (value) { return value["{% FIELD java.lang.Byte:value:B %}"]; }
	static unboxShort     (value) { return value["{% FIELD java.lang.Short:value:S %}"]; }
	static unboxChar      (value) { return value["{% FIELD java.lang.Character:value:C %}"]; }
	static unboxInt       (value) { return value["{% FIELD java.lang.Integer:value:I %}"]; }
	static unboxLong      (value) { return value["{% FIELD java.lang.Long:value:J %}"]; }
	static unboxFloat     (value) { return value["{% FIELD java.lang.Float:value:F %}"]; }
	static unboxDouble    (value) { return value["{% FIELD java.lang.Double:value:D %}"]; }
	static unboxString    (value) { return N.istr(value); }
	static unboxWrapped   (value) { return value._wrapped; }

	static unboxByteArray(value) {
		return value.data;
	};

	static unbox(value, throwOnInvalid) {
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

	static wrap(value) {
		var out = new {% CLASS com.jtransc.JTranscWrapped %}();
		out._wrapped = value;
		return out;
	}

	static createRuntimeException(_jc, msg) {
		return {% CONSTRUCTOR java.lang.RuntimeException:(Ljava/lang/String;)V %}(_jc, N.str(msg));
	};

	static throwRuntimeException(msg) {
		throw N.createRuntimeException(msg);
		//throw msg;
	};

	static boxWithType(clazz, value) {
		if (value instanceof JA_0) return value;
		if (value instanceof {% CLASS java.lang.Object %}) return value;

		var clazzName = N.istr(clazz{% IFIELD java.lang.Class:name %});

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

	static unboxWithTypeWhenRequired(clazz, value) {
		var clazzName = N.istr(clazz{% IFIELD java.lang.Class:name %});

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

	static unboxArray(array) {
		return array.map(function(it) { return N.unbox(it); });
	};

	static boxArray(array) {
		return JA_L.fromArray(array.map(function(it) { return N.box(it); }));
	};

	static box(_jc, v) {
		if (v instanceof {% CLASS java.lang.Object %}) return v; // already boxed!
		if (v instanceof Int64) return N.boxLong(_jc, v);
		if (typeof v == 'string') return N.str(v);
		if ((v|0) == v) return N.boxInt(_jc, v);
		if (+(v) == v) return N.boxFloat(_jc, v);
		if ((v == null) || N.is(v, {% CLASS java.lang.Object %})) return v;
		return N.wrap(v);
	};

	static isNegativeZero(x) { return x === 0 && 1 / x === -Infinity; };

	//N.sort = async function(array, start, end, comparator) {
	//	var slice = array.slice(start, end);
	//	if (comparator === undefined) {
	//		slice.sort();
	//	} else {
	//		throw 'Unsupported N.sort!';
	//		//slice.sort(async function(a, b) {
	//		//	return await comparator["{% METHOD java.util.Comparator:compare:(Ljava/lang/Object;Ljava/lang/Object;)I %}"](a, b);
	//		//});
	//	}
	//	for (var n = 0; n < slice.length; ++n) array[start + n] = slice[n];
	//};

	static async asyncAsyncStr(_jc, v) {
		if (v == null) return 'null';
		if (typeof v.toStringAsync !== 'undefined') {
			return N.istr(await(v.toStringAsync(_jc)));
		}
		return '' + v;
	};

	static getByteArray(v) {
		if (v instanceof JA_B) return v;
		var length = v.byteLength || v.length
		if (v.buffer) v = v.buffer;
		var out = new JA_B(length);
		out.data = new Int8Array(v);
		return out;
	};

	static clone(obj) {
		if (obj == null) return null;
		var temp = Object.create(obj);
		temp{% IFIELD java.lang.Object:$$id %} = 0;
		return temp;
	};

	static methodWithoutBody(name) {
		throw 'Method not implemented: native or abstract: ' + name;
	};

	static EMPTY_FUNCTION() { }

	static get MIN_INT32() { return -2147483648; }
    static get isLittleEndian() { return __reints.isLittleEndian(); }
    static get intBitsToFloat() { return __reints.intBitsToFloat; }
    static get floatToIntBits() { return __reints.floatToIntBits; }
    static get doubleToLongBits() { return __reints.doubleToLongBits; }
    static get longBitsToDouble() { return __reints.longBitsToDouble; }

    // @TODO: async
    static async monitorEnter(_jc, obj) {
    	if (obj.__jt_mutex__ == null) obj.__jt_mutex__ = new RecursiveMutex();
    	obj.__jt_mutex__.lock(_jc);
    }

    static async monitorExit(_jc, obj) {
    	if (obj.__jt_mutex__ == null) obj.__jt_mutex__ = new RecursiveMutex();
    	obj.__jt_mutex__.unlock(_jc);
    }
} // N

class RecursiveMutex {
	lock(_jc) {
    	//console.log('RecursiveMutex.lock:' + this);
	}

	unlock(_jc) {
    	//console.log('RecursiveMutex.unlock:' + this);
	}
}

function stackTrace() {
	var err = new Error();
	return err.stack.split('\n').slice(3);
}

class java_lang_Object_base {
	constructor() {
	}

	toString() {
		console.error('unsupported use toStringAsync instead:');
		console.error((new Error()).stack);
		return '(' + this.constructor.name + '): unsupported use toStringAsync instead';
	}

	async toStringAsync(_jc) {
		return this ? N.istr(await this{% IMETHOD java.lang.Object:toString %}(_jc)) : null;
	};
}

java_lang_Object_base.prototype.__jt_mutex__ = null;


function WrappedError(javaThrowable) {
	this.constructor.prototype.__proto__ = Error.prototype;
	Error.captureStackTrace(this, this.constructor);
	this.name = this.constructor.name;
	this.javaThrowable = javaThrowable;
	//try {
	//	this.message = (javaThrowable != null) ? (('' + javaThrowable) || 'JavaError') : 'JavaError';
	//} catch (e) {
	this.message = 'JavaErrorWithoutValidMessage';
	//}
}

function NewWrappedError(javaThrowable) {
	return new WrappedError(javaThrowable);
}

//process.on('uncaughtException', function (exception) { console.error(exception); });

/* ## BODY ## */

})(_global);