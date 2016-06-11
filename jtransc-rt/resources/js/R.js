var R = function() {
};

function __createField(info) {
	var out = new java_lang_reflect_Field();

	return out;
}

R.__initClass = function(clazzClazz) {
	var clazzName = N.istr(clazzClazz._name);
	var clazz = jtranscClasses[clazzName];

	// Array
	if (clazzName.startsWith('[')) {
		return true;
	} else {
		var clazzInfo = clazz.$$JS_CONTEXT$$;

		//console.log('__initClass:' + clazzClazz._name);
		//console.log(clazzInfo.fields);

		clazzClazz._fields = clazzInfo.fields.map(function(info) { return __createField(info); });
		clazzClazz._methods = [];
		clazzClazz._jsClass = clazz;

		//java.lang.reflect.Field

		return true;
	}
};

R.getClass = function(obj) {
	if (obj instanceof JA_0) {
		return N.resolveClass(obj.desc);
	}

	var typeContext = obj.$$JS_CONTEXT$$;
	if (!typeContext.clazzClazz) {
		typeContext.clazzClazz = N.resolveClass(typeContext.name);
	}
	return typeContext.clazzClazz;
};