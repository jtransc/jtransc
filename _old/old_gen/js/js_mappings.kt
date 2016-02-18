package com.jtransc.js

import com.jtransc.ast.AstType
import com.jtransc.ast.feature.SwitchesFeature
import com.jtransc.gen.ClassMappings

val JsCopyFiles = listOf<String>(
	// "all/as3/StringAdaptor.as",
	// "all/as3/Long.as",
	// "all/native/As3Natives.as",
	// "all/native/As3Output.as"
)

val JsFeatures = setOf(SwitchesFeature)

val JsKeywords = setOf(
	"package",
	"import",
	"class", "interface", "extends", "implements",
	"internal", "private", "protected", "final",
	"function", "var", "const",
	"if", "else",
	"switch", "case", "default",
	"do", "while", "for", "each", "in",
	"break", "continue",
	"int", "uint", "void",
	"goto"
)


fun JsMappings(): ClassMappings {
	val mappings = ClassMappings()

	mappings.map("java.lang.Object") {
		classReplacement("java.lang.Object\$", "*")
	}

	mappings.map("java.lang.String") {
		classReplacement("String")
		adaptor("java.lang.CharSequence", "all.as3.StringAdaptor")
		adaptor("java.lang.Comparable", "all.as3.StringAdaptor")
		adaptor("java.io.Serializable", "all.as3.StringAdaptor")

		body("<init>", "*", "") // Strip String Constructors
		body("compareTo", "*", "throw new Error('Not implemented');")

		call(STRING, "toUpperCase", ARGS(), "@obj.toUpperCase()")
		call(STRING, "toLowerCase", ARGS(), "@obj.toLowerCase()")
		call(INT, "length", ARGS(), "@obj.length")
		call(CHAR, "charAt", ARGS(INT), "@obj.charCodeAt(@args)")
		call(INT, "indexOf", ARGS(INT), "all.native.As3Natives.stringIndexOf(@obj, @args)")
		call(INT, "lastIndexOf", ARGS(INT), "all.native.As3Natives.stringLastIndexOf(@obj, @args)")
		call(STRING, "substring", ARGS(INT), "@obj.substring(@args)")
		call(STRING, "substring", ARGS(INT, INT), "@obj.substring(@args)")
		call(BOOL, "equals", ARGS(OBJECT), "@obj == @args")
		call(INT, "hashCode", ARGS(), "all.native.As3Natives.stringHashCode(@obj)") // @TODO
		call(STRING, "valueOf", ARGS(ARRAY(CHAR)), "all.native.As3Natives.charArrayToString(@args)")
		call(STRING, "valueOf", ARGS(CHAR), "String.fromCharCode(@args)")
		call(STRING, "toString", ARGS(), "@obj")
		callConstructor(ARGS(), "''")
		callConstructor(ARGS(STRING), "@args")
		callConstructor(ARGS(AstType.REF("java.lang.StringBuilder")), "@args._str")
		callConstructor(ARGS(ARRAY(CHAR)), "all.native.As3Natives.charArrayToString(@args)")
		callConstructor(ARGS(ARRAY(CHAR), INT, INT), "all.native.As3Natives.charArrayToString(@args)")
		callConstructor(ARGS(ARRAY(INT), INT, INT), "all.native.As3Natives.charArrayToString(@args)")
		callConstructor(ARGS(ARRAY(BYTE)), "all.native.As3Natives.byteArrayToString(@args)")

		call("valueOf", "*", "('' + @args)")
	}

	mappings.map("all.rt.StdioOutputStream") {
		body(VOID, "write", ARGS(INT), "all.native.As3Natives.outputChar(p0)")
	}

	mappings.map("java.lang.System") {
		nativeImport("flash.system.System")
		nativeImport("flash.utils.getTimer")

		body(LONG, "currentTimeMillis", ARGS(), "return all.as3.Long.fromInt(flash.utils.getTimer());")
		body(VOID, "gc", ARGS(), "flash.system.System.gc();")
		body(STRING, "getProperty", ARGS(STRING), "return all.native.As3Natives.getProperty(p0);")

	}

	mappings.map("java.lang.Math") {
		call(DOUBLE, "cos", ARGS(DOUBLE), "Math.cos(@args)")
		call(DOUBLE, "sin", ARGS(DOUBLE), "Math.sin(@args)")
		call(DOUBLE, "pow", ARGS(DOUBLE, DOUBLE), "Math.pow(@args)")
	}

	mappings.map("java.lang.Double") {
		call(BOOL, "isNaN", ARGS(DOUBLE), "isNaN(@args)")
		call(BOOL, "isFinite", ARGS(DOUBLE), "isFinite(@args)")
		body(STRING, "toString", ARGS(DOUBLE), "return '' + p0;")
	}

	mappings.map("java.lang.Integer") {
		body(STRING, "toString", ARGS(INT), "return '' + p0;")
		body(STRING, "toString", ARGS(INT, INT), "return Number(p0).toString(p1);")
	}

	mappings.map("java.lang.StringBuilder") {
		nativeImport("flash.utils.getTimer")
		nativeMember("public var _str:String = '';")
		body("append", "*", "this._str += p0; return this;")
		body("length", "*", "return this._str.length;")
		body("charAt", "*", "return this._str.charCodeAt(p0);")
		body("toString", "*", "return this._str;")
	}

	mappings.map("java.util.ArrayList") {
		nativeMember("private var _items:Array = [];")
		body(BOOL, "add", ARGS(OBJECT), "this._items.push(p0); return true;")
		body(OBJECT, "set", ARGS(INT, OBJECT), "var prev:* = this._items[p0]; this._items[p0] = p1; return prev;")
		body(OBJECT, "get", ARGS(INT), "return this._items[p0];")
		body(INT, "size", ARGS(), "return this._items.length;")
	}

	mappings.map("java.util.HashMap") {
		nativeImport("flash.utils.Dictionary")
		nativeMember("private var _items:flash.utils.Dictionary = new flash.utils.Dictionary();")
		nativeMember("private var _size:int = 0;")
		// @TODO: This should create keys with the hashcode and lists with values in order to iterate them to check for equality
		body(OBJECT, "put", ARGS(OBJECT, OBJECT), "var old:* = _items[p0]; _items[p0] = p1; if (old === undefined) { old = null; _size++; } return old;")
		body(OBJECT, "get", ARGS(OBJECT), "var res:* = _items[p0]; if (res === undefined) res = null; return res;")
		body(INT, "size", ARGS(), "return _size;")
		body(VOID, "clear", ARGS(), "_items = new flash.utils.Dictionary(); _size = 0;")
	}

	return mappings
}