package java.net.internal;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class Http extends HttpURLConnection {
    
    private ByteArrayOutputStream outputStream;
    private ByteArrayInputStream inputStream;
    private Map<String, String> responseHeaders;
    
    Http(URL url) {
        super(url);
        outputStream = new ByteArrayOutputStream();
        init(url.toString());
    }

    private native void init(String url);
    
    @Override
    public void setConnectTimeout(int timeout) {
        super.setConnectTimeout(timeout);
        setTimeout(timeout);
    }

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

    native private void setContentType(String contentType);

    native private void setUserAgent(String userAgent);

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

    private native void setHTTPMethod(String method);

    private native void setHTTPData(byte[] data);

    private native void load();

    private native boolean finished();

    private native void setResponseCode();

    private native void setResponseMessage();

    private native byte[] getResponseData();

    public String getHeaderField(String key) {
        return responseHeaders.get(key);
    }
    
    @Override
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
