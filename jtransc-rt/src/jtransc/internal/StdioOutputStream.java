package jtransc.internal;

import java.io.OutputStream;

class StdioOutputStream extends OutputStream {
	public StdioOutputStream() {
	}

	@Override
	native public void write(int b);
}
