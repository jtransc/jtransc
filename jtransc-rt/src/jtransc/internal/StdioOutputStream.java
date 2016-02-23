package jtransc.internal;

import jtransc.annotation.haxe.HaxeMethodBody;

import java.io.OutputStream;

class StdioOutputStream extends OutputStream {
    public StdioOutputStream() {
    }

    @Override
    @HaxeMethodBody("HaxeNatives.outputChar(p0);")
    native public void write(int b);
}
