package javatest;

import com.jtransc.util.JTranscHex;

public class ExtendedCharsetsTest {
	static public void main(String[] args) throws Throwable {
		System.out.println(JTranscHex.encode("｢HELLO WORLD｣あ｡".getBytes("Shift_JIS")));
		System.out.println(new String(new byte[] {(byte) 0xa2, 0x48, 0x45, 0x4c, 0x4c, 0x4f, 0x20, 0x57, 0x4f, 0x52, 0x4c, 0x44, (byte) 0xa3, (byte) 0x82, (byte) 0xa0, (byte) 0xa1}, "Shift_JIS"));
	}
}
