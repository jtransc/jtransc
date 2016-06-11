var _global = (typeof window !== "undefined") ? window : global;

var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p) && d[p] === undefined) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};

var __TRACE = false;
//var __TRACE = true;

var S = {};
var SS = {};
var CLASSES = {};

if (__TRACE) console.log("global:" + _global);

_global.$JS$__lastId = 0;

_global.jtranscClasses = {};

function __buildStrings() {
	for (id in SS) S[id] = N.str(SS[id]);
}

var ProgramContext = function() {
	this.types = {};
	this.mainClass = null;
};

var TypeContext = function (name, flags, parent, interfaces) {
	this.initialized = false;
	this.name = name;
	this.flags = flags;
	this.clazz = function() { this.$instanceInit(); };
	Object.defineProperty(this.clazz.prototype, "name", { value: name });
	//this.clazz.prototype.name = name;
	this.parent = parent;
	this.interfaces = interfaces;
	this.clazz.$$JS_TYPE_CONTEXT$$ = this;
	this.clazz.prototype.$$JS_TYPE_CONTEXT$$ = this;
	this.fields = [];
	this.methods = [];
	this.constructors = [];
	this.annotations = [];
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
	var inits = ['this.$JS$ID$ = $JS$__lastId++;', ''];

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

	this.clazz.$staticInit = new Function(inits[1]);
	this.clazz.prototype.$instanceInit = new Function(inits[0]);
};

ProgramContext.prototype.registerType = function(name, flags, parent, interfaces, callback) {
	//console.log("Register class: " + name);

	var context = new TypeContext(name, flags, parent, interfaces);
	_global[name.replace(/\./g, '_')] = context.clazz;
	_global.jtranscClasses[name] = context.clazz;
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
	return context;
};

ProgramContext.prototype.registerMainClass = function(name) {
	this.mainClass = name;
};

TypeContext.prototype._getScopeFromFlags = function(flags) {
	// static
	return (flags & 0x00000008) ? this.clazz : this.clazz.prototype;
};

TypeContext.prototype.registerMethod = function(id, name, desc, genericDesc, flags, callback) {
	if (id == null) id = name + desc;
	var typeContext = this;
	if (callback == null) callback = function() { throw 'Method without body ' + typeContext.name + "." + name + " : " + id; };
	this._getScopeFromFlags(flags)[id] = callback;
	Object.defineProperty(callback, "name", { value: typeContext.name + "." + name });
	this.methods.push({
		id : id,
		name : name,
		desc : desc,
		genericDesc : genericDesc,
		flags: flags,
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
	this._getScopeFromFlags(flags)[name] = value;
	this.fields.push({
		id : id,
		name : name,
		desc : desc,
		genericDesc : genericDesc,
		flags: flags,
		value: value,
		static : (flags & 0x00000008) != 0
	});
	//console.log("  - Register field: " + name);
};

ProgramContext.prototype.getType = function(clazzName) {
	var clazz = _global.jtranscClasses[clazzName];
	var clazzInfo = clazz.$$JS_TYPE_CONTEXT$$;

	if (!clazzInfo.initialized) {
		clazzInfo.initialized = true;

		var allInterfaces = clazzInfo.interfaces.slice(0);

		var allAncestors = [clazzInfo.name];

		// Normal classes
		if (clazzInfo.parent != null) {
			var parentClazz = this.getType(clazzInfo.parent);
			var parentClazzInfo = parentClazz.$$JS_TYPE_CONTEXT$$;

			//console.log(clazzName);
			if (clazzName == "java.lang.AbstractStringBuilder") {
				//console.log(Object.keys(parentClazz.prototype));
			}

			__extends(clazz.prototype, parentClazz.prototype);
			clazz.prototype._super = parentClazz.prototype;

			allInterfaces = allInterfaces.concat(parentClazzInfo.allInterfaces);
			allAncestors = allAncestors.concat(parentClazzInfo.allAncestors);
		}
		// java.lang.Object
		else if (clazzName == "java.lang.Object") {
			//console.log('clazzName:' + clazzName);

		}
		// Interfaces
		else {
		}

		clazzInfo.allInterfaces = allInterfaces;
		clazzInfo.allAncestors = allAncestors;
		clazzInfo.allAncestorsAndInterfaces = allAncestors.concat(allInterfaces);

		clazz.prototype.toString = function() {
			//console.log('called object.toString!');
			return N.istr(this["toString()Ljava/lang/String;"]());
		};
	}
	return clazz;
};

ProgramContext.prototype.finishTypes = function() {
	for (clazzName in _global.jtranscClasses) {
		this.getType(clazzName);
	}

	__createJavaArrays();
	__buildStrings();
};

ProgramContext.prototype.registerProgram = function(callback) {
	if (__TRACE) console.log('registering program...');
	callback.apply(this, []);
	if (__TRACE) console.log('registered program...');

	this.finishTypes();

	if (__TRACE) console.log('executing program...');
	var mainMethod = _global.jtranscClasses[this.mainClass]["main([Ljava/lang/String;)V"];
	mainMethod(N.strArray(N.args()));
};

var jtranscProgram = new ProgramContext();

jtranscProgram.registerProgram(function() {
	/* ## BODY ## */
});
