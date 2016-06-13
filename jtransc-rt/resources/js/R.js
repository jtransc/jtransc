// R : Reflection
var R = function() {
};

function __createField(clazzClazz, info) {
	var out = new java_lang_reflect_Field();
	out["{% FIELD java.lang.reflect.Field:clazz %}"] = clazzClazz;
	out["_internalName"] = info.id;
	out["{% FIELD java.lang.reflect.Field:name %}"] = N.str(info.name);
	out["{% FIELD java.lang.reflect.Field:signature %}"] = N.str(info.desc);
	out["{% FIELD java.lang.reflect.Field:genericSignature %}"] = N.str(info.genericDesc || info.desc);
	out["{% FIELD java.lang.reflect.Field:modifiers %}"] = N.str(info.flags);
	return out;
}

function __createMethod(clazzClazz, info) {
	var out = new java_lang_reflect_Method();
	out["{% FIELD java.lang.reflect.Method:clazz %}"] = clazzClazz;
	out["_internalName"] = info.id;
	out["_hasBody"] = info.hasBody;
	out["{% FIELD java.lang.reflect.Method:name %}"] = N.str(info.name);
	out["{% FIELD java.lang.reflect.Method:signature %}"] = N.str(info.desc);
	out["{% FIELD java.lang.reflect.Method:genericSignature %}"] = N.str(info.genericDesc);
	out["{% FIELD java.lang.reflect.Method:modifiers %}"] = N.str(info.flags);
	out["_annotations"] = info.annotations();
	out["_parameterAnnotations"] = info.argumentAnnotations ? info.argumentAnnotations() : [];
	return out;
}

function __createConstructor(clazzClazz, info) {
	var out = new java_lang_reflect_Constructor();
	out["{% FIELD java.lang.reflect.Constructor:clazz %}"] = clazzClazz;
	out["_internalName"] = info.id;
	out["{% FIELD java.lang.reflect.Constructor:signature %}"] = N.str(info.desc);
	out["{% FIELD java.lang.reflect.Constructor:genericSignature %}"] = N.str(info.genericDesc);
	out["{% FIELD java.lang.reflect.Constructor:modifiers %}"] = N.str(info.flags);
	out["_annotations"] = info.annotations ? info.annotations() : [];
	out["_parameterAnnotations"] = info.argumentAnnotations ? info.argumentAnnotations() : [];
	return out;
}

R.__initClass = function(clazzClazz) {
	var clazzName = N.istr(clazzClazz._name);
	var clazz = jtranscClasses[clazzName];

	// Array
	if (clazzName.startsWith('[')) {
		clazzClazz._internalName = clazzName;

		return true;
	} else {
		var clazzInfo = clazz.$$JS_TYPE_CONTEXT$$;

    	clazzClazz._internalName = clazzInfo.internalName;

		if (!clazzInfo.__initClassOnce) {
			clazzInfo.__initClassOnce = true;

			//console.log('__initClass:' + clazzClazz._name);
			//console.log(clazzInfo.fields);

			//console.log(clazzInfo.annotations);

			clazzClazz._fields = clazzInfo.fields.map(function(info) { return __createField(clazzClazz, info); });
			clazzClazz._methods = clazzInfo.methods.map(function(info) { return __createMethod(clazzClazz, info); });;
			clazzClazz._constructors = clazzInfo.constructors.map(function(info) { return __createConstructor(clazzClazz, info); });
			clazzClazz._annotations = clazzInfo.annotations();

			clazzClazz._jsClass = clazz;
			clazzClazz._interfaces = clazzInfo.interfaces;
			clazzClazz._superclass = clazzInfo.parent;
			clazzClazz.{% FIELD java.lang.Class:modifiers %} = clazzInfo.flags;

			//java.lang.reflect.Field
		}

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
	var obj2 = (obj == null) ? jtranscClasses[N.istr(field._clazz._name)] : obj;
	return obj2[field._internalName];
};

R.setField = function(field, obj, value) {
	var obj2 = (obj == null) ? jtranscClasses[N.istr(field._clazz._name)] : obj;
	obj2[field._internalName] = value;
};

R.invokeMethod = function(method, obj, args) {
	var obj2 = (obj == null) ? jtranscClasses[N.istr(method._clazz._name)] : obj;
	var result = obj2[method._internalName].apply(obj2, args.data);
	//console.log('RESULT::::::::: ' + result);
	//console.log('RESULT::::::::: ' + method['{% METHOD java.lang.reflect.Method:getReturnType %}']());
	return N.boxWithType(method['{% METHOD java.lang.reflect.Method:getReturnType %}'](), result);
};

R.newInstance = function(constructor, args) {
	//console.log(constructor);
	if (args == null) args = [];
	if (constructor == null) throw 'Invalid R.newInstance : constructor == null';

	var clazz = constructor._clazz._jsClass;
	var obj = new clazz();
	return obj[constructor._internalName].apply(obj, args.data);
};

R.newProxyInstance = function(ifc, invocationHandler) {
	var javaClass   = N.resolveClass(N.istr(ifc._name));
	var typeContext = jtranscTypeContext[N.istr(ifc._name)];

	if (!typeContext.proxyClass) {
		typeContext.proxyClass = function(__invocationHandler) {
			this.__invocationHandler = __invocationHandler;
		};

		typeContext.proxyClass.prototype = $extend(typeContext.clazz.prototype, {});

		javaClass._methods.forEach(function(method) {
			typeContext.proxyClass.prototype[method._internalName] = function() {
				return N.box(invocationHandler["{% METHOD java.lang.reflect.InvocationHandler:invoke %}"].call(
					invocationHandler,
					this,
					method,
					N.boxArray(Array.from(arguments))
				));
			};
			//console.log(it.id);
		});
		//console.log(typeContext.methods);
		//console.log(typeContext._methods);
	}
	//throw 'WIP';
	return new typeContext.proxyClass(invocationHandler);
};

R.createLambda = function(ifc, callback) {
	var typeContext = ifc.$$JS_TYPE_CONTEXT$$;
	var javaClass   = N.resolveClass(typeContext.name);
	var functionalMethod = javaClass._methods.filter(function(method) { return !method._hasBody; })[0];

	if (!functionalMethod) {
		throw "R.createLambda, can't detect functional interface method in class " + ifc;
	}

	var obj = new ifc();

	obj[functionalMethod._internalName] = function() {
		return callback.apply(this, Array.from(arguments));
	};

	return obj;
};

R.createAnnotation = function(ifc, values) {
	var obj = new ifc();
	var typeContext = ifc.$$JS_TYPE_CONTEXT$$;
	var methods = typeContext.methods;
	methods.forEach(function(method, n) {
		var value = values[n];
		//console.log(method + " : " + n + " : " + values[n]);
		obj[method.id] = function() { return value; };
	});
	obj['{% METHOD java.lang.Object:toString %}'] = function() {
		return {% SMETHOD com.jtransc.internal.JTranscAnnotationBase:toStaticString %}(this);
	};
	//console.log(obj);
	return obj;
};