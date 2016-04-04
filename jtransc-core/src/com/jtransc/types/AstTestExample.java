package com.jtransc.types;

public class AstTestExample extends Other {
	public AstTestExample append(char c) {
		super.append2(c);
		return this;
	}
}

class Other {
	public void append2(char c) {

	}
}