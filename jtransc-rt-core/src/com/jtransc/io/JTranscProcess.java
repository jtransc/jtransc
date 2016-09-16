package com.jtransc.io;

import com.jtransc.JTranscSystem;
import com.jtransc.JTranscWrapped;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.*;
import com.jtransc.util.JTranscCollections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("ConstantConditions")
@HaxeAddMembers({
	"#if sys public var process: sys.io.Process; #end"
})
public class JTranscProcess extends Process {
	private JTranscWrapped processWrapped;

	@HaxeMethodBodyPre("" +
		"var cmd = HaxeNatives.toNativeString(p0);\n" +
		"var args = HaxeNatives.toNativeStrArray(p1);\n" +
		"var cwd = HaxeNatives.toNativeString(p2);\n" +
		"var env = HaxeNatives.mapToObject(p3);\n"
	)
	@HaxeMethodBody(target = "sys", value = "return HaxeNatives.wrap(new sys.io.Process(cmd, args));")
	@HaxeMethodBody(target = "js", value = "return HaxeNatives.wrap(untyped __js__(\"require('child_process')\").spawnSync(cmd, args, {cwd:cwd, env:env}));")
	@HaxeMethodBody("return null;")
	@JTranscMethodBody(target = "js", value = {
		"return N.wrap(require('child_process').spawnSync(N.istr(p0), N.istrArray(p1), {cwd:N.istr(p2), env:N.imap(p3)}));"
	})
	private native JTranscWrapped create(String cmd, String[] args, String cwd, Map<String, String> env);

	private InputStream stdout;
	private InputStream stderr;
	private OutputStream stdin;

	private String stdoutString;
	private String stderrString;

	private int exitCode;
	private int pid;

	static public class Creator {
		public JTranscProcess start(JTranscProcess process, List<String> cmds, Map<String, String> environment, String dir, ProcessBuilder.Redirect stdin, ProcessBuilder.Redirect stdout, ProcessBuilder.Redirect stderr, boolean redirectErrorStream) {
			if (JTranscSystem.isCpp()) {
				process.stdin = new ByteArrayOutputStream(0);
				process.stdout = new ByteArrayInputStream(new byte[] { 'd', 'u', 'm', 'm', 'y' });
				process.stderr = new ByteArrayInputStream(new byte[0]);
				process.exitCode = -1;
				process.pid = -1;
				return process;
			} else {
				process.processWrapped = process.create(cmds.get(0), JTranscCollections.sliceArray(cmds, 1, new String[cmds.size() - 1]), dir, environment);
				if (JTranscSystem.isJs()) {
					process.stdoutString = Objects.toString(process.processWrapped.get("stdout"));
					process.stderrString = Objects.toString(process.processWrapped.get("stderr"));
					process.stdout = new ByteArrayInputStream(process.stdoutString.getBytes(Charset.forName("utf-8")));
					process.stderr = new ByteArrayInputStream(process.stderrString.getBytes(Charset.forName("utf-8")));
					process.stderr = null;
					process.exitCode = (Integer) process.processWrapped.get("status");
					process.pid = (int) process.processWrapped.get("pid");
				} else {
					process.stdin = new JTranscHaxeOutputStream((JTranscWrapped) process.processWrapped.get("stdin"));
					process.stdout = new JTranscHaxeInputStream((JTranscWrapped) process.processWrapped.get("stdout"));
					process.stderr = new JTranscHaxeInputStream((JTranscWrapped) process.processWrapped.get("stderr"));
				}
				return process;
			}
		}
	}

	static public Creator creator = new Creator();

	public Process start(List<String> cmds, Map<String, String> environment, String dir, ProcessBuilder.Redirect stdin, ProcessBuilder.Redirect stdout, ProcessBuilder.Redirect stderr, boolean redirectErrorStream) {
		return creator.start(this, cmds, environment, dir, stdin, stdout, stderr, redirectErrorStream);
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
	@HaxeMethodBody(target = "sys", value = "return this.process.exitCode();")
	//@HaxeMethodBody("return this.{% FIELD com.jtransc.io.JTranscProcess:exitCode %};")
	public int exitValue() {
		return this.exitCode;
	}

	@HaxeMethodBody(target = "sys", value = "return this.process.getPid();")
	//@HaxeMethodBody("return this.{% FIELD com.jtransc.io.JTranscProcess:pid %};")
	public int pid() {
		return this.pid;
	}

	@Override
	@HaxeMethodBody(target = "sys", value = "this.process.kill();")
	@HaxeMethodBody("")
	//@JTranscMethodBody(target = "js", value = "")
	public native void destroy();
}

