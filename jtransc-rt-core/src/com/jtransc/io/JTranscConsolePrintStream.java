package com.jtransc.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class JTranscConsolePrintStream extends PrintStream {
	final boolean error;
	final ConsoleOutputStream stream;

	public JTranscConsolePrintStream(final boolean error) {
		this(new ConsoleOutputStream(error), error);
	}

	private JTranscConsolePrintStream(ConsoleOutputStream stream, final boolean error) {
		super(stream);
		this.stream = stream;
		this.error = error;
	}

	@Override
	public void println(String x) {
		JTranscConsole.logOrError(stream.sb.toString() + x, error);
		stream.sb.setLength(0);
	}
}

class ConsoleOutputStream extends OutputStream {
	public StringBuilder sb = new StringBuilder();
	private final boolean error;

	public ConsoleOutputStream(boolean error) {
		this.error = error;
	}

	@Override
	public void write(int b) throws IOException {
		char c = (char)b;
		if (c == '\n') {
			JTranscConsole.logOrError(sb.toString(), error);
			sb.setLength(0);
		} else {
			sb.append(c);
		}
	}
}