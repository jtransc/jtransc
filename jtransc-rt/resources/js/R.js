var R = function() {
};

function __createField(info) {
	var out = new java_lang_reflect_Field();
	out._clazz = N.resolveClass(info.desc);
	out._name = N.str(info.name);
	out._signature = N.str(info.desc);
	out._modifiers = N.str(info.flags);
	return out;
}

R.__initClass = function(clazzClazz) {
	var clazzName = N.istr(clazzClazz._name);
	var clazz = jtranscClasses[clazzName];

	// Array
	if (clazzName.startsWith('[')) {
		return true;
	} else {
		var clazzInfo = clazz.$$JS_TYPE_CONTEXT$$;

		//console.log('__initClass:' + clazzClazz._name);
		//console.log(clazzInfo.fields);

		clazzClazz._fields = clazzInfo.fields.map(function(info) { return __createField(info); });
		clazzClazz._methods = [];
		clazzClazz._jsClass = clazz;
		clazzClazz._interfaces = clazzInfo.interfaces;
		clazzClazz._superclass = clazzInfo.parent;

		//java.lang.reflect.Field

		return true;
	}
};

R.getClass = function(obj) {
	if (obj instanceof JA_0) {
		return N.resolveClass(obj.desc);
	}

	var typeContext = obj.$$JS_TYPE_CONTEXT$$;
	if (!typeContext.clazzClazz) {
		typeContext.clazzClazz = N.resolveClass(typeContext.name);
	}
	return typeContext.clazzClazz;
};
