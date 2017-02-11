package com.jtransc.mix;

import com.jtransc.JTranscWrapped;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

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