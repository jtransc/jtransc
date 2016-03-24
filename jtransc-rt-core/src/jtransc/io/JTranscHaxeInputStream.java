package jtransc.io;

import jtransc.JTranscWrapped;
import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

import java.io.IOException;
import java.io.InputStream;

@HaxeAddMembers({
	"public var input:haxe.io.Input;"
})
class JTranscHaxeInputStream extends InputStream {
	@HaxeMethodBody("this.input = p0._wrapped;")
	public JTranscHaxeInputStream(JTranscWrapped value) {
	}

	@Override
	@HaxeMethodBody("return this.input.readByte();")
	public native int read() throws IOException;
}