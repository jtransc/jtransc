/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.gen.haxe

import com.jtransc.ast.AstType
import com.jtransc.ast.feature.SwitchesFeature
import com.jtransc.error.InvalidOperationException
import com.jtransc.gen.ClassMappings
import javax.print.DocFlavor

val HaxeCopyFiles = listOf(
	"HaxeNatives.hx",
	"HaxeFormat.hx",
	"HaxeNativeWrapper.hx",
	"HaxeBaseArray.hx",
	"HaxeByteArray.hx",
	"HaxeShortArray.hx",
	"HaxeIntArray.hx",
	"HaxeFloatArray.hx",
	"HaxeDoubleArray.hx",
	"HaxeLongArray.hx",
	"HaxeArray.hx"
)

//val HaxeFeatures = setOf(GotosFeature, SwitchesFeature)
val HaxeFeatures = setOf(SwitchesFeature)

val HaxeKeywords = setOf(
	"java",
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

enum class HaxeSubtarget(val switch: String, val singleFile: Boolean, val interpreter: String? = null) {
	JS(switch = "-js", singleFile = true, interpreter = "node"),
	CPP(switch = "-cpp", singleFile = false, interpreter = null),
	SWF(switch = "-swf", singleFile = true, interpreter = null),
	NEKO(switch = "-neko", singleFile = true, interpreter = "neko"),
	PHP(switch = "-php", singleFile = false, interpreter = "php"),
	CS(switch = "-cs", singleFile = false, interpreter = null),
	JAVA(switch = "-java", singleFile = false, interpreter = "java -jar"),
	PYTHON(switch = "-python", singleFile = true, interpreter = "python")
	;

	companion object {
		fun fromString(subtarget: String) = when (subtarget.toLowerCase()) {
			"" -> HaxeSubtarget.JS
			"js", "javascript" -> HaxeSubtarget.JS
			"cpp", "c", "c++" -> HaxeSubtarget.CPP
			"swf", "flash", "as3" -> HaxeSubtarget.SWF
			"neko" -> HaxeSubtarget.NEKO
			"php" -> HaxeSubtarget.PHP
			"cs", "c#" -> HaxeSubtarget.CS
			"java" -> HaxeSubtarget.JAVA
			"python" -> HaxeSubtarget.PYTHON
			else -> throw InvalidOperationException("Unknown subtarget '$subtarget'")
		}
	}
}

fun HaxeMappings(): ClassMappings {
	val TO_ARRAY = "toArray___Ljava_lang_Object_"
	val ITERATOR_TYPE = "java_.util.Iterator_"
	val ITERATOR = "iterator__Ljava_util_Iterator_"
	val HAS_NEXT = "hasNext__Z"
	val NEXT = "next__Ljava_lang_Object_"
	val CHARSEQUENCE = AstType.REF("java.lang.CharSequence")
	val COMPARATOR = AstType.REF("java.util.Comparator")
	val CHARSET = AstType.REF("java.nio.charset.Charset")
	val LOCALE = AstType.REF("java.util.Locale")
	val BYTEARRAY = AstType.ARRAY(AstType.BYTE)
	val RUNNABLE = AstType.REF("java.lang.Runnable")

	val mappings = ClassMappings()

	mappings.map("java.lang.String") {
		nativeMember("public var _str:String = '';")
		nativeMember("public function __native_init__(str:String) { this._str = str; return this; }")
		nativeMember("static public function make(str:String) { return new java_.lang.String_().__native_init__(str); }")
		body(INT, "length", ARGS(), "return _str.length;")
		body(CHAR, "charAt", ARGS(INT), "return _str.charCodeAt(p0);")
		body(STRING, "valueOf", ARGS(BYTE), "return HaxeNatives.str('' + p0);")
		body(STRING, "valueOf", ARGS(CHAR), "return HaxeNatives.str(String.fromCharCode(p0));")
		body(STRING, "valueOf", ARGS(SHORT), "return HaxeNatives.str('' + p0);")
		body(STRING, "valueOf", ARGS(INT), "return HaxeNatives.str('' + p0);")
		body(STRING, "valueOf", ARGS(FLOAT), "return HaxeNatives.str('' + p0);")
		body(STRING, "valueOf", ARGS(DOUBLE), "return HaxeNatives.str('' + p0);")
		body(STRING, "valueOf", ARGS(LONG), "return HaxeNatives.str('' + p0);")
		body(STRING, "valueOf", ARGS(OBJECT), "return HaxeNatives.str('' + p0);")
		body(STRING, "toString", ARGS(), "return this;")
		body(STRING, "toUpperCase", ARGS(), "return make(_str.toUpperCase());")
		body(STRING, "toLowerCase", ARGS(), "return make(_str.toLowerCase());")
		body(STRING, "toUpperCase", ARGS(LOCALE), "return make(_str.toUpperCase());")
		body(STRING, "toLowerCase", ARGS(LOCALE), "return make(_str.toLowerCase());")
		body(BOOL, "equals", ARGS(OBJECT), "return Std.is(p0, java_.lang.String_) && (cast(p0, java_.lang.String_)._str == this._str);")

		body("<init>", "*", "throw 'Not implemented this String.constructor';")
		body(VOID, "<init>", ARGS(), "this._str = '';")
		body(VOID, "<init>", ARGS(REF("java.lang.StringBuilder")), "this._str = p0._str;")
		body(VOID, "<init>", ARGS(STRING), "this._str = p0._str;")
		body(VOID, "<init>", ARGS(ARRAY(CHAR)), "this._str = HaxeNatives.charArrayToString(p0);")
		body(VOID, "<init>", ARGS(ARRAY(CHAR), INT, INT), "this._str = HaxeNatives.charArrayToString(p0, p1, p2);")
		body(VOID, "<init>", ARGS(ARRAY(INT), INT, INT), "this._str = HaxeNatives.intArrayToString(p0, p1, p2);")
		body(VOID, "<init>", ARGS(BYTEARRAY), "this._str = HaxeNatives.byteArrayToString(p0);")
		body(VOID, "<init>", ARGS(BYTEARRAY, INT, INT), "this._str = HaxeNatives.byteArrayToString(p0, p1, p2);")
		body(VOID, "<init>", ARGS(BYTEARRAY, INT, INT, STRING), "this._str = HaxeNatives.byteArrayToString(p0, p1, p2, p3._str);")
		body(VOID, "<init>", ARGS(BYTEARRAY, INT, INT, CHARSET), "this._str = HaxeNatives.byteArrayToString(p0, p1, p2, p3.canonicalName._str);")
		body(VOID, "<init>", ARGS(BYTEARRAY, STRING), "this._str = HaxeNatives.byteArrayToString(p0, 0, -1, p1._str);")
		body(VOID, "<init>", ARGS(BYTEARRAY, CHARSET), "this._str = HaxeNatives.byteArrayToString(p0, 0, -1, p1.canonicalName._str);")

		body(BYTEARRAY, "getBytes", ARGS(STRING), "return HaxeNatives.stringToByteArray(this._str, p0._str);")
		body(BYTEARRAY, "getBytes", ARGS(CHARSET), "return HaxeNatives.stringToByteArray(this._str, p0.canonicalName._str);")
		body(BYTEARRAY, "getBytes", ARGS(), "return HaxeNatives.stringToByteArray(this._str);")

		body(INT, "indexOf", ARGS(INT), "return _str.indexOf(String.fromCharCode(p0));")

		body(INT, "indexOf", ARGS(INT), "return _str.indexOf(String.fromCharCode(p0));")
		body(INT, "indexOf", ARGS(INT, INT), "return _str.indexOf(String.fromCharCode(p0), p1);")
		body(INT, "indexOf", ARGS(STRING), "return _str.indexOf(p0._str);")
		body(INT, "indexOf", ARGS(STRING, INT), "return _str.indexOf(p0._str, p1);")
		body(INT, "lastIndexOf", ARGS(INT), "return _str.lastIndexOf(String.fromCharCode(p0));")
		body(INT, "lastIndexOf", ARGS(INT, INT), "return _str.lastIndexOf(String.fromCharCode(p0), p1);")
		body(STRING, "substring", ARGS(INT), "return make(_str.substring(p0));")
		body(STRING, "substring", ARGS(INT, INT), "return make(_str.substring(p0, p1));")
		body(CHARSEQUENCE, "subSequence", ARGS(INT, INT), "return make(_str.substring(p0, p1));")
		body(BOOL, "startsWith", ARGS(STRING), "return StringTools.startsWith(this._str, p0._str);")
		body(BOOL, "endsWith", ARGS(STRING), "return StringTools.endsWith(this._str, p0._str);")
		body(STRING, "replace", ARGS(CHAR, CHAR), "return HaxeNatives.str(StringTools.replace(this._str, String.fromCharCode(p0), String.fromCharCode(p1)));")
		body(INT, "compareTo", ARGS(STRING), "return HaxeNatives.strCompare(this._str, p0._str);")
		body(INT, "compareToIgnoreCase", ARGS(STRING), "return HaxeNatives.strCompareIgnoreCase(this._str, p0._str);")
	}

	mappings.map("java.util.Arrays") {
		/*
		body(VOID, "sort", ARGS(ARRAY(BYTE), INT, INT), "return p0.sort(p1, p2);")
		body(VOID, "sort", ARGS(ARRAY(SHORT), INT, INT), "return p0.sort(p1, p2);")
		body(VOID, "sort", ARGS(ARRAY(CHAR), INT, INT), "return p0.sort(p1, p2);")
		body(VOID, "sort", ARGS(ARRAY(INT), INT, INT), "return p0.sort(p1, p2);")
		body(VOID, "sort", ARGS(ARRAY(FLOAT), INT, INT), "return p0.sort(p1, p2);")
		body(VOID, "sort", ARGS(ARRAY(DOUBLE), INT, INT), "return p0.sort(p1, p2);")
		body(VOID, "sort", ARGS(ARRAY(LONG), INT, INT), "return p0.sort(p1, p2);")
		*/

		body(VOID, "sort", ARGS(ARRAY(OBJECT), INT, INT, COMPARATOR), """
			var array = cast(p0, HaxeArray);
			var start = p1;
			var end = p2;
			var comparator = p3;
			var slice = array.toArray().slice(start, end);
			haxe.ds.ArraySort.sort(slice, function(a, b) {
				return comparator.compare_Ljava_lang_Object_Ljava_lang_Object__I(cast a, cast b);
			});
			for (n in 0 ... slice.length) {
				array.set(start + n, slice[n]);
			}
		""")
	}

	/*
	mappings.map("java.util.AbstractCollection") {
		body(ARRAY(OBJECT), "toArray", ARGS(), """
			var iterator:$ITERATOR_TYPE = this.$ITERATOR();
			var out = [];
			while (iterator.$HAS_NEXT()) {
				var value = iterator.$NEXT();
				out.push(value);
			}
			return HaxeArray.fromArray(out);
		""")
	}
	*/

	mappings.map("java.lang.Float") {
		body(BOOL, "isNaN", ARGS(FLOAT), "return Math.isNaN(p0);")
		body(BOOL, "isFinite", ARGS(FLOAT), "return Math.isFinite(p0);")
		body(STRING, "toString", ARGS(FLOAT), "return HaxeNatives.str('' + p0);")
		body(STRING, "toString", ARGS(), "return HaxeNatives.str('' + this.value);")

		body(FLOAT, "intBitsToFloat", ARGS(INT), "return HaxeNatives.intBitsToFloat(p0);")
		body(INT, "floatToIntBits", ARGS(FLOAT), "return HaxeNatives.floatToIntBits(p0);")
	}

	mappings.map("java.lang.Double") {
		body(STRING, "toString", ARGS(DOUBLE), "return HaxeNatives.str('' + p0);")
		body(DOUBLE, "parseDouble", ARGS(STRING), "return Std.parseFloat(p0._str);")

		body(DOUBLE, "longBitsToDouble", ARGS(LONG), "return HaxeNatives.longBitsToDouble(p0);")
		body(LONG, "doubleToLongBits", ARGS(DOUBLE), "return HaxeNatives.doubleToLongBits(p0);")
	}

	mappings.map("java.lang.Class") {
		nativeMember("public var _hxClass:Class<Dynamic> = null;")
		nativeMember("public var _internalName = '';")
		nativeMember("public var _parent = null;")
		nativeMember("public var _interfaces = [];")
		nativeMember("public var _fields = [];")
		nativeMember("public var _modifiers = 0;")
		nativeMember("public var _methods = [];")
		nativeMember("public var _constructors = [];")
		nativeMember("public var _annotations = [];")
		//body(VOID, "<init>", ARGS(), "this._str = '';")
		//body(CLASS, "forName", ARGS(STRING), "return HaxeNatives.newInstance(this.name._str);")
		body(OBJECT, "newInstance", ARGS(), "return HaxeNatives.newInstance(this._internalName);")
		body(ARRAY(REF("java.lang.reflect.Method")), "getDeclaredMethods", ARGS(), "return HaxeArray.fromArray(_methods);")
		body(ARRAY(REF("java.lang.reflect.Field")), "getDeclaredFields", ARGS(), "return HaxeArray.fromArray(_fields);")
		body(ARRAY(REF("java.lang.reflect.Constructor")), "getDeclaredConstructors", ARGS(), "return HaxeArray.fromArray(_constructors);")
		body(ARRAY(REF("java.lang.annotation.Annotation")), "getDeclaredAnnotations", ARGS(), "return HaxeArray.fromArray(_annotations);")

		// Returns the Class representing the superclass of the entity (class, interface, primitive type or void) represented by this Class. If this Class represents either the Object class, an interface, a primitive type, or void, then null is returned. If this object represents an array class then the Class object representing the Object class is returned.
		body(CLASS, "getSuperclass", ARGS(), "return HaxeNatives.resolveClass(_parent);")
		body(INT, "getModifiers", ARGS(), "return _modifiers;")

		body(BOOL, "isInstance", ARGS(OBJECT), "return Std.is(p0, _hxClass);")

		body(ARRAY(CLASS), "getInterfaces", ARGS(), "return HaxeArray.fromArray(Lambda.array(Lambda.map(_interfaces, function(i) { return HaxeNatives.resolveClass(i); })));")

		body(BOOL, "_check", ARGS(), "return HaxeReflectionInfo.__initClass(this);")
	}

	mappings.map("java.lang.reflect.Field") {
		nativeMember("public var _internalName = '';")
		nativeMember("public var _annotations = [];")

		body(OBJECT, "get", ARGS(OBJECT), """
			//trace('dynamic get : ' + this._internalName);
			return HaxeNatives.box(Reflect.field(p0, this._internalName));
		""")
		body(VOID, "set", ARGS(OBJECT, OBJECT), """
			//trace('dynamic set : ' + this._internalName);
			Reflect.setField(p0, this._internalName, p1);
		""")
	}

	mappings.map("java.lang.reflect.Constructor") {
		nativeMember("public var _internalName = '';")
		nativeMember("public var _annotations = [];")

		body(OBJECT, "newInstance", ARGS(ARRAY(OBJECT)), """
			//trace('dynamic newInstance : ' + this._internalName);
			var instance = Type.createEmptyInstance(Type.resolveClass(this.clazz._internalName));
			Reflect.callMethod(instance, Reflect.field(instance, this._internalName), p0.data.toArray());
			return instance;
		""")
	}

	mappings.map("java.util.regex.Matcher") {
		nativeMember("public var _ereg:EReg;")
		nativeMember("public var _matches:Bool;")
		nativeMember("public var _offset:Int = 0;")
		nativeMember("public var _matchPos:Int = 0;")
		nativeMember("public var _matchLen:Int = 0;")
		//public static final int UNIX_LINES = 0x01;
		//public static final int CASE_INSENSITIVE = 0x02;
		//public static final int COMMENTS = 0x04;
		//public static final int MULTILINE = 0x08;
		//public static final int LITERAL = 0x10;
		//public static final int DOTALL = 0x20;
		//public static final int UNICODE_CASE = 0x40;
		//public static final int CANON_EQ = 0x80;
		//public static final int UNICODE_CHARACTER_CLASS = 0x100;

		//i case insensitive matching
		//g global replace or split, see below
		//m multiline matching, ^ and $ represent the beginning and end of a line
		//s the dot . will also match newlines (Neko, C++, PHP, Flash and Java targets only)
		//u use UTF-8 matching (Neko and C++ targets only)

		body(VOID, "_init", ARGS(), """
			var opts = '';
			if ((this.flags & 0x02) != 0) opts += 'i';
			if ((this.flags & 0x08) != 0) opts += 'm';
			if ((this.flags & 0x20) != 0) opts += 's';

			this._ereg = new EReg(this.pattern._str, opts);
			this._matches = this._ereg.match(this.text._str);
		""")
		body(BOOL, "matches", ARGS(), "return this._matches;")
		body(INT, "start", ARGS(), "return this._matchPos;")
		body(INT, "end", ARGS(), "return this._matchPos + this._matchLen;")
		body(STRING, "group", ARGS(), "return HaxeNatives.str(this._ereg.matched(0));")
		body(STRING, "group", ARGS(INT), "return HaxeNatives.str(this._ereg.matched(p0));")
		nativeMember("""
		public function _find() {
			var r = this._ereg;
			this._matches = r.matchSub(this.text._str, this._offset);
			if (this._matches) {
				var rpos = r.matchedPos();
				this._matchPos = rpos.pos;
				this._matchLen = rpos.len;
				this._offset = rpos.pos + rpos.len;
			} else {
				this._matchPos = 0;
				this._matchLen = 0;
			}

			return this._matches;
		}
		""")
		body(BOOL, "find", ARGS(), "return _find();")
		body(BOOL, "find", ARGS(INT), "this._offset = p0; return _find();")
	}

	mappings.map("jtransc.FastMemory") {
		removeField("data")
		nativeMember("public var _length:Int;")
		nativeMember("public var _data:haxe.io.Bytes;")
		nativeMember("public var shortData:haxe.io.UInt16Array;")
		nativeMember("public var intData:haxe.io.Int32Array;")
		nativeMember("public var floatData:haxe.io.Float32Array;")
		nativeMember("public var doubleData:haxe.io.Float64Array;")
		body(VOID, "<init>", ARGS(INT), """
			this._length = p0;
			this._data = haxe.io.Bytes.alloc((p0 + 7) & ~7);
			this.shortData = haxe.io.UInt16Array.fromBytes(this._data);
			this.intData = haxe.io.Int32Array.fromBytes(this._data);
			this.floatData = haxe.io.Float32Array.fromBytes(this._data);
			this.doubleData = haxe.io.Float64Array.fromBytes(this._data);
		""")
		body(INT, "getLength", ARGS(), "return this._length;")
		body(INT, "getAllocatedLength", ARGS(), "return this._data.length;")

		// Unaligned
		body(BYTE, "getInt8", ARGS(INT), "return this._data.get(p0);")
		body(SHORT, "getInt16", ARGS(INT), "return (this._data.getUInt16(p0) << 16) >> 16;")
		body(INT, "getInt32", ARGS(INT), "return this._data.getInt32(p0);")
		body(LONG, "getInt64", ARGS(INT), "return this._data.getInt64(p0);")
		body(FLOAT, "getFloat32", ARGS(INT), "return this._data.getFloat(p0);")
		body(DOUBLE, "getFloat64", ARGS(INT), "return this._data.getDouble(p0);")

		body(VOID, "setInt8", ARGS(INT, BYTE), "this._data.set(p0, p1);")
		body(VOID, "setInt16", ARGS(INT, SHORT), "this._data.setUInt16(p0, p1);")
		body(VOID, "setInt32", ARGS(INT, INT), "this._data.setInt32(p0, p1);")
		body(VOID, "setInt64", ARGS(INT, LONG), "this._data.setInt64(p0, p1);")
		body(VOID, "setFloat32", ARGS(INT, FLOAT), "this._data.setFloat(p0, p1);")
		body(VOID, "setFloat64", ARGS(INT, DOUBLE), "this._data.setDouble(p0, p1);")

		// Aligned
		body(BYTE, "getAlignedInt8", ARGS(INT), "return this._data.get(p0);")
		body(SHORT, "getAlignedInt16", ARGS(INT), "return (this.shortData.get(p0) << 16) >> 16;")
		body(INT, "getAlignedInt32", ARGS(INT), "return this.intData.get(p0);")
		body(LONG, "getAlignedInt64", ARGS(INT), "return this._data.getInt64(p0 << 3);") // @TODO: Optimize
		body(FLOAT, "getAlignedFloat32", ARGS(INT), "return this.floatData.get(p0);")
		body(DOUBLE, "getAlignedFloat64", ARGS(INT), "return this.doubleData.get(p0);")

		body(VOID, "setAlignedInt8", ARGS(INT, BYTE), "this._data.set(p0, p1);")
		body(VOID, "setAlignedInt16", ARGS(INT, SHORT), "this.shortData.set(p0, p1);")
		body(VOID, "setAlignedInt32", ARGS(INT, INT), "this.intData.set(p0, p1);")
		body(VOID, "setAlignedInt64", ARGS(INT, LONG), "this._data.setInt64(p0 << 3, p1);") // @TODO: Optimize
		body(VOID, "setAlignedFloat32", ARGS(INT, FLOAT), "this.floatData.set(p0, p1);")
		body(VOID, "setAlignedFloat64", ARGS(INT, DOUBLE), "this.doubleData.set(p0, p1);")
	}

	return mappings
}