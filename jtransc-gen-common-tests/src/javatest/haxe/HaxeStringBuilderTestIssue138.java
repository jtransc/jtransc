package javatest.haxe;

import com.jtransc.io.JTranscConsole;

public class HaxeStringBuilderTestIssue138 {
	static public void main(String[] args) {
		char[] test = {0x0054, 0x0065, 0x0073, 0x0074 , 0x0020, 0x00A0, 0x00A1, 0x00A2, 0x00A3, 0x00A4, 0x00A5, 0x00A6, 0x00A7, 0x00A8, 0x00A9, 0x00AA, 0x00C0, 0x00C1, 0x00C2, 0x00C3,};
		String testStr = new String(test);
		System.out.println(testStr);
		StringBuilder sb = new StringBuilder();
		for (char c : test) {
			sb.append(c);
		}
		//JTranscConsole.log(testStr);
		testStr = sb.toString();
		//JTranscConsole.log(testStr);
		System.out.println(testStr);
	}
}
