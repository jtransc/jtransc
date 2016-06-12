// N : Native
var onNodeJs = typeof window == "undefined";

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
    		return Int64.make(intArray[1], intArray[0]);
    	},
		longBitsToDouble: function(v) {
			intArray[0] = v.low;
			intArray[1] = v.high;
			return doubleArray[0];
		}
    };
})();

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
N.lnew = function(high, low) { return Int64.make(high, low); };
N.ladd = function(a, b) { return Int64.add(a, b); }
N.lsub = function(a, b) { return Int64.sub(a, b); }
N.lmul = function(a, b) { return Int64.mul(a, b); }
N.ldiv = function(a, b) { return Int64.div(a, b); }
N.lrem = function(a, b) { return Int64.rem(a, b); }
N.llcmp = function(a, b) { return Int64.compare(a, b); }
N.lxor = function(a, b) { return Int64.xor(a, b); }
N.land = function(a, b) { return Int64.and(a, b); }
N.lor = function(a, b) { return Int64.or(a, b); }
N.lshl = function(a, b) { return Int64.shl(a, b); }
N.lshr = function(a, b) { return Int64.shr(a, b); }
N.lushr = function(a, b) { return Int64.ushr(a, b); }

N.lneg = function(a) { return Int64.neg(a); }
N.linv = function(a) { return Int64.not(a); }
//N.lnot = function(a) { return Int64.not(a); }

N.l2i = function(v) { return Int64.toInt(v); }
N.l2d = function(v) { return Int64.toFloat(v); }

N.cmp  = function(a, b) { return (a < b) ? -1 : ((a > b) ? 1 : 0); }
N.cmpl = function(a, b) { return (isNaN(a) || isNaN(b)) ? -1 : N.cmp(a, b); }
N.cmpg = function(a, b) { return (isNaN(a) || isNaN(b)) ? 1 : N.cmp(a, b); }


N.getTime = function() { return Date.now(); };

N.is = function(i, clazz) { return (i != null) ? (typeof clazz.$$instanceOf[i.$JS$CLASS_ID$] !== "undefined") : false; };

N.istr = function(str) {
	if (str == null) return null;
	if (str instanceof java_lang_String) return str._str;
	return '' + str;
}

N.ichar = function(i) {
	return String.fromCharCode(i);
}

N.str = function(str) {
	if (str == null) return null;
	if (str instanceof java_lang_String) return str;
	var out = new java_lang_String();
	out._str = '' + str;
	return out;
}

N.strLit = function(str) {
	// Check cache!
	return N.str(str);
};

N.strArray = function(strs) {
	var out = new JA_L(strs.length, '[Ljava/lang/String;');
	for (var n = 0; n < strs.length; n++) {
		out.set(n, N.str(strs[n]));
	}
	return out;
};

N.args = function() {
	return [];
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
	//var clazz = jtranscClasses[name];
	//var clazzInfo = clazz.$$JS_TYPE_CONTEXT$$;
	//if (!this.clazzInfo.clazzClass) {
	//	this.clazzInfo.clazzClass = java_lang_Class["forName"]();
	//}
	////this.clazzClass = function() { };
	//return this.clazzInfo.clazzClass;
	return java_lang_Class["forName(Ljava/lang/String;)Ljava/lang/Class;"](N.str(name));
};

N.createStackTraceElement = function(declaringClass, methodName, fileName, lineNumber) {
	var out = new java_lang_StackTraceElement();
	out["java.lang.StackTraceElement<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V"](
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

N.arraycopy = function(src, srcPos, dest, destPos, length) {
	for (var n = 0; n < length; n++) {
		dest.set(destPos + n, src.get(srcPos + n));
	}
};

N.isInstanceOfClass = function(obj, javaClass) {
	return N.is(obj, jtranscClasses[N.istr(javaClass._name)]);
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
//N.boxWrapped = function(value) { return JtranscWrapped.wrap(value); }
//N.boxByteArray = function(value) { return HaxeArrayByte.fromBytes(value); }

N.box = function(v) {
	if ((v|0) == v) return N.boxInt(v);
	if (+(v) == v) return N.boxFloat(v);
	if (v instanceof Int64) return N.boxLong(v);
	if (typeof v == 'string') return N.str(v);
	if ((v == null) || N.is(v, java_lang_Object)) return v;
	return null;
};


