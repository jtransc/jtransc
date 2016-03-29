package java.util.zip;

import java.io.OutputStream;

public class GZIPOutputStream extends DeflaterOutputStream {
	public GZIPOutputStream(OutputStream out) {
		super(out);
	}
}
