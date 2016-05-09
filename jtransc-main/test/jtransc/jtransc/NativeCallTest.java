package jtransc.jtransc;

import com.jtransc.annotation.JTranscNativeClass;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.util.HashMap;

public class NativeCallTest {
	static public void main(String[] args) {
		HaxeBytes bytes = HaxeBytesTools.toBytes(new byte[]{1, 2, 3});
		System.out.println("STATIC:" + HaxeAdler32.make(bytes));
		//HaxeAdler32 adler = new HaxeAdler32();
		HaxeAdler32 adler = HaxeAdler32Tools.create();
		adler.update(bytes, 0, 3);
		System.out.println("INSTANCE:" + adler.get());

		HashMap<String, HaxeAdler32> map = new HashMap<>();
		map.put("adler", adler);

		System.out.println("MAP:" + map.get("adler").get());

		NativeCallTest.adler = adler;

		System.out.println("FIELD:" + NativeCallTest.adler.get());
	}

	static private HaxeAdler32 adler;

	static private class HaxeBytesTools {
		@HaxeMethodBody("return p0.getBytes();")
		native static private HaxeBytes toBytes(byte[] data);
	}

	static private class HaxeStringTools {
		@HaxeMethodBody("return p0._str;")
		native static private HaxeString toHaxe(String str);

		@HaxeMethodBody("return N.str(p0);")
		native static private String toJava(HaxeString str);
	}

	static private class HaxeAdler32Tools {
		@HaxeMethodBody("return new haxe.crypto.Adler32();")
		native static public HaxeAdler32 create();
	}

	@JTranscNativeClass("String")
	private static class HaxeString {
	}

	@JTranscNativeClass("haxe.io.Bytes")
	private static class HaxeBytes {
	}

	@JTranscNativeClass("haxe.io.Input")
	private static class HaxeInput {
	}

	@JTranscNativeClass("haxe.crypto.Adler32")
	private static class HaxeAdler32 {
		public HaxeAdler32() {
		}

		native public boolean equals(HaxeAdler32 that);

		native public int get();

		native public void update(HaxeBytes b, int pos, int len);

		native static public int make(HaxeBytes b);

		native static public HaxeAdler32 read(HaxeInput i);
	}
}
