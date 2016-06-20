package java.net;

import java.io.IOException;

public abstract class ContentHandler {
	public abstract Object getContent(URLConnection uConn) throws IOException;
	// Class arg not generified in the spec.
	@SuppressWarnings("unchecked")
	public Object getContent(URLConnection uConn, Class[] types)
		throws IOException {
		Object content = getContent(uConn);
		for (int i = 0; i < types.length; i++) {
			if (types[i].isInstance(content)) {
				return content;
			}
		}
		return null;
	}
}
