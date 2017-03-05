package com.jtransc.crypto.org.bouncycastle;

public interface Memoable {
	Memoable copy();

	void reset(Memoable other);
}