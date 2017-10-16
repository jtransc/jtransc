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

(function(_global) { "use strict";


//const _global = (typeof window !== "undefined") ? window : global;
const DEBUG_VERSION = {{ debug != false }};
const IS_ASYNC = {{ !!IS_ASYNC }};
const onBrowser = typeof window != "undefined";
const onNodeJs = typeof window == "undefined";

////////////////////////////////////////////////////////////////////////////

class Int32 {
	constructor(value) {
		this.value = value | 0;
	}

	static compare(a, b) {
		a |= 0;
		b |= 0;
		if(a < b) return -1;
		if(a > b) return 1;
		return 0;
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
const M2P32_DBL = Math.pow(2, 32);
const MAX_INT64 = new Int64(0x7FFFFFFF, 0xFFFFFFFF);
const MIN_INT64 = new Int64(0x80000000, 0x00000000);

Int64.zero = new Int64(0, 0);
Int64.one = new Int64(0, 1);
Int64.MIN_VALUE = MIN_INT64;
Int64.MAX_VALUE = MAX_INT64;

////////////////////////////////////////////////////////////////////////////

let S = [];
let SS = [];

function __buildStrings() {
	var len = SS.length
	S.length = len;
	for (var n = 0; n < len; ++n) S[n] = N.str(SS[n]);
}

let JA_0 = null;
let JA_Z = null;
let JA_B = null;
let JA_C = null;
let JA_S = null;
let JA_I = null;
let JA_J = null;
let JA_F = null;
let JA_D = null;
let JA_L = null;

function __createJavaArrayBaseType() {
	class JA_0 extends {% CLASS java.lang.Object %} {
		constructor(size, desc) {
			super();
			this.length = size;
			this.desc = desc;
		}

		getClass({{ JC }}) {
			return N.resolveClass({{ JC_COMMA }}this.desc);
		}

		"{% METHOD java.lang.Object:getClass %}"({{ JC }}) {
			return N.resolveClass({{ JC_COMMA }}this.desc);
		}

		"{% METHOD java.lang.Object:clone %}"() {
			return this.clone();
		}

		"{% METHOD java.lang.Object:getClass %}"({{ JC }}) {
			return N.resolveClass({{ JC_COMMA }}this.desc);
		};

		"{% METHOD java.lang.Object:toString %}"({{ JC }}) {
			return N.str('ARRAY(' + this.desc + ')');
		};
	}

	return JA_0;
}

function __copyOfJA_0() {
	return class extends JA_0 {
		setArraySlice(startIndex, array) {
			var len = array.length;
			for (var n = 0; n < len; ++n) this.data[startIndex + n] = array[n];
		}

		checkIndex(index) {
			if (index < 0 || index >= this.data.length) {
				N.throwRuntimeException('Out of bounds ' + index + " !in [0, " + this.data.length + "]");
			}
		}

		get(index) {
			if (DEBUG_VERSION) this.checkIndex(index);
			return this.data[index];
		}

		set(index, value) {
			if (DEBUG_VERSION) this.checkIndex(index);
			this.data[index] = value;
		}
	};
}

function __createGenericArrayType(classId) {
	class ARRAY extends __copyOfJA_0() {
		constructor(size, desc) {
			super(size, desc);
			this.data = new Array(size);
			for (var n = 0; n < size; ++n) this.data[n] = null;
		}

		static copyOfRange(jarray, start, end, desc) {
			if (desc === undefined) desc = jarray.desc;
			var size = end - start;
			var out = new ARRAY(size, desc);
			var outData = out.data;
			var jarrayData = jarray.data;
			for (var n = 0; n < size; ++n) outData[n] = jarrayData[start + n];
			return out;
		}

		static fromArray(desc, array) {
			if (array == null) return null;
			var out = new JA_L(array.length, desc);
			for (var n = 0; n < out.length; ++n) out.set(n, array[n]);
			return out;
		}

		static T0(desc) { return this.fromArray(desc, []); }
        static T1(desc, a) { return this.fromArray(desc, [a]); }
        static T2(desc, a, b) { return this.fromArray(desc, [a, b]); }
        static T3(desc, a, b, c) { return this.fromArray(desc, [a, b, c]); }
        static T4(desc, a, b, c, d) { return this.fromArray(desc, [a, b, c, d]); }

		clone() {
			var out = new JA_L(this.length, this.desc);
			for (var n = 0; n < this.length; ++n) out.set(n, this.get(n));
			return out;
		}

		toArray() { return this.data; }

		//JA_0 dest, int srcPos, int destPos, int length
		copyTo(dest, srcPos, destPos, length, overlapping) {
			var srcData = this.data;
			var destData = dest.data;
			if (overlapping) {
				for (var n = length - 1; n >= 0; n--) destData[destPos + n] = srcData[srcPos + n];
			} else {
				for (var n = 0; n < length; ++n) destData[destPos + n] = srcData[srcPos + n];
			}
		};

		static createMultiSure(sizes, desc) {
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

		static create(size, desc) {
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

		static fromArray1(items, desc) {
			if (items == null) return null;
			var out = JA_L.create(items.length, desc);
			for (var n = 0; n < items.length; ++n) out.set(n, items[n]);
			return out;
		}

		static fromArray2(items, desc) {
			if (items == null) return null;
			var out = new JA_L(items.length, desc);
			for (var n = 0; n < items.length; ++n) out.set(n, JA_L.fromArray1(items[n], desc.substr(1)));
			return out;
		};
	}

	ARRAY.prototype.__JT__CLASS_ID = ARRAY.__JT__CLASS_ID = classId;
	ARRAY.prototype.__JT__CLASS_IDS = ARRAY.__JT__CLASS_IDS = [classId];

	//ARRAY.constructor.name = "TST";

	return ARRAY;
}

function __createJavaArrayType(classId, desc, type, elementBytesSize) {
	const ELEMENT_BYTES_SIZE = elementBytesSize;
	const TYPE = type;
	const DESC = desc;
	const IS_LONG = (DESC == '[J');

	class ARRAY extends __copyOfJA_0() {
		constructor(size) {
			super(size, DESC);
			this.memorySize = size * ELEMENT_BYTES_SIZE;
			this.data = new TYPE((((this.memorySize + 7) & ~7) / ELEMENT_BYTES_SIZE)|0);

			if (IS_LONG) {
				var zero = N.lnew(0, 0);
				for (var n = 0; n < this.length; ++n) this.set(n, zero);
			}
		}

		clone() {
			var out = new ARRAY(this.length);
			if (IS_LONG) {
				for (var n = 0; n < this.length; ++n) out.set(n, this.get(n));
			} else {
				out.data.set(this.data);
			}
			return out;
		}

		static fromTypedArray(typedArray) {
			var out = new ARRAY(typedArray.length);
			out.data.set(typedArray);
			return out;
		}

		static T(typedArray) {
			return ARRAY.fromTypedArray(typedArray);
		}

		static wrapBuffer(arrayBuffer) {
			var out = new ARRAY(0);
			out.data = new type(arrayBuffer);
			out.length = out.data.length;
			return out;
		}

		getBuffer() {
			return this.data.buffer;
		}

		toArray() {
			var out = new Array(this.length);
			for (var n = 0; n < out.length; ++n) out[n] = this.get(n);
			return out;
		};

		copyTo(dest, srcPos, destPos, length, overlapping) {
			if (IS_LONG) {
				var srcData = this.data;
				var destData = dest.data;
				if (overlapping) {
					for (var n = length - 1; n >= 0; n--) destData[destPos + n] = srcData[srcPos + n];
				} else {
					for (var n = 0; n < length; ++n) destData[destPos + n] = srcData[srcPos + n];
				}
			} else {
				dest.data.set(new TYPE(this.data.buffer, srcPos * ELEMENT_BYTES_SIZE, length), destPos);
			}
		}
	};

	ARRAY.prototype.__JT__CLASS_ID = ARRAY.__JT__CLASS_ID = classId;
	ARRAY.prototype.__JT__CLASS_IDS = ARRAY.__JT__CLASS_IDS = [classId];

	//ARRAY.constructor.name = "TST";

	return ARRAY;
}

function __createJavaArrays() {
	JA_0 = __createJavaArrayBaseType();
	JA_L = class JA_L extends __createGenericArrayType(-1) {} // Generic Array
	JA_Z = class JA_Z extends __createJavaArrayType(-2, '[Z', Int8Array, 1) {}    // Bool Array
	JA_B = class JA_B extends __createJavaArrayType(-3, '[B', Int8Array, 1) {}    // Byte Array
	JA_C = class JA_C extends __createJavaArrayType(-4, '[C', Uint16Array, 2) {}  // Character Array
	JA_S = class JA_S extends __createJavaArrayType(-5, '[S', Int16Array, 2) {}  // Short Array
	JA_I = class JA_I extends __createJavaArrayType(-6, '[I', Int32Array, 4) {}   // Int Array
	JA_F = class JA_F extends __createJavaArrayType(-7, '[F', Float32Array, 4) {} // Float Array
	JA_D = class JA_D extends __createJavaArrayType(-8, '[D', Float64Array, 8) {} // Double Array
	JA_J = class JA_J extends __createJavaArrayType(-9, '[J', Array, 1) {}        // Long Array (Specially handled)
}

var __reints = (function() {
	const buffer = new ArrayBuffer(8);
	const doubleArray = new Float64Array(buffer);
	const floatArray = new Float32Array(buffer);
    const intArray = new Int32Array(buffer);
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
	static {{ ASYNC }} preInit({{ JC }}) {
	}

	static afterInit({{ JC }}) {
		//console.log(JA_Z);
		//console.log(JA_B);
		//console.log(JA_C);
		//console.log(JA_L);
		//console.log((new JA_Z(8)));
		//console.log((new JA_B(8)));
		//console.log((new JA_C(8)));
		//console.log((new JA_Z(8)).desc);
		//console.log((new JA_B(8)).desc);
		//console.log((new JA_C(8)).desc);
		//console.log((new JA_Z(8)) instanceof JA_Z);
		//console.log((new JA_B(8)) instanceof JA_Z);
		//console.log((new JA_C(8)) instanceof JA_Z);
		//console.log(N.is(new JA_Z(8), JA_Z));
		//console.log(N.is(new JA_B(8), JA_Z));
		//console.log(N.is(new JA_C(8), JA_Z));
	}

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

	static {{ ASYNC }} iteratorToArray({{ JC_COMMA }}it) {
		if (it == null) return null;
		var out = [];
		while ({{ AWAIT }} it{% IMETHOD java.util.Iterator:hasNext:()Z %}({{ JC }})) {
			out.push({{ AWAIT }} it{% IMETHOD java.util.Iterator:next:()Ljava/lang/Object; %}({{ JC }}));
		}
		return out;
	};

	static {{ ASYNC }} imap({{ JC_COMMA }}map) {
		if (map == null) return null;
		var obj = {};
		let array = {{ AWAIT }}(N.iteratorToArray({{ JC_COMMA }}{{ AWAIT }} {{ AWAIT }} map{% IMETHOD java.util.Map:entrySet %}(){% IMETHOD java.util.Set:iterator %}({{ JC }})))

		for (let n = 0; n < array.length; n++) {
			let item = array[n];
			var key = {{ AWAIT }} item{% IMETHOD java.util.Map$Entry:getKey %}({{ JC }});
			var value = {{ AWAIT }} item{% IMETHOD java.util.Map$Entry:getValue %}({{ JC }});
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
	static resolveClass({{ JC_COMMA }}name) {
		return {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}({{ JC_COMMA }}N.str(name));
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

	static boxVoid    ({{ JC_COMMA }}value) { return null; }
	static boxBool    ({{ JC_COMMA }}value) { return {% SMETHOD java.lang.Boolean:valueOf:(Z)Ljava/lang/Boolean; %}({{ JC_COMMA }}value); }
	static boxByte    ({{ JC_COMMA }}value) { return {% SMETHOD java.lang.Byte:valueOf:(B)Ljava/lang/Byte; %}({{ JC_COMMA }}value); }
	static boxShort   ({{ JC_COMMA }}value) { return {% SMETHOD java.lang.Short:valueOf:(S)Ljava/lang/Short; %}({{ JC_COMMA }}value); }
	static boxChar    ({{ JC_COMMA }}value) { return {% SMETHOD java.lang.Character:valueOf:(C)Ljava/lang/Character; %}({{ JC_COMMA }}value); }
	static boxInt     ({{ JC_COMMA }}value) { return {% SMETHOD java.lang.Integer:valueOf:(I)Ljava/lang/Integer; %}({{ JC_COMMA }}value); }
	static boxLong    ({{ JC_COMMA }}value) { return {% SMETHOD java.lang.Long:valueOf:(J)Ljava/lang/Long; %}({{ JC_COMMA }}value); }
	static boxFloat   ({{ JC_COMMA }}value) { return {% SMETHOD java.lang.Float:valueOf:(F)Ljava/lang/Float; %}({{ JC_COMMA }}value); }
	static boxDouble  ({{ JC_COMMA }}value) { return {% SMETHOD java.lang.Double:valueOf:(D)Ljava/lang/Double; %}({{ JC_COMMA }}value); }
	static boxString  ({{ JC_COMMA }}value) { return (value != null) ? N.str(value) : null; }
	static boxWrapped ({{ JC_COMMA }}value) { return N.wrap(value); }

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

	static createRuntimeException({{ JC_COMMA }}msg) {
		return {% CONSTRUCTOR java.lang.RuntimeException:(Ljava/lang/String;)V %}({{ JC_COMMA }}N.str(msg));
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

	static box({{ JC_COMMA }}v) {
		if (v instanceof {% CLASS java.lang.Object %}) return v; // already boxed!
		if (v instanceof Int64) return N.boxLong({{ JC_COMMA }}v);
		if (typeof v == 'string') return N.str(v);
		if ((v|0) == v) return N.boxInt({{ JC_COMMA }}v);
		if (+(v) == v) return N.boxFloat({{ JC_COMMA }}v);
		if ((v == null) || N.is(v, {% CLASS java.lang.Object %})) return v;
		return N.wrap(v);
	};

	static isNegativeZero(x) { return x === 0 && 1 / x === -Infinity; };

	//N.sort = {{ ASYNC }} function(array, start, end, comparator) {
	//	var slice = array.slice(start, end);
	//	if (comparator === undefined) {
	//		slice.sort();
	//	} else {
	//		throw 'Unsupported N.sort!';
	//		//slice.sort({{ ASYNC }} function(a, b) {
	//		//	return {{ AWAIT }} comparator["{% METHOD java.util.Comparator:compare:(Ljava/lang/Object;Ljava/lang/Object;)I %}"](a, b);
	//		//});
	//	}
	//	for (var n = 0; n < slice.length; ++n) array[start + n] = slice[n];
	//};

	static {{ ASYNC }} asyncAsyncStr({{ JC_COMMA }}v) {
		if (v == null) return 'null';
		{% if IS_ASYNC %}
		if (typeof v.toStringAsync !== 'undefined') {
			return N.istr({{ AWAIT }}(v.toStringAsync({{ JC }})));
		}
		{% end %}
		return '' + v;
	}

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

    // @TODO: {{ ASYNC }}
   	{% if IS_JC %}
		static {{ ASYNC }} monitorEnter({{ JC_COMMA }}obj) {
			{{ AWAIT }}(RecursiveMutex.getMutex(obj).lock({{ JC }}));
		}

		static {{ ASYNC }} monitorExit({{ JC_COMMA }}obj) {
			{{ AWAIT }}(RecursiveMutex.getMutex(obj).unlock({{ JC }}));
		}

		static {{ ASYNC }} threadNotify({{ JC_COMMA }}obj) {
			{{ AWAIT }}(RecursiveMutex.getMutex(obj).notify({{ JC }}));
		}

		static {{ ASYNC }} threadNotifyAll({{ JC_COMMA }}obj) {
			{{ AWAIT }}(RecursiveMutex.getMutex(obj).notifyAll({{ JC }}));
		}

		static {{ ASYNC }} threadWait({{ JC_COMMA }}obj, timeoutMs, timeoutNanos) {
			{{ AWAIT }}(RecursiveMutex.getMutex(obj).wait({{ JC_COMMA }}timeoutMs, timeoutNanos));
		}
	{% else %}
		static {{ ASYNC }} monitorEnter({{ JC_COMMA }}obj) { }
		static {{ ASYNC }} monitorExit({{ JC_COMMA }}obj) { }
		static {{ ASYNC }} threadNotify({{ JC_COMMA }}obj) { throw new Error('Not implemented N.threadNotify'); }
		static {{ ASYNC }} threadNotifyAll({{ JC_COMMA }}obj) { throw new Error('Not implemented N.threadNotifyAll'); }
		static {{ ASYNC }} threadWait({{ JC_COMMA }}obj, timeoutMs, timeoutNanos) { throw new Error('Not implemented N.threadWait'); }
	{% end %}
} // N

{% if IS_JC %}
class RecursiveMutex {
	static getMutex(obj) {
    	if (obj.__jt_mutex__ == null) obj.__jt_mutex__ = new RecursiveMutex();
    	return obj.__jt_mutex__;
	}

	constructor() {
		this.capturedThread = -1;
		this.capturedCount = 0;
		this.lockQueue = [];
		this.waitQueue = [];
	}

	// MUTEX

	{{ ASYNC }} lock({{ JC }}) {
    	//console.log('RecursiveMutex.lock:', this, {{ JC }});
		let threadId = {{ JC }}.threadId;
		if (this.capturedThread == -1) this.capturedThread = threadId;

		if (this.capturedThread == threadId) {
			this.capturedCount++;
		} else {
			return new Promise((resolve, reject) => {
				this.lockQueue.push(() => {
					this.capturedThread = threadId;
					this.capturedCount++;
					resolve();
				});
			});
		}
	}

	{{ ASYNC }} unlock({{ JC }}) {
    	//console.log('RecursiveMutex.unlock:', this, {{ JC }});
		let threadId = {{ JC }}.threadId;

		if (this.capturedThread == threadId) {
			this.capturedCount--;
		}

		if (this.capturedCount <= 0) {
			this.capturedCount = 0;
			this.capturedThread = -1;
			this.unlockNext();
		}
	}

	unlockNext() {
		if (this.lockQueue.length > 0) {
			let func = this.lockQueue.shift();
			func();
		}
	}

	// WAIT/NOTIFY

	{{ ASYNC }} notify({{ JC }}) {
		//console.log('RecursiveMutex.notify', {{ JC }});
		if (this.waitQueue.length > 0) {
			let callback = this.waitQueue.shift();
			callback();
		}
	}

	{{ ASYNC }} notifyAll({{ JC }}) {
		//console.log('RecursiveMutex.notifyAll', {{ JC }});
		var queue = this.waitQueue.splice(0, this.waitQueue.length);
		for (let callback of queue) callback();
	}

	{{ ASYNC }} wait({{ JC_COMMA }}timeoutMs, timeoutNano) {
		//console.log('RecursiveMutex.wait', {{ JC_COMMA }}timeoutMs, timeoutNano);
		return new Promise((resolve, reject) => {
			let func;
			let timer = -1;
			func = () => {
				if (timer >= 0) clearTimeout(timer);
				//console.log('------ resolved');
				resolve();
			};

			// 0 means wait forever
			if (timeoutMs > 0) {
				timer = setTimeout(() => {
					//console.log('------ timeout');
					let index = this.waitQueue.indexOf(func);
					if (index >= 0) {
						this.waitQueue.splice(index, 1);
					}
					reject(new Error("Timeout!"));
				}, timeoutMs);
			}

			this.waitQueue.push(func);
		});
	}
}
{% end %}

function stackTrace() {
	var err = new Error();
	return err.stack.split('\n').slice(3);
}

class java_lang_Object_base {
	constructor() {
	}

	toString() {
		{% if IS_ASYNC %}
			console.error('unsupported use toStringAsync instead:');
			console.error((new Error()).stack);
			return '(' + this.constructor.name + '): unsupported use toStringAsync instead';
		{% else %}
			return this ? N.istr(this{% IMETHOD java.lang.Object:toString %}({{ JC }})) : null;
		{% end %}
	}

	{{ ASYNC }} toStringAsync({{ JC }}) {
		return this ? N.istr({{ AWAIT }} this{% IMETHOD java.lang.Object:toString %}({{ JC }})) : null;
	};
}

{% if IS_JC %}
java_lang_Object_base.prototype.__jt_mutex__ = null;
{% end %}
java_lang_Object_base.prototype.___id = 0;
java_lang_Object_base.prototype.__JT__CLASS_ID = java_lang_Object_base.__JT__CLASS_ID = 0;
java_lang_Object_base.prototype.__JT__CLASS_IDS = java_lang_Object_base.__JT__CLASS_IDS = [0];


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