package com.jtransc;

import com.jtransc.annotation.*;



import java.lang.reflect.Field;
import java.util.Objects;

@JTranscAddMembers(target = "cs", value = {
	"public object _wrapped;",
	"static public {% CLASS com.jtransc.JTranscWrapped: %} wrap(object value) { var o = new {% CLASS com.jtransc.JTranscWrapped: %}(); o._wrapped = value; return o; }",
	"static public object unwrap({% CLASS com.jtransc.JTranscWrapped: %} value) { return value._wrapped; }"
})
@JTranscKeep
public class JTranscWrapped {
	private Object item;


	@JTranscMethodBody(target = "js", value = "this._wrapped = p0;")
	public JTranscWrapped(Object item) {
		this.item = item;
	}

	//

	@JTranscMethodBody(target = "js", value = "return N.box(this._wrapped[N.istr(p0)]);")
	public Object get(String field) {
		try {
			Field f = item.getClass().getField(field);
			f.setAccessible(true);
			return f.get(this.item);
		} catch (Throwable e) {
			return null;
		}
	}


	@JTranscMethodBody(target = "js", value = "this._wrapped[N.istr(p0)] = N.unbox(p1);")
	public void set(String field, Object value) {
		try {
			item.getClass().getField(field).set(this.item, value);
		} catch (Throwable e) {
		}
	}

	//
	//public native Object access(String field);


	@JTranscMethodBody(target = "js", value = "return N.box(this._wrapped[N.istr(p0)].apply(this._wrapped, N.unboxArray(p1.data)));")
	public native Object invoke(String name, Object... args);


	@JTranscMethodBody(target = "js", value = "return N.str('' + this._wrapped);")
	public String toString() {
		return Objects.toString(item);
	}
}
