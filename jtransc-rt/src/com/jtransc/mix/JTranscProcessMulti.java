package com.jtransc.mix;

import com.jtransc.JTranscProcess;
import com.jtransc.JTranscSystem;
import com.jtransc.JTranscWrapped;
import com.jtransc.annotation.JTranscAddMembers;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBodyList;
import com.jtransc.annotation.haxe.HaxeMethodBodyPre;
import com.jtransc.util.JTranscCollections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
@HaxeAddMembers({
	"#if sys public var process: sys.io.Process; #end"
})
@JTranscAddMembers(target = "d", value = {
	"ProcessPipes pipes;"
})
public class JTranscProcessMulti extends JTranscProcess {
	private JTranscWrapped processWrapped;

	@HaxeMethodBodyPre("" +
		"var cmd = N.toNativeString(p0);\n" +
		"var args = N.toNativeStrArray(p1);\n" +
		"var cwd = N.toNativeString(p2);\n" +
		"var env = N.mapToObject(p3);\n"
	)
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "sys", value = "return N.wrap(new sys.io.Process(cmd, args));"),
		@HaxeMethodBody(target = "js", value = "return N.wrap(untyped __js__(\"require('child_process')\").spawnSync(cmd, args, {cwd:cwd, env:env}));"),
		@HaxeMethodBody("return null;"),
	})
	@JTranscMethodBody(target = "js", value = {
		"return N.wrap(require('child_process').spawnSync(N.istr(p0), N.istrArray(p1), { cwd : N.istr(p2), env : {{ AWAIT }}(N.imap(p3)) }));"
	}, async = true)
	private native JTranscWrapped create(String cmd, String[] args, String cwd, Map<String, String> env);

	//private native JTranscWrapped createD(String cmd, String[] args, String cwd, Map<String, String> env);

	private InputStream stdout;
	private InputStream stderr;
	private OutputStream stdin;

	private String stdoutString;
	private String stderrString;

	private int exitCode;
	private int pid;

	private String[] cmds;

	static public class Creator {
		public JTranscProcessMulti start(JTranscProcessMulti process, List<String> cmds, Map<String, String> environment, String dir, ProcessBuilder.Redirect stdin, ProcessBuilder.Redirect stdout, ProcessBuilder.Redirect stderr, boolean redirectErrorStream) {
			process.cmds = cmds.toArray(new String[cmds.size()]);
			if (JTranscSystem.isCpp() || JTranscSystem.isD()) {
				process.__init();
				return process;
			} else {
				process.processWrapped = process.create(cmds.get(0), JTranscCollections.sliceArray(cmds, 1, new String[cmds.size() - 1]), dir, environment);
				if (JTranscSystem.isJs()) {
					process.stdoutString = process.processWrapped.get("stdout").toString();
					process.stderrString = process.processWrapped.get("stderr").toString();
					process.stdout = new ByteArrayInputStream(process.stdoutString.getBytes(Charset.forName("utf-8")));
					process.stderr = new ByteArrayInputStream(process.stderrString.getBytes(Charset.forName("utf-8")));
					process.stderr = null;
					process.exitCode = (Integer) process.processWrapped.get("status");
					process.pid = (Integer) (process.processWrapped.get("pid"));
				} else {
					process.stdin = new JTranscHaxeOutputStream((JTranscWrapped) process.processWrapped.get("stdin"));
					process.stdout = new JTranscHaxeInputStream((JTranscWrapped) process.processWrapped.get("stdout"));
					process.stderr = new JTranscHaxeInputStream((JTranscWrapped) process.processWrapped.get("stderr"));
				}
				return process;
			}
		}
	}

	private void __init() {
		genPipes();
		stdin = new ByteArrayOutputStream(0);
		stdout = genStdout();
		stderr = genStderr();
	}


	@JTranscMethodBody(target = "d", value = {
		"this.pipes = pipeShell(escapeShellCommand(N.istrArray2(this{% IFIELD #CLASS:cmds %})));"
	})
	private void genPipes() {

	}

	@JTranscMethodBody(target = "d", value = "return new {% CLASS com.jtransc.mix.DFileInputStream %}(this.pipes.stderr);")
	private InputStream genStderr() {
		return new ByteArrayInputStream(new byte[]{'d', 'u', 'm', 'm', 'y'});
	}

	@JTranscMethodBody(target = "d", value = "return new {% CLASS com.jtransc.mix.DFileInputStream %}(this.pipes.stdout);")
	private InputStream genStdout() {
		return new ByteArrayInputStream(new byte[]{'d', 'u', 'm', 'm', 'y'});
	}

	static public Creator creator = new Creator();

	public Process start(List<String> cmds, Map<String, String> environment, String startDirectory, ProcessBuilder.Redirect stdin, ProcessBuilder.Redirect stdout, ProcessBuilder.Redirect stderr, boolean redirectErrorStream) {
		return creator.start(this, cmds, environment, startDirectory, stdin, stdout, stderr, redirectErrorStream);
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
	//@HaxeMethodBody("return this{% IFIELD com.jtransc.io.JTranscProcess:exitCode %};")
	@JTranscMethodBody(target = "d", value = "return std.process.wait(this.pipes.pid);")
	public int exitValue() {
		return this.exitCode;
	}

	@HaxeMethodBody(target = "sys", value = "return this.process.getPid();")
	//@HaxeMethodBody("return this{% IFIELD com.jtransc.io.JTranscProcess:pid %};")
	@JTranscMethodBody(target = "d", value = "return this.pipes.pid.processID;")
	public int pid() {
		return this.pid;
	}

	@Override
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "sys", value = "this.process.kill();"),
		@HaxeMethodBody(""),
	})
	@JTranscMethodBody(target = "d", value = "std.process.kill(this.pipes.pid);")
	//@JTranscMethodBody(target = "js", value = "")
	public native void destroy();

	@JTranscMethodBody(target = "d", value = "return !std.process.tryWait(this.pipes.pid).terminated;")
	public boolean isAlive() {
		try {
			exitValue();
			return false;
		} catch (IllegalThreadStateException e) {
			return true;
		}
	}
}
