package jtransc.micro;

import com.jtransc.io.JTranscConsole;
import javatest.misc.BenchmarkTest;

import java.util.Properties;

public class MicroHelloWorld {

	static private int a = 10;
	private int b = 11;

	static public void main(String[] args) {
		JTranscConsole.log(true);
		JTranscConsole.log(a);
		new MicroHelloWorld().demo();
	}

	private void demo() {
		JTranscConsole.log(b);
		//Properties properties = new Properties();
		//properties.setProperty("hello", "world");
		if (a < b) {
			JTranscConsole.log("HELLO");
		} else {
			JTranscConsole.log("WOOPS");
		}
		//BenchmarkTest.main(new String[0]);
	}
}
