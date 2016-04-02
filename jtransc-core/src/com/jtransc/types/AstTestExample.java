package com.jtransc.types;

import java.io.IOException;

public class AstTestExample {
	static Demo1 a = new Demo1(1);
	static Demo b = new Demo(2);
	static Demo c = new Demo(3);

	private void ensureOpen() throws IOException {
		throw new IOException("Stream closed");
	}

	static public Object demo() {
		String[] array = new String[] { "test" };
		Object[] array1 = (Object[]) array;
		return array1[0];
	}

	static class Demo1 {
		public Demo1(int a) {

		}
	}
	static class Demo {
		public Demo(int a) {

		}
	}
}
