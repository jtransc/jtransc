package com.jtransc;

import com.jtransc.annotation.JTranscKeep;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.lang.reflect.Field;
import java.util.Objects;

@HaxeAddMembers({
	"public var _wrapped:Dynamic;",
	"static public function wrap(value:Dynamic) { var out = new {% CLASS com.jtransc.JTranscWrapped: %}(); out._wrapped = value; return out; }",
	"static public function unwrap(value:{% CLASS com.jtransc.JTranscWrapped: %}) { return value._wrapped; }"
})
@JTranscKeep
public class JTranscWrapped {
	private Object item;

	@HaxeMethodBody("this._wrapped = p0;")
	public JTranscWrapped(Object item) {
		this.item = item;
	}

	//@HaxeMethodBody("return HaxeNatives.box(Reflect.field(this._wrapped, p0._str));")
	@HaxeMethodBody("return HaxeNatives.box(Reflect.getProperty(this._wrapped, p0._str));")
	public Object get(String field) {
		try {
			Field f = item.getClass().getField(field);
			f.setAccessible(true);
			return f.get(this.item);
		} catch (Throwable e) {
			return null;
		}
	}

	@HaxeMethodBody("Reflect.setProperty(this._wrapped, p0._str, HaxeNatives.unbox(p1));")
	public void set(String field, Object value) {
		try {
			item.getClass().getField(field).set(this.item, value);
		} catch (Throwable e) {
		}
	}

	//@HaxeMethodBody("return HaxeNatives.box(Reflect.field(this._wrapped, p0._str));")
	//public native Object access(String field);

	@HaxeMethodBody("return HaxeNatives.box(Reflect.callMethod(_wrapped, Reflect.field(_wrapped, HaxeNatives.toNativeString(p0)), HaxeNatives.toNativeUnboxedArray(p1)));")
	public native Object invoke(String name, Object... args);

	@HaxeMethodBody("return HaxeNatives.str('' + this._wrapped);")
	public String toString() {
		return Objects.toString(item);
	}
}
