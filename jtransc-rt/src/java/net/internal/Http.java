package java.net.internal;

import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@HaxeAddMembers(
	"#if lime\n" +
		"\tvar HTTPRequest: lime.net.HTTPRequest<haxe.io.Bytes>;\n" +
		"\tvar future: lime.app.Future<haxe.io.Bytes>;\n" +
		"\t#end"
)
public class Http extends HttpURLConnection {

	private ByteArrayOutputStream outputStream;
	private ByteArrayInputStream inputStream;
	private Map<String, String> responseHeaders;

	Http(URL url) {
		super(url);
		outputStream = new ByteArrayOutputStream();
		init(url.toString());
	}

	@HaxeMethodBody(
		"#if lime\n" +
			"\t\tHTTPRequest = new lime.net.HTTPRequest<haxe.io.Bytes>(p0._str);\n" +
			"\t\tHTTPRequest.followRedirects = true;\n" +
			"\t\tHTTPRequest.enableResponseHeaders = true;\n" +
			"\t\t#end"
	)
	private native void init(String url);

	@Override
	public void setConnectTimeout(int timeout) {
		super.setConnectTimeout(timeout);
		setTimeout(timeout);
	}

	@HaxeMethodBody(
		"#if lime\n" +
			"\t\tHTTPRequest.timeout = p0;\n" +
			"\t\t#end"
	)
	private native void setTimeout(int timeout);

	@Override
	public void setRequestProperty(String header, String value) {
		super.setRequestProperty(header, value);
		if (header.equals("User-Agent")) {
			setUserAgent(value);
		} else if (header.equals("Content-Type")) {
			setContentType(value);
		} else {
			setHeader(header, value);
		}
	}

	@HaxeMethodBody(
		"#if lime\n" +
			"\t\tHTTPRequest.contentType = p0._str;\n" +
			"\t\t#end"
	)
	native private void setContentType(String contentType);

	@HaxeMethodBody(
		"#if lime\n" +
			"\t\tHTTPRequest.userAgent = p0._str;\n" +
			"\t\t#end"
	)
	native private void setUserAgent(String userAgent);

	@HaxeMethodBody(
		"#if lime\n" +
			"\t\tHTTPRequest.headers.push(new lime.net.HTTPRequestHeader(p0._str, p1._str));\n" +
			"\t\t#end"
	)
	native private void setHeader(String header, String value);

	@Override
	public InputStream getInputStream() throws IOException {
		if (inputStream != null) {
			inputStream.reset();
			return inputStream;
		}

		setHTTPMethod(method);
		setHTTPData(outputStream.toByteArray());
		load();
		while (!finished()) {
			try {
				Thread.sleep(50);
			} catch (Exception e) {
				// ignore
			}
		}
		setResponseCode();
		setResponseMessage();
		if (responseMessage != null) {
			throw new IOException(responseCode + ":" + responseMessage);
		}

		inputStream = new ByteArrayInputStream(getResponseData());
		return inputStream;
	}

	@HaxeMethodBody(
		"#if lime\n" +
			"\t\tvar str: String = p0._str;\n" +
			"\t\tif(str == 'GET') HTTPRequest.method = lime.net.HTTPRequestMethod.GET;\n" +
			"\t\telse if(str == 'POST') HTTPRequest.method = lime.net.HTTPRequestMethod.POST;\n" +
			"\t\telse if(str == 'PUT') HTTPRequest.method = lime.net.HTTPRequestMethod.PUT;\n" +
			"\t\telse if(str == 'DELETE') HTTPRequest.method = lime.net.HTTPRequestMethod.DELETE;\n" +
			"\t\telse if(str == 'HEAD') HTTPRequest.method = lime.net.HTTPRequestMethod.HEAD;\n" +
			"\t\telse if(str == 'OPTIONS') HTTPRequest.method = lime.net.HTTPRequestMethod.OPTIONS;\n" +
			"\t\t#end"
	)
	private native void setHTTPMethod(String method);

	@HaxeMethodBody(
		"#if lime\n" +
			"\t\tHTTPRequest.data = p0.data;\n" +
			"\t\t#end"
	)
	private native void setHTTPData(byte[] data);


	@HaxeMethodBody(
		"#if lime\n" +
			"\t\tfuture = HTTPRequest.load();\n" +
			"\t\t#end"
	)
	private native void load();


	@HaxeMethodBody(
		"#if lime\n" +
			"\t\treturn future.isComplete || future.isError;\n" +
			"\t\t#else\n" +
			"\t\treturn false;\n" +
			"\t\t#end"
	)
	private native boolean finished();


	@HaxeMethodBody(
		"#if lime\n" +
			"\t\tthis{% IFIELD java.net.HttpURLConnection:responseCode %} = HTTPRequest.responseStatus;\n" +
			"\t\t#end"
	)
	private native void setResponseCode();

	@HaxeMethodBody(
		"#if lime\n" +
			"\t\tif(future.error != null) this{% IFIELD java.net.HttpURLConnection:responseMessage %} = N.str(Std.string(future.error));\n" +
			"\t\t#end"
	)
	private native void setResponseMessage();

	@HaxeMethodBody(
		"#if lime\n" +
			"\t\treturn N.boxByteArray(HTTPRequest.responseData);\n" +
			"\t\t#else\n" +
			"\t\treturn null;\n" +
			"\t\t#end"
	)
	private native byte[] getResponseData();

	@Override
	@HaxeMethodBody(
		"#if lime\n" +
			"\t\tfor (header in HTTPRequest.headers) if (header.name == p0._str) return N.str(header.value);\n" +
			"\t\t#end\n" +
			"\t\treturn null;\n"
	)
	public String getHeaderField(String key) {
		return responseHeaders.get(key);
	}

	@Override
	@HaxeMethodBody(
		"#if lime\n" +
			"\t\tif (p0 < 0 || p0 >= HTTPRequest.headers.length) return null;\n" +
			"\t\treturn N.str(HTTPRequest.headers[p0].name);\n" +
			"\t\t#else\n" +
			"\t\treturn null;\n" +
			"\t\t#end"

	)
	public String getHeaderFieldKey(int n) {
		return null;
	}

	@Override
	public OutputStream getOutputStream() {
		return outputStream;
	}

	@Override
	public void disconnect() {
	}

	@Override
	public boolean usingProxy() {
		return false;
	}

	@Override
	public void connect() throws IOException {
		connected = true;
	}
}
