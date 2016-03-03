package jtransc;

import jtransc.annotation.JTranscKeep;
import jtransc.annotation.haxe.HaxeAddMembers;

@HaxeAddMembers({
	"public var _wrapped:Dynamic;",
	"static public function wrap(value:Dynamic) { var out = new JTranscWrapped_(); out._wrapped = value; return out; }",
	"static public function unwrap(value:JTranscWrapped_) { return value._wrapped; }"
})
@JTranscKeep
public class JTranscWrapped {
}
