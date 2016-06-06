package com.jtransc.util;

public class JTranscMath {
	static public int clamp(int v, int min, int max) {
		return Math.min(Math.max(v, min), max);
	}
}