var R = function() {
};

function __createField(info) {
	var out = new java_lang_reflect_Field();
	out.{% FIELD java.lang.reflect.Field:clazz %} = N.resolveClass(info.desc);
	out._internalName = info.id;
	out.{% FIELD java.lang.reflect.Field:name %} = N.str(info.name);
	out.{% FIELD java.lang.reflect.Field:signature %} = N.str(info.desc);
	out.{% FIELD java.lang.reflect.Field:genericSignature %} = N.str(info.genericDesc || info.desc);
	out.{% FIELD java.lang.reflect.Field:modifiers %} = N.str(info.flags);
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
		clazzClazz._constructors = [];
		clazzClazz._annotations = [];

		clazzClazz._jsClass = clazz;
		clazzClazz._interfaces = clazzInfo.interfaces;
		clazzClazz._superclass = clazzInfo.parent;
		clazzClazz.{% FIELD java.lang.Class:modifiers %} = clazzInfo.flags;

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

R.getField = function(field, obj) {
	var obj2 = (obj == null) ? obj.$$JS_TYPE_CONTEXT$$ : obj;
	return obj2[field._internalName];
};

R.setField = function(field, obj, value) {
	var obj2 = (obj == null) ? obj.$$JS_TYPE_CONTEXT$$ : obj;
	obj2[field._internalName] = value;
};