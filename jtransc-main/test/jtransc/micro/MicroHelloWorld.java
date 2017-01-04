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
		new MicroHelloWorld().demo2(new C());
	}

	private void demo2(A a) {
		a.test();
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

class A {
	public void test() {
		System.out.println("A");
	}
}

class B extends A {
}

class C extends B {
	public void test() {
		System.out.println("C");
	}
}
