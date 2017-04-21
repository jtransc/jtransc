package java.lang.jtransc.charsets;

import com.jtransc.JTranscBits;

import java.io.ByteArrayOutputStream;
import java.lang.jtransc.JTranscCharset;

public class JTranscCharsetUTF16LE extends JTranscCharsetUTF16Base {
	public JTranscCharsetUTF16LE() {
		super(new String[] { "UTF-16LE", "UTF-16", "UnicodeLittleUnmarked", "X-UTF-16LE" }, false);
	}
}
