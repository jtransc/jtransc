package com.jtransc.mix;

import com.jtransc.annotation.JTranscAddMembers;
import com.jtransc.annotation.JTranscMethodBody;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("unused")
@JTranscAddMembers(target = "d", value = {
	"public File file;",
	"public this(File file) { this.file = file; }",
})
class DFileInputStream extends InputStream {
	@Override
	@JTranscMethodBody(target = "d", value = {
		"if (this.file.eof) {",
		"	return -1;",
		"} else {",
		"	scope b = new byte[1];",
		"	scope o = this.file.rawRead(b);",
		"	return (o.length >= 1) ? b[0] : -1;",
		"}",
	})
	native public int read() throws IOException;
}