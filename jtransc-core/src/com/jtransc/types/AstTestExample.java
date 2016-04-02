package com.jtransc.types;

import java.io.IOException;

public class AstTestExample {

	//return ((byte)com.jtransc.types.AstTestExample.checkDecode(((int)p0), ((java.lang.String)java.lang.Integer.parseInt(((int)p0), ((java.lang.String)p1)))));

	/*
	public static byte parseByte(String value, int radix) throws NumberFormatException {
		return (byte) checkDecode(value, Integer.parseInt(value, radix));
	}

	private static int checkDecode(String value, int decoded) throws NumberFormatException {
		return 0;
	}
	*/

	private void test() {
		_setProperty("java.io.tmpdir", getenvs(new String[]{"TMPDIR", "TEMP", "TMP"}, "/tmp"));
	}

	private void _setProperty(String prop, String name) {
	}

	private String getenvs(String[] prop, String def) {
		return null;
	}

}
