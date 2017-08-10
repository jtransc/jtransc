package jtransc.jtransc;

import com.jtransc.annotation.JTranscNativeName;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

@HaxeAddMembers({"static public var mynativeField:Int = 123;"})
public class HaxeNativeCallTest {
	static public void main(String[] args) {
		byte[] bytes = new byte[]{1, 2, 3};
		System.out.println("STATIC:" + HaxeAdler32.make(bytes));
		//HaxeAdler32 adler = new HaxeAdler32();
		HaxeAdler32 adler = HaxeAdler32Tools.create();
		adler.update(bytes, 0, 3);
		System.out.println("INSTANCE:" + adler.get());

		HashMap<String, HaxeAdler32> map = new HashMap<>();
		map.put("adler", adler);

		System.out.println("MAP:" + map.get("adler").get());

		//System.out.println(new JTranscWrapped(adler).hashCode());

		HaxeNativeCallTest.adler = adler;

		System.out.println("FIELD:" + HaxeNativeCallTest.adler.get());

		System.out.println("INPUT:" + HaxeAdler32.read(new ByteArrayInputStream(new byte[]{1, 2, 3, 4})).get());

		System.out.println(HaxeStringTools.htmlEscape("<hello>\"&\"</hello>"));
		System.out.println(HaxeStringTools.htmlEscape("<hello>\"&\"</hello>", true));
		System.out.println("mult:" + mult(7));
	}

	static private HaxeAdler32 adler;

	@JTranscNativeName("StringTools")
	static private class HaxeStringTools {
		native static String htmlEscape(String s);

		native static String htmlEscape(String s, boolean quotes);
	}

	static private class HaxeAdler32Tools {
		@HaxeMethodBody("return new haxe.crypto.Adler32();")
		native static public HaxeAdler32 create();
	}

	@JTranscNativeName("haxe.crypto.Adler32")
	private static class HaxeAdler32 {
		public HaxeAdler32() {
		}

		native public boolean equals(HaxeAdler32 that);

		native public int get();

		native public void update(byte[] b, int pos, int len);

		native static public int make(byte[] b);

		native static public HaxeAdler32 read(InputStream i);
	}

	@HaxeMethodBody("return mynativeField * p0;")
	static public native int mult(int p0);
}
