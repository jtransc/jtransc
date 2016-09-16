package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.security.AllPermission;
import java.security.Permission;

abstract public class HttpURLConnection extends URLConnection {

	protected String method = "GET";
	protected int chunkLength = -1;
	protected int fixedContentLength = -1;
	protected long fixedContentLengthLong = -1;

	native public String getHeaderFieldKey(int n);

	native public void setFixedLengthStreamingMode(int contentLength);

	native public void setFixedLengthStreamingMode(long contentLength);

	native public void setChunkedStreamingMode(int chunklen);

	native public String getHeaderField(int n);

	protected int responseCode = -1;

	protected String responseMessage = null;

	protected boolean instanceFollowRedirects = true;

	protected HttpURLConnection(URL u) {
		super(u);
	}

	native public static void setFollowRedirects(boolean set);

	native public static boolean getFollowRedirects();

	native public void setInstanceFollowRedirects(boolean followRedirects);

	native public boolean getInstanceFollowRedirects();

	native public void setRequestMethod(String method) throws ProtocolException;

	native public String getRequestMethod();

	native public int getResponseCode() throws IOException;

	native public String getResponseMessage() throws IOException;

	@SuppressWarnings("deprecation")
	native public long getHeaderFieldDate(String name, long Default);

	public abstract void disconnect();

	public abstract boolean usingProxy();

	public Permission getPermission() throws IOException {
		return new AllPermission();
	}

	public InputStream getErrorStream() {
		return null;
	}

	public static final int HTTP_OK = 200;
	public static final int HTTP_CREATED = 201;
	public static final int HTTP_ACCEPTED = 202;
	public static final int HTTP_NOT_AUTHORITATIVE = 203;
	public static final int HTTP_NO_CONTENT = 204;
	public static final int HTTP_RESET = 205;
	public static final int HTTP_PARTIAL = 206;
	public static final int HTTP_MULT_CHOICE = 300;
	public static final int HTTP_MOVED_PERM = 301;
	public static final int HTTP_MOVED_TEMP = 302;
	public static final int HTTP_SEE_OTHER = 303;
	public static final int HTTP_NOT_MODIFIED = 304;
	public static final int HTTP_USE_PROXY = 305;
	public static final int HTTP_BAD_REQUEST = 400;
	public static final int HTTP_UNAUTHORIZED = 401;
	public static final int HTTP_PAYMENT_REQUIRED = 402;
	public static final int HTTP_FORBIDDEN = 403;
	public static final int HTTP_NOT_FOUND = 404;
	public static final int HTTP_BAD_METHOD = 405;
	public static final int HTTP_NOT_ACCEPTABLE = 406;
	public static final int HTTP_PROXY_AUTH = 407;
	public static final int HTTP_CLIENT_TIMEOUT = 408;
	public static final int HTTP_CONFLICT = 409;
	public static final int HTTP_GONE = 410;
	public static final int HTTP_LENGTH_REQUIRED = 411;
	public static final int HTTP_PRECON_FAILED = 412;
	public static final int HTTP_ENTITY_TOO_LARGE = 413;
	public static final int HTTP_REQ_TOO_LONG = 414;
	public static final int HTTP_UNSUPPORTED_TYPE = 415;
	@Deprecated
	public static final int HTTP_SERVER_ERROR = 500;
	public static final int HTTP_INTERNAL_ERROR = 500;
	public static final int HTTP_NOT_IMPLEMENTED = 501;
	public static final int HTTP_BAD_GATEWAY = 502;
	public static final int HTTP_UNAVAILABLE = 503;
	public static final int HTTP_GATEWAY_TIMEOUT = 504;
	public static final int HTTP_VERSION = 505;

}
