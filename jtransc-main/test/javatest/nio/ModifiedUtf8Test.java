package javatest.nio;

import com.jtransc.charset.ModifiedUtf8;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class ModifiedUtf8Test {
	static public void main(String[] args) throws IOException {
		System.out.println("ModifiedUtf8Test.main:");
		expect(new byte[]{0, 5, 104, 101, 108, 108, 111}, "hello");
		expect(new byte[]{0, 7, 104, 101, 108, 108, 111, -64, -128}, "hello\0");
		//expect(new byte[]{0, 15, 104, 101, 108, 108, 111, -64, -128, -62, -128, -19, -81, -65, -19, -65, -65}, "hello\0\u0080\uDBFF\uDFFF"); // Surrogate pairs
		idemp("hello");
		idemp("hello\0");
		//idemp("hello\0\u0080\uDBFF\uDFFF");
		// C++ fails with surrogate pair literals!
	}

	static private void expect(final byte[] expect, final String str) throws IOException {
		byte[] modifiedUtf8FromJTransc = ModifiedUtf8.encode(str);

		System.out.println(Arrays.toString(expect));
		System.out.println(Arrays.toString(modifiedUtf8FromJTransc));
		System.out.println(Arrays.equals(expect, modifiedUtf8FromJTransc));
	}

	static private void idemp(final String str) throws IOException {
		byte[] encoded = ModifiedUtf8.encode(str);
		String decoded = ModifiedUtf8.decodeLen(encoded);
		System.out.println(Objects.equals(decoded, str));
	}
}
