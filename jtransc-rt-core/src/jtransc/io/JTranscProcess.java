package jtransc.io;

import jtransc.JTranscWrapped;
import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

@HaxeAddMembers({
	"#if sys public var process: sys.io.Process; #end"
})
public class JTranscProcess extends Process {
	private JTranscWrapped processWrapped;

	@HaxeMethodBody("" +
		"#if sys return HaxeNatives.wrap(new sys.io.Process(HaxeNatives.toNativeString(p0), HaxeNatives.toNativeStrArray(p1)));\n" +
		"#else return null; \n" +
		"#end\n"
	)
	private native JTranscWrapped create(String cmd, String[] args);

	private JTranscHaxeInputStream stdout;
	private JTranscHaxeInputStream stderr;
	private JTranscHaxeOutputStream stdin;

	public Process start(String[] cmdarray, Map<String, String> environment, String dir, ProcessBuilder.Redirect[] redirects, boolean redirectErrorStream) {
		this.processWrapped = create(cmdarray[0], Arrays.copyOfRange(cmdarray, 1, cmdarray.length));
		this.stdin = new JTranscHaxeOutputStream((JTranscWrapped) this.processWrapped.access("stdin"));
		this.stdout = new JTranscHaxeInputStream((JTranscWrapped) this.processWrapped.access("stdout"));
		this.stderr = new JTranscHaxeInputStream((JTranscWrapped) this.processWrapped.access("stderr"));
		return this;
	}

	@Override
	public OutputStream getOutputStream() {
		return this.stdin;
	}

	@Override
	public InputStream getInputStream() {
		return this.stdout;
	}

	@Override
	public InputStream getErrorStream() {
		return this.stderr;
	}

	@Override
	public int waitFor() throws InterruptedException {
		return exitValue();
	}

	@Override
	@HaxeMethodBody("" +
		"#if sys return this.process.exitCode();\n" +
		"#else return -1;\n" +
		"#end\n"
	)
	public native int exitValue();

	@HaxeMethodBody("" +
		"#if sys return this.process.getPid();\n" +
		"#else return -1;\n" +
		"#end\n"
	)
	public native int pid();

	@Override
	@HaxeMethodBody("" +
		"#if sys this.process.kill();\n" +
		"#else \n" +
		"#end\n"
	)
	public native void destroy();
}

