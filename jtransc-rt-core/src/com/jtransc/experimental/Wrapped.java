package com.jtransc.experimental;

import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeNativeConversion;

@HaxeNativeConversion(
	haxeType = "Dynamic",
	toHaxe = "((@self).value)",
	toJava = "({% CLASS com.jtransc.media.limelibgdx.gl.Wrapped %}.create(@self))"
)
@HaxeAddMembers({
	"public var value: Dynamic;",
	"static public function create(value: Dynamic) { var result = {% CONSTRUCTOR com.jtransc.media.limelibgdx.gl.Wrapped:()V %}(); result.value = value; return result; }"
})
public class Wrapped<T> {
	public Wrapped() {
	}
}
