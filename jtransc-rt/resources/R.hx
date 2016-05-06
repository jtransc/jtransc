package ;

class R {
	static public var classByJavaNames = new Map<String, #CLASS:java.lang.Class# -> #CLASS:java.lang.Class#>();
	static public var haxeToJavaName = new Map<String, String>();
	static public var javaToHaxeName = new Map<String, String>();

	static private var registeredClasses = false;

	static private function registerClassesOnce() {
		if (!registeredClasses) {
			registeredClasses = true;
			HaxeReflectionInfo.__registerClasses();
		}
	}

	static public function internalClassNameToName(internalClassName:String):String {
		registerClassesOnce();
		return haxeToJavaName[internalClassName];
	}

	static public function classNameToInternalName(className:String):String {
		registerClassesOnce();
		return javaToHaxeName[className];
	}

	static public function __initClass(clazz:#CLASS:java.lang.Class#):Bool {
		registerClassesOnce();
		var className = clazz.#FIELD:java.lang.Class:name#._str;
		if (className.substr(0, 1) == '[') return true;
		var clazzGen = classByJavaNames[className];
		if (clazzGen == null) return false;
		clazzGen(clazz);
		return true;
	}

	static public function register(javaName:String, haxeName:String, clazzGen:#CLASS:java.lang.Class# -> #CLASS:java.lang.Class#) {
		classByJavaNames[javaName] = clazzGen;
		haxeToJavaName[haxeName] = javaName;
		javaToHaxeName[javaName] = haxeName;
	}

	static public function getJavaClassName(clazz:Class<Dynamic>):String return Reflect.field(clazz, 'HAXE_CLASS_NAME');
	static public function getJavaClass(str:String) return #CLASS:java.lang.Class#.#METHOD:java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class;#(HaxeNatives.str(str));
	static public function i(c:#CLASS:java.lang.Class#, haxeClass:Class<Dynamic>, proxyClass:Class<Dynamic>, ffiClass:Class<Dynamic>, parent:String, interfaces:Array<String>, modifiers:Int, annotations:Array<Dynamic>) {
		c._hxClass = haxeClass;
		c._hxProxyClass = proxyClass;
		c._hxFfiClass = ffiClass;
		c._internalName = Type.getClassName(haxeClass);
		c._parent = parent;
		c._interfaces = interfaces;
		c._modifiers = modifiers;
		c._fields = [];
		c._methods = [];
		c._constructors = [];
		c._annotations = annotations;
		var initMethod = Reflect.field(haxeClass, '__hx_static__init__'); if (initMethod != null) Reflect.callMethod(haxeClass, initMethod, []);
	}
	static public function f(c:#CLASS:java.lang.Class#, internalName:String, slot:Int, name:String, type:String, modifiers:Int, genericDescriptor:String, annotations:Array<Dynamic>) {
		var out = new #CLASS:java.lang.reflect.Field#();
		out.#FIELD:java.lang.reflect.Field:clazz# = c;
		out.#FIELD:java.lang.reflect.Field:name# = HaxeNatives.str(name);
		out._internalName = internalName;
		out.#FIELD:java.lang.reflect.Field:modifiers# = modifiers;
		out.#FIELD:java.lang.reflect.Field:signature# = HaxeNatives.str(type);
		out.#FIELD:java.lang.reflect.Field:genericSignature# = HaxeNatives.str(genericDescriptor);
		out.#FIELD:java.lang.reflect.Field:slot# = slot;
		out._annotations = annotations;
		c._fields.push(out);
	}
	static public function m(c:#CLASS:java.lang.Class#, id:Int, internalName:String, slot:Int, name:String, modifiers:Int, signature:String, genericDescriptor:String, annotations:Array<Dynamic>, parameterAnnotations:Array<Array<Dynamic>>) {
		var out = new #CLASS:java.lang.reflect.Method#();
		out._internalName = internalName;
		out.#FIELD:java.lang.reflect.Method:clazz# = c;
		out.#FIELD:java.lang.reflect.Method:id# = id;
		out.#FIELD:java.lang.reflect.Method:name# = HaxeNatives.str(name);
		out.#FIELD:java.lang.reflect.Method:signature# = HaxeNatives.str(signature);
		out.#FIELD:java.lang.reflect.Method:genericSignature# = HaxeNatives.str(genericDescriptor);
		out.#FIELD:java.lang.reflect.Method:slot# = slot;
		out.#FIELD:java.lang.reflect.Method:modifiers# = modifiers;
		out._annotations = annotations;
		out._parameterAnnotations = parameterAnnotations;
		c._methods.push(out);
	}
	static public function c(c:#CLASS:java.lang.Class#, internalName:String, slot:Int, modifiers:Int, signature:String, genericDescriptor:String, annotations:Array<Dynamic>, parameterAnnotations:Array<Array<Dynamic>>) {
		var out = new #CLASS:java.lang.reflect.Constructor#();
		out._internalName = internalName;
		out.#FIELD:java.lang.reflect.Constructor:clazz# = c;
		out.#FIELD:java.lang.reflect.Constructor:slot# = slot;
		out.#FIELD:java.lang.reflect.Constructor:modifiers# = modifiers;
		out.#FIELD:java.lang.reflect.Constructor:signature# = HaxeNatives.str(signature);
		out.#FIELD:java.lang.reflect.Constructor:genericSignature# = HaxeNatives.str(genericDescriptor);
		out._annotations = annotations;
		out._parameterAnnotations = parameterAnnotations;
		c._constructors.push(out);
	}

	static public function n(className:String, methodId:Int) {
		HaxeNatives.debugger();
		var clazz = HaxeNatives.resolveClass(className);
		var method = (clazz != null) ? clazz.locateMethodById(methodId) : null;
		var methodName = (method != null) ? method.#FIELD:java.lang.reflect.Method:name#._str : "unknown";
		var methodSignature = (method != null) ? method.#FIELD:java.lang.reflect.Method:signature#._str : "unknown";
		return 'Native or abstract: $className.$methodName ($methodId) :: $methodSignature';
	}
}