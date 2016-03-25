package jtransc.io;

import jtransc.JTranscSystem;
import jtransc.JTranscWrapped;
import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@HaxeAddMembers({
	"#if sys public var process: sys.io.Process; #end"
})
public class JTranscProcess extends Process {
	private JTranscWrapped processWrapped;

	@HaxeMethodBody("" +
		"var cmd = HaxeNatives.toNativeString(p0);\n" +
		"var args = HaxeNatives.toNativeStrArray(p1);\n" +
		"var cwd = HaxeNatives.toNativeString(p2);\n" +
		"var env = HaxeNatives.mapToObject(p3);\n" +
		"#if sys return HaxeNatives.wrap(new sys.io.Process(cmd, args));\n" +
		"#elseif js return HaxeNatives.wrap(untyped __js__(\"require('child_process')\").spawnSync(cmd, args, {cwd:cwd, env:env}));\n" +
		"#else return null; \n" +
		"#end\n"
	)
	private native JTranscWrapped create(String cmd, String[] args, String cwd, Map<String, String> env);

	private InputStream stdout;
	private InputStream stderr;
	private OutputStream stdin;

	private String stdoutString;
	private String stderrString;

	private int exitCode;
	private int pid;

	public Process start(String[] cmdarray, Map<String, String> environment, String dir, ProcessBuilder.Redirect[] redirects, boolean redirectErrorStream) {
		this.processWrapped = create(cmdarray[0], Arrays.copyOfRange(cmdarray, 1, cmdarray.length), dir, environment);
		if (JTranscSystem.isJs()) {
			stdoutString = Objects.toString(this.processWrapped.get("stdout"));
			stderrString = Objects.toString(this.processWrapped.get("stderr"));
			this.stdout = new ByteArrayInputStream(stdoutString.getBytes(Charset.forName("utf-8")));
			this.stderr = new ByteArrayInputStream(stderrString.getBytes(Charset.forName("utf-8")));
			this.stderr = null;
			this.exitCode = (Integer)this.processWrapped.get("status");
			this.pid = (int) this.processWrapped.get("pid");
		} else {
			this.stdin = new JTranscHaxeOutputStream((JTranscWrapped) this.processWrapped.get("stdin"));
			this.stdout = new JTranscHaxeInputStream((JTranscWrapped) this.processWrapped.get("stdout"));
			this.stderr = new JTranscHaxeInputStream((JTranscWrapped) this.processWrapped.get("stderr"));
		}
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
		"#else return this.exitCode;\n" +
		"#end\n"
	)
	public native int exitValue();

	@HaxeMethodBody("" +
		"#if sys return this.process.getPid();\n" +
		"#else return this.pid;\n" +
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

