package com.jtransc.types;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AstTestExample extends AstTestExample2 {

	//return ((byte)com.jtransc.types.AstTestExample.checkDecode(((int)p0), ((java.lang.String)java.lang.Integer.parseInt(((int)p0), ((java.lang.String)p1)))));

	/*
	public static byte parseByte(String value, int radix) throws NumberFormatException {
		return (byte) checkDecode(value, Integer.parseInt(value, radix));
	}

	private static int checkDecode(String value, int decoded) throws NumberFormatException {
		return 0;
	}
	*/

	/*
	private void test() {
		_setProperty("java.io.tmpdir", getenvs(new String[]{"TMPDIR", "TEMP", "TMP"}, "/tmp"));
	}

	private void _setProperty(String prop, String name) {
	}

	private String getenvs(String[] prop, String def) {
		return null;
	}
	*/

	/*
	public static int compare(boolean l, boolean r) {
		return (l == r) ? 0 : ((!l) ? -1 : +1);
	}

	public static boolean logicalAnd(boolean l, boolean r) {
		return l & r;
	}

	public static boolean logicalOr(boolean l, boolean r) {
		return l | r;
	}

	public static boolean logicalXor(boolean l, boolean r) {
		return l ^ r;
	}

	private boolean bigEndian;
	private boolean nativeByteOrder;

	public final AstTestExample order(ByteOrder bo) {
		bigEndian = (bo == ByteOrder.BIG_ENDIAN);
		return this;
	}
	*/
	private AstTestExample(String path, String reason) {
		super(path + ((reason == null) ? "" : " (" + reason + ")"));
	}


}

class AstTestExample2 {
	public AstTestExample2(String str) {
	}
}