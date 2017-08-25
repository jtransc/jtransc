package com.jtransc.io;

import com.jtransc.annotation.JTranscMethodBody;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class JTranscConsolePrintStream extends PrintStream {
	final boolean error;
	final ConsoleStream stream;

	public JTranscConsolePrintStream(final boolean error) {
		this(new ConsoleStream(error), error);
	}

	private JTranscConsolePrintStream(ConsoleStream stream, final boolean error) {
		super(stream);
		this.stream = stream;
		this.error = error;
	}

	@Override
	public void println(String x) {
		synchronized (this) {
			JTranscConsole.logOrError(stream.sb.toString() + x, error);
			stream.sb.setLength(0);
		}
	}

	static private class ConsoleStream extends OutputStream {
		public final StringBuilder sb = new StringBuilder();
		private final boolean error;

		public ConsoleStream(boolean error) {
			this.error = error;
		}

		protected void _write(char c) throws IOException {
			if (c == '\n') {
				JTranscConsole.logOrError(sb.toString(), error);
				sb.setLength(0);
			} else {
				sb.append(c);
			}
		}

		@Override
		@JTranscMethodBody(target = "dart", value = "if (this{% IFIELD #CLASS:error %}) stderr.writeCharCode(p0); else stdout.writeCharCode(p0);")
		public void write(int b) throws IOException {
			synchronized (this) {
				_write((char) b);
			}
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			synchronized (this) {
				super.write(b, off, len);
			}
		}
	}
}

