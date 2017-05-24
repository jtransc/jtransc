package com.jtransc.io;

import com.jtransc.annotation.JTranscMethodBody;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class JTranscConsolePrintStream extends PrintStream {
	final boolean error;
	final ConsoleBaseStream stream;

	public JTranscConsolePrintStream(final boolean error) {
		this(error ? new ConsoleErrorStream() : new ConsoleOutputStream(), error);
	}

	private JTranscConsolePrintStream(ConsoleBaseStream stream, final boolean error) {
		super(stream);
		this.stream = stream;
		this.error = error;
	}

	@Override
	public void println(String x) {
		JTranscConsole.logOrError(stream.sb.toString() + x, error);
		stream.sb.setLength(0);
	}

	static private abstract class ConsoleBaseStream extends OutputStream {
		public StringBuilder sb = new StringBuilder();
		private final boolean error;

		public ConsoleBaseStream(boolean error) {
			this.error = error;
		}

		protected void _write(int b) throws IOException {
			char c = (char)b;
			if (c == '\n') {
				JTranscConsole.logOrError(sb.toString(), error);
				sb.setLength(0);
			} else {
				sb.append(c);
			}
		}
	}

	static private class ConsoleOutputStream extends ConsoleBaseStream {
		public ConsoleOutputStream() {
			super(false);
		}

		@Override
		@JTranscMethodBody(target = "dart", value = "stdout.writeCharCode(p0);")
		public void write(int b) throws IOException {
			_write(b);
		}
	}

	static private class ConsoleErrorStream extends ConsoleBaseStream {
		public ConsoleErrorStream() {
			super(true);
		}

		@Override
		@JTranscMethodBody(target = "dart", value = "stderr.writeCharCode(p0);")
		public void write(int b) throws IOException {
			_write(b);
		}
	}
}

