package java.util.zip;

import java.io.IOException;
import java.io.InputStream;

public class GZIPInputStream extends InflaterInputStream {

	public GZIPInputStream(InputStream in, int size) throws IOException {
		super(in, null, size);
		//super(var1, new Inflater(true), var2);
		/*
		super(var1, new Inflater(true), var2);
		this.crc = new CRC32();
		this.closed = false;
		this.tmpbuf = new byte[128];
		this.usesDefaultInflater = true;
		this.readHeader(var1);
		*/
	}

	public GZIPInputStream(InputStream in) throws IOException {
		this(in, 512);
	}

	@Override
	native public int read() throws IOException;
}
