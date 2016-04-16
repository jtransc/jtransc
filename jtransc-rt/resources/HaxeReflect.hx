class HaxeReflect {
	static public function getJavaClass(str:String) {
		return java_.lang.Class_.forName_Ljava_lang_String__Ljava_lang_Class_(HaxeNatives.str(str));
	}
	static public function info(c:java_.lang.Class_, haxeClass:Class<Dynamic>, proxyClass:Class<Dynamic>, ffiClass:Class<Dynamic>, parent:String, interfaces:Array<String>, modifiers:Int, annotations:Array<Dynamic>) {
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
	static public function field(c:java_.lang.Class_, internalName:String, slot:Int, name:String, type:String, modifiers:Int, genericDescriptor:String, annotations:Array<Dynamic>) {
		var out = new java_.lang.reflect.Field_();
		out.clazz = c;
		out.name = HaxeNatives.str(name);
		out._internalName = name;
		out.modifiers = modifiers;
		out.signature = HaxeNatives.str(type);
		out.genericSignature = HaxeNatives.str(genericDescriptor);
		out.slot = slot;
		out._annotations = annotations;
		c._fields.push(out);
	}
	static public function method(c:java_.lang.Class_, id:Int, internalName:String, slot:Int, name:String, modifiers:Int, signature:String, genericDescriptor:String, annotations:Array<Dynamic>, parameterAnnotations:Array<Array<Dynamic>>) {
		var out = new java_.lang.reflect.Method_();
		out._internalName = internalName;
		out.clazz = c;
		out.id = id;
		out.name = HaxeNatives.str(name);
		out.signature = HaxeNatives.str(signature);
		out.genericSignature = HaxeNatives.str(genericDescriptor);
		out.slot = slot;
		out.modifiers = modifiers;
		out._annotations = annotations;
		out._parameterAnnotations = parameterAnnotations;
		c._methods.push(out);
	}
	static public function constructor(c:java_.lang.Class_, internalName:String, slot:Int, modifiers:Int, signature:String, genericDescriptor:String, annotations:Array<Dynamic>, parameterAnnotations:Array<Array<Dynamic>>) {
		var out = new java_.lang.reflect.Constructor_();
		out._internalName = internalName;
		out.clazz = c;
		out.slot = slot;
		out.modifiers = modifiers;
		out.signature = HaxeNatives.str(signature);
		out.genericSignature = HaxeNatives.str(genericDescriptor);
		out._annotations = annotations;
		out._parameterAnnotations = parameterAnnotations;
		c._constructors.push(out);
	}
}