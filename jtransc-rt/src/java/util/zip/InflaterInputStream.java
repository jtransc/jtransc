package java.util.zip;

import java.io.FilterInputStream;
import java.io.InputStream;

public class InflaterInputStream extends FilterInputStream {
	public InflaterInputStream(InputStream in, Inflater inf, int size) {
		super(in);
	}

	public InflaterInputStream(InputStream in, Inflater inf) {
		this(in, inf, 512);
	}
}
