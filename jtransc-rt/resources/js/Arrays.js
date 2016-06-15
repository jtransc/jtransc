var JA_0, JA_Z, JA_B, JA_C, JA_S, JA_I, JA_J, JA_F, JA_D, JA_L;

function __decorateArray(ARRAY) {
	var tc = new TypeContext();

	tc.allInterfaces = [];
	tc.allAncestors = ["java.lang.Object"];
	tc.interfaces = [];
	tc.allAncestorsAndInterfaces = tc.allInterfaces.concat(tc.allAncestors);

	jtranscTypeContext["java.lang.Object"].instanceOf[tc.id] = true;
	tc.instanceOf[tc.id] = true;
	ARRAY.$$instanceOf = tc.instanceOf;

	ARRAY.$$JS_TYPE_CONTEXT$$ = tc;
	ARRAY.prototype.$$JS_TYPE_CONTEXT$$ = tc;
}

function __createJavaArrayBaseType() {
	var ARRAY = function() {
		this.$JS$ID$ = $JS$__lastId++;
		this.$JS$CLASS_ID$ = ARRAY.$$JS_TYPE_CONTEXT$$.id;
	};

	__decorateArray(ARRAY);

	ARRAY.prototype["getClass()Ljava/lang/Class;"] = function() {
		return N.resolveClass(this.desc);
	};
	return ARRAY;
}

function __createJavaArrayType(desc, type) {
	var ARRAY = function(size) {
		this.$JS$ID$ = $JS$__lastId++;
		this.$JS$CLASS_ID$ = ARRAY.$$JS_TYPE_CONTEXT$$.id;
		this.desc = desc;
		this.data = new type(size);
		this.length = size;
		this.init();

		//console.log('Created array instance: [' + desc + ":" + type.name + ":" + size + "]");
	};

	__decorateArray(ARRAY);

	ARRAY.prototype = $extend(JA_0.prototype, {});

	if (desc == '[J') {
		ARRAY.prototype.init = function() {
			for (var n = 0; n < this.length; n++) this.set(n, Int64.zero);
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

	ARRAY.prototype.get = function(index) { return this.data[index]; };
	ARRAY.prototype.set = function(index, value) { this.data[index] = value; };

	ARRAY.prototype.toArray = function() {
    	var out = new Array(this.length);
    	for (var n = 0; n < out.length; n++) out[n] = this.get(n);
    	return out;
    };

	ARRAY.prototype["clone()Ljava/lang/Object;"] = ARRAY.prototype.clone;

	ARRAY.prototype["getClass()Ljava/lang/Class;"] = function() {
		return N.resolveClass(this.desc);
	};

	return ARRAY;
}

function __createGenericArrayType() {
	var ARRAY = function(size, desc) {
		this.$JS$ID$ = $JS$__lastId++;
		this.$JS$CLASS_ID$ = ARRAY.$$JS_TYPE_CONTEXT$$.id;
		this.desc = desc;
		this.data = new Array(size);
		this.length = size;
		for (var n = 0; n < size; n++) this.data[n] = null;
	};

	__decorateArray(ARRAY);

	ARRAY.prototype = $extend(JA_0.prototype, {});

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

	ARRAY.prototype["clone()Ljava/lang/Object;"] = ARRAY.prototype.clone;

	ARRAY.prototype["getClass()Ljava/lang/Class;"] = function() {
		return N.resolveClass(this.desc);
	};

	return ARRAY;
}

function __createJavaArrays() {
	JA_0 = __createJavaArrayBaseType();
	JA_Z = __createJavaArrayType('[Z', Int8Array);    // Bool Array
	JA_B = __createJavaArrayType('[B', Int8Array);    // Byte Array
	JA_C = __createJavaArrayType('[C', Uint16Array);  // Character Array
	JA_S = __createJavaArrayType('[S', Int16Array);   // Short Array
	JA_I = __createJavaArrayType('[I', Int32Array);   // Int Array
	JA_J = __createJavaArrayType('[J', Array);        // Long Array
	JA_F = __createJavaArrayType('[F', Float32Array); // Float Array
	JA_D = __createJavaArrayType('[D', Float64Array); // Double Array

	JA_L =__createGenericArrayType(); // Generic Array

	JA_L.createMultiSure = function(sizes, desc) {
		if (!desc.startsWith('[')) return null;
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
