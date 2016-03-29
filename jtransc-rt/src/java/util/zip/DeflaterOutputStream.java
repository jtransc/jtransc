package java.util.zip;

import java.io.FilterOutputStream;
import java.io.OutputStream;

public class DeflaterOutputStream extends FilterOutputStream {
	public DeflaterOutputStream(OutputStream out) {
		super(out);
	}
}
