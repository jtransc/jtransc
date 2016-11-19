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

////////////////////////////////////////////////////////////////////////////

(function(_global) { "use strict";

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

	ARRAY.prototype = Object.create(java_lang_Object.prototype);
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

//function Int64Ref(high, low) { this.high = high; this.low = low; }
//N.lnewRef = function(high, low) { return new Int64Ref(high, low); };
N.lnewRef = function(high, low) { return N.lnew(high, low); };

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
    		return {% SMETHOD com.jtransc.lang.Int64:make %}(intArray[1], intArray[0]);
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
N.i2j = function(v) { return {% SMETHOD com.jtransc.lang.Int64:ofInt %}(v); }
N.i2f = function(v) { return +v; }
N.i2d = function(v) { return +v; }

N.f2j = function(v) { return {% SMETHOD com.jtransc.lang.Int64:ofFloat %}(v); }
N.d2j = function(v) { return {% SMETHOD com.jtransc.lang.Int64:ofFloat %}(v); }


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
N.lnew = function(high, low) { return {% SMETHOD com.jtransc.lang.Int64:make %}(high, low); };
N.lnewFloat = function(float) { return {% SMETHOD com.jtransc.lang.Int64:ofFloat %}(float); };
N.ltoFloat = function(v) { return {% SMETHOD com.jtransc.lang.Int64:toFloat %}(v); };
N.llow = function(v) { return v['{% FIELD com.jtransc.lang.Int64:low %}']; }
N.lhigh = function(v) { return v['{% FIELD com.jtransc.lang.Int64:high %}']; }
N.ladd = function(a, b) { return {% SMETHOD com.jtransc.lang.Int64:add %}(a, b); }
N.lsub = function(a, b) { return {% SMETHOD com.jtransc.lang.Int64:sub %}(a, b); }
N.lmul = function(a, b) { return {% SMETHOD com.jtransc.lang.Int64:mul %}(a, b); }
N.ldiv = function(a, b) { return {% SMETHOD com.jtransc.lang.Int64:div %}(a, b); }
N.lrem = function(a, b) { return {% SMETHOD com.jtransc.lang.Int64:rem %}(a, b); }
N.llcmp = function(a, b) { return {% SMETHOD com.jtransc.lang.Int64:compare %}(a, b); } // Deprecated
N.lcmp = function(a, b) { return {% SMETHOD com.jtransc.lang.Int64:compare %}(a, b); }
N.lxor = function(a, b) { return {% SMETHOD com.jtransc.lang.Int64:xor %}(a, b); }
N.land = function(a, b) { return {% SMETHOD com.jtransc.lang.Int64:and %}(a, b); }
N.lor = function(a, b) { return {% SMETHOD com.jtransc.lang.Int64:or %}(a, b); }
N.lshl = function(a, b) { return {% SMETHOD com.jtransc.lang.Int64:shl %}(a, b); }
N.lshr = function(a, b) { return {% SMETHOD com.jtransc.lang.Int64:shr %}(a, b); }
N.lushr = function(a, b) { return {% SMETHOD com.jtransc.lang.Int64:ushr %}(a, b); }

N.lneg = function(a) { return {% SMETHOD com.jtransc.lang.Int64:neg %}(a); }
N.linv = function(a) { return {% SMETHOD com.jtransc.lang.Int64:not %}(a); }

N.l2i = function(v) { return {% SMETHOD com.jtransc.lang.Int64:toInt %}(v); }
N.l2d = function(v) { return {% SMETHOD com.jtransc.lang.Int64:toFloat %}(v); }

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

N.stringToByteArray = function(str, encoding) {
	// @TODO: Handle encoding!
	var out = new JA_B(str.length);
	for (var n = 0; n < str.length; n++) out.set(n, str.charCodeAt(n));
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

N.getStackTrace = function(count) {
	var out = new JA_L(3, '[Ljava/lang/StackTraceElement;');
	out.set(0, N.createStackTraceElement('Dummy', 'dummy', 'Dummy.java', 0));
	out.set(1, N.createStackTraceElement('Dummy', 'dummy', 'Dummy.java', 0));
	out.set(2, N.createStackTraceElement('Dummy', 'dummy', 'Dummy.java', 0));
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

N._arraycopyTyped_B = function(srcData, srcPos, destData, destPos, length) {
	destData.set(new Int8Array(srcData.buffer, srcPos * 1, length), destPos);
}

N._arraycopyTyped_S = function(srcData, srcPos, destData, destPos, length) {
	destData.set(new Int16Array(srcData.buffer, srcPos * 1, length), destPos);
}

N._arraycopyTyped_I = function(srcData, srcPos, destData, destPos, length) {
	destData.set(new Int32Array(srcData.buffer, srcPos * 4, length), destPos);
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
	} else if (src instanceof JA_B) {
		N._arraycopyTyped_B(srcData, srcPos, destData, destPos, length);
	} else if (src instanceof JA_S) {
		N._arraycopyTyped_S(srcData, srcPos, destData, destPos, length);
	} else if (src instanceof JA_I) {
		N._arraycopyTyped_I(srcData, srcPos, destData, destPos, length);
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
//N.boxByteArray = function(value) { return HaxeArrayByte.fromBytes(value); }

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
//N.unboxByteArray = function(value) { return (value).getBytes(); }


N.unbox = function(value) {
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
	if (N.is(value, {% CLASS com.jtransc.JTranscWrapped %})) return unboxWrapped(value);
	throw 'Was not able to unbox "$value"';
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
	if (v instanceof {% CLASS com.jtransc.lang.Int64 %}) return N.boxLong(v);
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
	if (v.buffer) v = v.buffer;
	var out = new JA_B(v.byteLength);
	out.data = new Int8Array(v);
	return out;
};

N.clone = function(obj) {
	if (obj == null) return null;

	var temp = Object.create(obj);

	temp['{% FIELD java.lang.Object:$$id %}'] = {% SFIELD java.lang.Object:$$lastId %}++;

	return temp;
};

N.methodWithoutBody = function() {
	throw 'Method not implemented: native or abstract';
};

N.EMPTY_FUNCTION = function() { }

var java_lang_Object_base = function() { };
java_lang_Object_base.prototype.toString = function() {
	return this ? N.istr(this['{% METHOD java.lang.Object:toString %}']()) : null;
};

/* ## BODY ## */

})((typeof window !== "undefined") ? window : global);