function $extend(from, fields) {
	function Inherit() {} Inherit.prototype = from; var proto = new Inherit();
	for (var name in fields) proto[name] = fields[name];
	if( fields.toString !== Object.prototype.toString ) proto.toString = fields.toString;
	return proto;
}

var __TRACE = false;
//var __TRACE = true;

var S = {};
var SS = {};
var CLASSES = {};

if (__TRACE) console.log("global:" + _global);

_global.$JS$__lastId = 0;

_global.jtranscClasses = {};
_global.jtranscTypeContext = {};

function __buildStrings() {
	for (var id in SS) S[id] = N.str(SS[id]);
}

var ProgramContext = function() {
	this.types = {};
	this.mainClass = null;
};

var lastTypeId = 1;

var TypeContext = function (internalName, name, flags, parent, interfaces) {
	this.id = lastTypeId++;
	this.internalName = internalName;
	this.initialized = false;
	this.name = name;
	this.flags = flags;
	var clazz = function() {
		if (clazz.$js$super) {
			clazz.$js$super.call(this);
		}
		clazz.$instanceInit.call(this);
	};
	this.clazz = clazz;
	Object.defineProperty(this.clazz.prototype, "name", { value: name });
	//this.clazz.prototype.name = name;
	this.parent = parent;
	this.interfaces = interfaces;
	//this.clazz.$$JS_TYPE_CONTEXT$$ = this;
	//this.clazz.prototype.$$JS_TYPE_CONTEXT$$ = this;
	this.fields = [];
	this.methods = [];
	this.constructors = [];
	this.annotations = [];
	this.initialized = false;
	this.instanceOf = {};

	this.staticMethodsBody = {};
	this.instanceMethodsBody = {};

	this.staticInit = null;
	this.instanceInit = null;

	this.clazz.$$instanceOf = this.instanceOf;

	_global.jtranscClasses[name] = this.clazz;
	_global.jtranscTypeContext[name] = this;
};

var MethodContext = function (name, flags) {
	this.name = name;
	this.flags = flags;
};

ProgramContext.prototype.registerStrings = function(strs) {
	//console.log("Register strings: " + strs);
	SS = strs;
};

var EMPTY_FUNCTION = function(){};

TypeContext.prototype.completeTypeFirst = function() {
	var inits = ['this.$JS$ID$ = $JS$__lastId++;this.$JS$CLASS_ID$ = ' + this.id + ';', ''];

	for (var n = 0; n < this.fields.length; n++) {
		var field = this.fields[n];
		var value = field.value;
		var valueStr;
		var index = field.static ? 1 : 0;

		if (value instanceof Int64) {
			valueStr = 'Int64.make(' + value.high + ',' + value.low + ')';
		} else if (value instanceof String) {
			valueStr = "N.strLit(" + (value).quote() + ")";
		} else {
			valueStr = JSON.stringify(value);
		}

		inits[index] += 'this["' + field.id + '"] = ' + valueStr + ";\n";
	}

	this.staticInit = new Function(inits[1]);
	this.instanceInit = new Function(inits[0]);

	this.staticMethodsBody['$staticInit'] = this.staticInit;
	this.staticMethodsBody['$instanceInit'] = this.instanceInit;
};

ProgramContext.prototype.registerType = function(internalName, name, flags, parent, interfaces, callback) {
	//console.log("Register class: " + name);
	if (internalName == null) internalName = name.replace(/\./g, '_');

	var context = new TypeContext(internalName, name, flags, parent, interfaces);

	//_global[name.replace(/\./g, '_')] = context.clazz;
	context.clazz.SI = function() {
		context.clazz.SI = EMPTY_FUNCTION;
		context.clazz.$staticInit();
		var clinit = context.clazz["<clinit>()V"];
		if (clinit != null) {
			clinit();
		}
	};
	callback.apply(context, []);
	this.types[name] = context;
	context.completeTypeFirst();

	return context.clazz;
};

ProgramContext.prototype.registerMainClass = function(name) {
	this.mainClass = name;
};

TypeContext.prototype._getScopeFromFlags = function(flags) {
	// static
	//return (flags & 0x00000008) ? this.clazz : this.clazz.prototype;
	return (flags & 0x00000008) ? this.staticMethodsBody : this.instanceMethodsBody;
};

TypeContext.prototype.registerMethod = function(id, name, desc, genericDesc, flags, callback) {
	if (id == null) id = name + desc;
	var typeContext = this;
	var hasBody = true;
	if (callback == null) {
		hasBody = false;
		callback = function() { throw 'Method without body ' + typeContext.name + "." + name + " : " + id; };
	}
	this._getScopeFromFlags(flags)[id] = callback;
	Object.defineProperty(callback, "name", { value: typeContext.name + "." + name });
	this.methods.push({
		id : id,
		name : name,
		desc : desc,
		genericDesc : genericDesc,
		flags: flags,
		hasBody: hasBody,
		static : (flags & 0x00000008) != 0
	});
};

