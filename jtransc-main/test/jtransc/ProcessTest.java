package jtransc;

import java.io.ByteArrayInputStream;

public class ProcessTest {
	static public void main(String[] args) throws Throwable {
		System.out.println("ProcessTest.main:");
		System.out.println(new ByteArrayInputStream(new byte[0]).read(new byte[1024]));
		ProcessBuilder pb = new ProcessBuilder("echo", "helloworld");
		System.out.println("[1]");
		pb.redirectInput();
		pb.redirectOutput();
		pb.redirectError();
		System.out.println("[2]");
		Process p = pb.start();
		System.out.println("[3]");
		p.waitFor();
		System.out.println("[4]");
		byte[] buffer = new byte[1024];
		System.out.println("[5]");
		int len = p.getInputStream().read(buffer);
		System.out.println("len:" + len);
		String out = new String(buffer, 0, len);
		for (int n = 0; n < len; n++) System.out.println("c[" + n + "]:" + (int)buffer[n]);
		System.out.println("[6]");
		System.out.println(out);
		System.out.println("[7]");
		System.out.println(p.exitValue());
		System.out.println("[8]");
	}
}
