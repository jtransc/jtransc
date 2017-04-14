/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

import com.jtransc.JTranscProcess;
import com.jtransc.lang.JTranscObjects;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class ProcessBuilder {
	private List<String> command;
	private File directory;
	private Map<String, String> environment;
	private boolean redirectErrorStream;
	private Redirect stdin = Redirect.PIPE;
	private Redirect stdout = Redirect.PIPE;
	private Redirect stderr = Redirect.PIPE;

	public ProcessBuilder(String... command) {
		command(command);
	}

	public ProcessBuilder(List<String> command) {
		command(command);
	}

	public ProcessBuilder command(List<String> command) {
		this.command = command;
		return this;
	}

	public ProcessBuilder command(String... command) {
		this.command = new ArrayList<String>(Arrays.asList(command));
		return this;
	}

	public List<String> command() {
		return command;
	}

	public Map<String, String> environment() {
		if (environment == null) environment = new HashMap<String, String>();
		return environment;
	}

	ProcessBuilder environment(String[] envp) {
		environment = new HashMap<String, String>((envp != null) ? envp.length : 0);

		if (envp != null) {
			for (String envstring : envp) {
				int eqlsign = envstring.indexOf('=', 1);
				if (eqlsign >= 0) {
					String key = envstring.substring(0, eqlsign);
					String value = envstring.substring(eqlsign + 1);
					environment.put(key, value);
				}
			}
		}
		return this;
	}

	public File directory() {
		return directory;
	}

	public ProcessBuilder directory(File directory) {
		this.directory = directory;
		return this;
	}

	public ProcessBuilder redirectInput(Redirect source) {
		stdin = source;
		return this;
	}

	public ProcessBuilder redirectOutput(Redirect destination) {
		stdout = destination;
		return this;
	}

	public ProcessBuilder redirectError(Redirect destination) {
		stderr = destination;
		return this;
	}

	public ProcessBuilder redirectInput(File file) {
		return redirectInput(Redirect.from(file));
	}

	public ProcessBuilder redirectOutput(File file) {
		return redirectOutput(Redirect.to(file));
	}

	public ProcessBuilder redirectError(File file) {
		return redirectError(Redirect.to(file));
	}

	public Redirect redirectInput() {
		return stdin;
	}

	public Redirect redirectOutput() {
		return stdout;
	}

	public Redirect redirectError() {
		return stderr;
	}

	public ProcessBuilder inheritIO() {
		stdin = stdout = stderr = Redirect.INHERIT;
		return this;
	}

	public boolean redirectErrorStream() {
		return redirectErrorStream;
	}

	public ProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
		this.redirectErrorStream = redirectErrorStream;
		return this;
	}

	public Process start() throws IOException {
		try {
			JTranscProcess process = ServiceLoader.load(JTranscProcess.class).iterator().next();
			return process.start(
				command,
				environment,
				JTranscObjects.toStringOrNull(directory),
				stdin, stdout, stderr,
				redirectErrorStream
			);
		} catch (Throwable cause) {
			throw cause;
			//throw new IOException("Problem executing process", cause);
		}
	}


	@SuppressWarnings({"EqualsWhichDoesntCheckParameterClass", "WeakerAccess"})
	public static abstract class Redirect {
		public enum Type {PIPE, INHERIT, READ, WRITE, APPEND}

		public abstract Type type();

		public static final Redirect PIPE = new Redirect() {
			public Type type() {
				return Type.PIPE;
			}

			public String toString() {
				return type().toString();
			}
		};

		public static final Redirect INHERIT = new Redirect() {
			public Type type() {
				return Type.INHERIT;
			}

			public String toString() {
				return type().toString();
			}
		};

		public File file() {
			return null;
		}

		public static Redirect from(final File file) {
			return new RedirectImpl(Type.READ, file);
		}

		public static Redirect to(final File file) {
			return new RedirectImpl(Type.WRITE, file);
		}

		public static Redirect appendTo(final File file) {
			return new RedirectImpl(Type.APPEND, file);
		}

		public boolean equals(Object that) {
			return JTranscObjects.equalsShape(this, that) && (((Redirect) that).type() == this.type()) && Objects.equals(this.file(), ((Redirect) that).file());
		}

		public int hashCode() {
			File file = file();
			if (file == null) {
				return super.hashCode();
			} else {
				return file.hashCode();
			}
		}

		@Override
		public String toString() {
			return type() + " : " + file();
		}
	}

	static private class RedirectImpl extends Redirect {
		Type type;
		File file;

		RedirectImpl(Type type, File file) {
			this.type = type;
			this.file = file;
		}

		@Override
		public Type type() {
			return type;
		}

		@Override
		public File file() {
			return file;
		}
	}
}