TypeContext.prototype.registerConstructor = function(id, desc, genericDesc, flags, callback) {
	var name = '<init>';
	if (id == null) id = this.name + name + desc;
	var typeContext = this;
	if (callback == null) callback = function() { throw 'Method without body ' + typeContext.name + "." + name + " : " + id; };
	this._getScopeFromFlags(flags)[id] = callback;
	Object.defineProperty(callback, "name", { value: typeContext.name + "." + name });
	this.constructors.push({
		id : id,
		name : name,
		desc : desc,
		genericDesc : genericDesc,
		flags: flags,
		static : (flags & 0x00000008) != 0
	});
};

TypeContext.prototype.registerField = function(id, name, desc, genericDesc, flags, value) {
	if (id == null) id = '_' + name;
	this.fields.push({
		id : id,
		name : name,
		desc : desc,
		genericDesc : genericDesc,
		flags: flags,
		value: value,
		static : (flags & 0x00000008) != 0
	});
};

ProgramContext.prototype.getType = function(clazzName) {
	var clazz = _global.jtranscClasses[clazzName];
	var clazzInfo = _global.jtranscTypeContext[clazzName];

	if (!clazzInfo.initialized) {
		clazzInfo.initialized = true;

		var allInterfaces = clazzInfo.interfaces.slice(0);

		var allAncestors = [clazzInfo.name];

		// Normal classes
		if (clazzInfo.parent != null) {
			var parentClazz = this.getType(clazzInfo.parent);
			var parentClazzInfo = _global.jtranscTypeContext[clazzInfo.parent];

			if (!parentClazz) throw 'No parentClazz: ' + clazzInfo.parent;

			clazz.prototype = $extend(parentClazz.prototype, clazzInfo.instanceMethodsBody);
			clazz.$js$super = parentClazz;

			allInterfaces = allInterfaces.concat(parentClazzInfo.allInterfaces);
			allAncestors = allAncestors.concat(parentClazzInfo.allAncestors);
		}
		// Interfaces and java.lang.Object
		else {
			clazz.prototype = clazzInfo.instanceMethodsBody;
			clazz.$js$super = null;

			// java.lang.Object
			if (clazzName == "java.lang.Object") {
				//console.log('clazzName:' + clazzName);

			} else {
			}
		}

		for (var k in clazzInfo.staticMethodsBody) {
			if (clazzInfo.staticMethodsBody.hasOwnProperty(k)) {
				clazz[k] = clazzInfo.staticMethodsBody[k];
			}
		}

		clazz.$$JS_TYPE_CONTEXT$$ = clazzInfo;
		clazz.prototype.$$JS_TYPE_CONTEXT$$ = clazzInfo;

		clazzInfo.allInterfaces = allInterfaces;
		clazzInfo.allAncestors = allAncestors;
		clazzInfo.allAncestorsAndInterfaces = allAncestors.concat(allInterfaces);

		for (var n = 0; n < clazzInfo.allAncestorsAndInterfaces.length; n++) {
			var ancestorName = clazzInfo.allAncestorsAndInterfaces[n];
			var ancestor = _global.jtranscTypeContext[ancestorName];
			ancestor.instanceOf[clazzInfo.id] = true;
		}

		clazz.prototype.toString = function() {
			//console.log('called object.toString!');
			return N.istr(this["toString()Ljava/lang/String;"]());
		};
	}
	return clazz;
};

ProgramContext.prototype.finishTypes = function() {
	for (var clazzName in _global.jtranscClasses) {
		this.getType(clazzName);
	}

	__createJavaArrays();
	__buildStrings();
};

//ProgramContext.prototype.registerProgram = function(callback) {
//	if (__TRACE) console.log('registering program...');
//	callback.apply(this, []);
//	if (__TRACE) console.log('registered program...');
//
//	this.finishTypes();
//
//	if (__TRACE) console.log('executing program...');
//	var mainMethod = _global.jtranscClasses[this.mainClass]["main([Ljava/lang/String;)V"];
//	mainMethod(N.strArray(N.args()));
//};

ProgramContext.prototype.finish = function() {
	this.finishTypes();
	var mainMethod = _global.jtranscClasses[this.mainClass]["main([Ljava/lang/String;)V"];
	mainMethod(N.strArray(N.args()));
};

var program = new ProgramContext();

/* ## BODY ## */

program.finish();