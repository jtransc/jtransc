package jtransc;

import jtransc.annotation.JTranscKeep;
import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

@HaxeAddMembers({
	"public var _wrapped:Dynamic;",
	"static public function wrap(value:Dynamic) { var out = new JTranscWrapped_(); out._wrapped = value; return out; }",
	"static public function unwrap(value:JTranscWrapped_) { return value._wrapped; }"
})
@JTranscKeep
public class JTranscWrapped {
	private Object item;

	@HaxeMethodBody("this._wrapped = p0;")
	public JTranscWrapped(Object item) {
		this.item = item;
	}

	@HaxeMethodBody("return HaxeNatives.box(Reflect.field(this._wrapped, p0._str));")
	public Object access(String field) {
		try {
			return item.getClass().getField(field).get(this.item);
		} catch (Throwable e) {
			return null;
		}
	}

	//@HaxeMethodBody("return HaxeNatives.box(Reflect.field(this._wrapped, p0._str));")
	//public native Object access(String field);

	@HaxeMethodBody("return HaxeNatives.box(Reflect.callMethod(_wrapped, Reflect.field(_wrapped, HaxeNatives.toNativeString(p0)), HaxeNatives.toNativeUnboxedArray(p1)));")
	public native Object invoke(String name, Object... args);
}
