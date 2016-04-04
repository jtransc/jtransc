package com.jtransc.types;

public class AstTestExample {
	static public void test1() {
		try {
			Integer.parseInt("+20 ", 16);
		} catch (NumberFormatException nfe) {
			System.out.println(nfe.getMessage());
		}
	}
}
