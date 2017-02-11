package com.jtransc.mix;

import com.jtransc.JTranscWrapped;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@HaxeAddMembers({
	"public var output:haxe.io.Output;"
})
class JTranscHaxeOutputStream extends OutputStream {
	@HaxeMethodBody("this.output = p0._wrapped;")
	public JTranscHaxeOutputStream(JTranscWrapped value) {
	}

	@Override
	@HaxeMethodBody("this.output.writeByte(p0);")
	public native void write(int b) throws IOException;
}