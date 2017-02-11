package com.jtransc.cs;

import com.jtransc.JTranscProcess;
import com.jtransc.annotation.JTranscAddMembers;
import com.jtransc.annotation.JTranscMethodBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@JTranscAddMembers(target = "cs", value = {
	"System.Diagnostics.Process process;",
})
public class JTranscProcessCSharp extends JTranscProcess {
	@JTranscMethodBody(target = "cs", value = {
		"var startInfo = new System.Diagnostics.ProcessStartInfo();",
		"startInfo.FileName = N.istr(p0);",
		"startInfo.Arguments = N.istr(p1);",
		"startInfo.WorkingDirectory = N.istr(p3);",
		"startInfo.RedirectStandardOutput = true;",
		"startInfo.RedirectStandardError = true;",
		"startInfo.UseShellExecute = false;",
		"process = System.Diagnostics.Process.Start(startInfo);",
	})
	native private void _start(String command, String arguments, Map<String, String> environment, String startDirectory);

	@Override
	public Process start(List<String> command, Map<String, String> environment, String startDirectory, ProcessBuilder.Redirect stdin, ProcessBuilder.Redirect stdout, ProcessBuilder.Redirect stderr, boolean redirectErrorStream) {
		JTranscProcessCSharp process = new JTranscProcessCSharp();
		process._start(command.get(0), buildCommands(command.subList(1, command.size()).toArray(new String[0])), environment, startDirectory);
		return process;
	}

	@JTranscMethodBody(target = "cs", value = {
		"process.StandardInput.Write(p0);",
	})
	native private void writeOut(int b) throws IOException;

	@JTranscMethodBody(target = "cs", value = {
		"return process.StandardOutput.Read();",
	})
	native private int readIn() throws IOException;

	@JTranscMethodBody(target = "cs", value = {
		"return process.StandardError.Read();",
	})
	native private int readErr() throws IOException;

	@Override
	public OutputStream getOutputStream() {
		return new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				JTranscProcessCSharp.this.writeOut(b);
			}
		};
	}

	@Override
	public InputStream getInputStream() {
		return new InputStream() {
			@Override
			public int read() throws IOException {
				return JTranscProcessCSharp.this.readIn();
			}
		};
	}

	@Override
	public InputStream getErrorStream() {
		return new InputStream() {
			@Override
			public int read() throws IOException {
				return JTranscProcessCSharp.this.readErr();
			}
		};
	}

	@Override
	@JTranscMethodBody(target = "cs", value = {
		"process.WaitForExit();",
		"return process.ExitCode;",
	})
	native public int waitFor() throws InterruptedException;

	@JTranscMethodBody(target = "cs", value = {
		"return process.HasExited;",
	})
	native private boolean hasExited();

	@Override
	public int exitValue() {
		if (hasExited()) {
			return _exitValue();
		} else {
			throw new IllegalThreadStateException();
		}
	}

	// @TODO: Convert C# InvalidOperationException to
	@JTranscMethodBody(target = "cs", value = {
		"return process.ExitCode;",
	})
	native private int _exitValue();

	@Override
	@JTranscMethodBody(target = "cs", value = {
		"process.Kill();",
	})
	public void destroy() {
	}

	public boolean isAlive() {
		return !hasExited();
	}
}
