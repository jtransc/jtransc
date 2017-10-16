package com.jtransc;

import com.jtransc.annotation.*;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.lang.reflect.Field;
import java.util.Objects;

@HaxeAddMembers({
	"public var _wrapped:Dynamic;",
	"static public function wrap(value:Dynamic) { var out = new {% CLASS com.jtransc.JTranscWrapped: %}(); out._wrapped = value; return out; }",
	"static public function unwrap(value:{% CLASS com.jtransc.JTranscWrapped: %}) { return value._wrapped; }"
})
@JTranscAddMembersList({
	@JTranscAddMembers(target = "cs", value = {
		"public object _wrapped;",
		"static public {% CLASS com.jtransc.JTranscWrapped: %} wrap(object value) { var o = new {% CLASS com.jtransc.JTranscWrapped: %}(); o._wrapped = value; return o; }",
		"static public object unwrap({% CLASS com.jtransc.JTranscWrapped: %} value) { return value._wrapped; }"
	}),
})
@JTranscKeep
public class JTranscWrapped {
	private Object item;

	@HaxeMethodBody("this._wrapped = p0;")
	@JTranscMethodBody(target = "js", value = "this._wrapped = p0;")
	@JTranscSync
	public JTranscWrapped(Object item) {
		this.item = item;
	}

	//@HaxeMethodBody("return N.box(Reflect.field(this._wrapped, p0._str));")
	@HaxeMethodBody("return N.box(Reflect.getProperty(this._wrapped, p0._str));")
	@JTranscMethodBody(target = "js", value = "return N.box({{ JC_COMMA }}this._wrapped[N.istr(p0)]);")
	@JTranscSync
	public Object get(String field) {
		try {
			Field f = item.getClass().getField(field);
			f.setAccessible(true);
			return f.get(this.item);
		} catch (Throwable e) {
			return null;
		}
	}

	@HaxeMethodBody("Reflect.setProperty(this._wrapped, p0._str, N.unbox(p1));")
	@JTranscMethodBody(target = "js", value = "this._wrapped[N.istr(p0)] = N.unbox(p1);")
	@JTranscSync
	public void set(String field, Object value) {
		try {
			item.getClass().getField(field).set(this.item, value);
		} catch (Throwable e) {
		}
	}

	//@HaxeMethodBody("return N.box(Reflect.field(this._wrapped, p0._str));")
	//public native Object access(String field);

	@HaxeMethodBody("return N.box(Reflect.callMethod(_wrapped, Reflect.field(_wrapped, N.toNativeString(p0)), N.toNativeUnboxedArray(p1)));")
	@JTranscMethodBody(target = "js", value = "return N.box({{ JC_COMMA }}this._wrapped[N.istr(p0)].apply(this._wrapped, N.unboxArray(p1.data)));")
	@JTranscAsync
	public native Object invoke(String name, Object... args);

	@HaxeMethodBody("return N.str('' + this._wrapped);")
	@JTranscMethodBody(target = "js", value = "return N.str('' + this._wrapped);")
	@JTranscAsync
	public String toString() {
		return Objects.toString(item);
	}
}
