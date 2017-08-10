package javatest.net;

import com.jtransc.io.JTranscConsole;

import java.net.URLDecoder;
import java.net.URLEncoder;

public class URLEncoderDecoderTest {
	static public void main(String[] args) throws Throwable {
		JTranscConsole.log(URLDecoder.decode("a%3Dbc%25a%C3%A1%26%E3%81%82+", "UTF-8"));
		JTranscConsole.log(URLEncoder.encode("áé a =%20&あ", "UTF-8"));

		JTranscConsole.log(URLDecoder.decode("a%3Dbc%25a%C3%A1%26%E3%81%82+", "CP866"));
		JTranscConsole.log(URLEncoder.encode("áé a =%20&あ", "CP866"));
	}
}
