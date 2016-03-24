package jtransc;

public class ProcessTest {
	static public void main(String[] args) throws Throwable {
		ProcessBuilder pb = new ProcessBuilder("echo", "helloworld");
		pb.redirectInput();
		pb.redirectOutput();
		pb.redirectError();
		Process p = pb.start();
		p.waitFor();
		byte[] buffer = new byte[1024];
		String out = new String(buffer, 0, p.getInputStream().read(buffer));
		System.out.println(out);
		System.out.println(p.exitValue());
	}
}
