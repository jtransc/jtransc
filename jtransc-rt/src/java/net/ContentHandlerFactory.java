package java.net;

public interface ContentHandlerFactory {
	ContentHandler createContentHandler(String contentType);
}
