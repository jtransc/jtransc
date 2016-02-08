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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ProcessBuilder {
    private List<String> command;
    private File directory;
    private Map<String, String> environment;
    private boolean redirectErrorStream;
    private Redirect[] redirects;

    public ProcessBuilder(List<String> command) {
        if (command == null)
            throw new NullPointerException();
        this.command = command;
    }

    public ProcessBuilder(String... command) {
        this.command = new ArrayList<String>(command.length);
        for (String arg : command)
            this.command.add(arg);
    }

    public ProcessBuilder command(List<String> command) {
        if (command == null) throw new NullPointerException();
        this.command = command;
        return this;
    }

    public ProcessBuilder command(String... command) {
        this.command = new ArrayList<String>(command.length);
        for (String arg : command) this.command.add(arg);
        return this;
    }

    public List<String> command() {
        return command;
    }

    public Map<String, String> environment() {
        if (environment == null)
            environment = ProcessEnvironment.environment();

        assert environment != null;

        return environment;
    }

    ProcessBuilder environment(String[] envp) {
        assert environment == null;
        if (envp != null) {
            environment = ProcessEnvironment.emptyEnvironment(envp.length);
            assert environment != null;

            for (String envstring : envp) {
                if (envstring.indexOf((int) '\u0000') != -1)
                    envstring = envstring.replaceFirst("\u0000.*", "");

                int eqlsign = envstring.indexOf('=', ProcessEnvironment.MIN_NAME_LENGTH);
                if (eqlsign != -1) environment.put(envstring.substring(0, eqlsign), envstring.substring(eqlsign + 1));
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

    static class NullInputStream extends InputStream {
        static final NullInputStream INSTANCE = new NullInputStream();

        private NullInputStream() {
        }

        public int read() {
            return -1;
        }

        public int available() {
            return 0;
        }
    }

    static class NullOutputStream extends OutputStream {
        static final NullOutputStream INSTANCE = new NullOutputStream();

        private NullOutputStream() {
        }

        public void write(int b) throws IOException {
            throw new IOException("Stream closed");
        }
    }

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

        boolean append() {
            throw new UnsupportedOperationException();
        }

        public static Redirect from(final File file) {
            if (file == null)
                throw new NullPointerException();
            return new Redirect() {
                public Type type() {
                    return Type.READ;
                }

                public File file() {
                    return file;
                }

                public String toString() {
                    return "redirect to read from file \"" + file + "\"";
                }
            };
        }

        public static Redirect to(final File file) {
            if (file == null)
                throw new NullPointerException();
            return new Redirect() {
                public Type type() {
                    return Type.WRITE;
                }

                public File file() {
                    return file;
                }

                public String toString() {
                    return "redirect to write to file \"" + file + "\"";
                }

                boolean append() {
                    return false;
                }
            };
        }

        public static Redirect appendTo(final File file) {
            if (file == null)
                throw new NullPointerException();
            return new Redirect() {
                public Type type() {
                    return Type.APPEND;
                }

                public File file() {
                    return file;
                }

                public String toString() {
                    return "redirect to append to file \"" + file + "\"";
                }

                boolean append() {
                    return true;
                }
            };
        }

        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof Redirect)) return false;
            Redirect r = (Redirect) obj;
            if (r.type() != this.type()) return false;
            assert this.file() != null;
            return this.file().equals(r.file());
        }

        public int hashCode() {
            File file = file();
            if (file == null) {
                return super.hashCode();
            } else {
                return file.hashCode();
            }
        }

        private Redirect() {
        }
    }

    private Redirect[] redirects() {
        if (redirects == null)
            redirects = new Redirect[]{Redirect.PIPE, Redirect.PIPE, Redirect.PIPE};
        return redirects;
    }

    public ProcessBuilder redirectInput(Redirect source) {
        if (source.type() == Redirect.Type.WRITE || source.type() == Redirect.Type.APPEND) {
            throw new IllegalArgumentException("Redirect invalid for reading: " + source);
        }
        redirects()[0] = source;
        return this;
    }

    public ProcessBuilder redirectOutput(Redirect destination) {
        if (destination.type() == Redirect.Type.READ)
            throw new IllegalArgumentException(
                    "Redirect invalid for writing: " + destination);
        redirects()[1] = destination;
        return this;
    }

    public ProcessBuilder redirectError(Redirect destination) {
        if (destination.type() == Redirect.Type.READ)
            throw new IllegalArgumentException("Redirect invalid for writing: " + destination);
        redirects()[2] = destination;
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
        return (redirects == null) ? Redirect.PIPE : redirects[0];
    }

    public Redirect redirectOutput() {
        return (redirects == null) ? Redirect.PIPE : redirects[1];
    }

    public Redirect redirectError() {
        return (redirects == null) ? Redirect.PIPE : redirects[2];
    }

    public ProcessBuilder inheritIO() {
        Arrays.fill(redirects(), Redirect.INHERIT);
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
        String[] cmdarray = command.toArray(new String[command.size()]);
        cmdarray = cmdarray.clone();

        for (String arg : cmdarray) {
            if (arg == null) throw new NullPointerException();
        }
        // Throws IndexOutOfBoundsException if command is empty
        String prog = cmdarray[0];

        String dir = directory == null ? null : directory.toString();

        for (int i = 1; i < cmdarray.length; i++) {
            if (cmdarray[i].indexOf('\u0000') >= 0) {
                throw new IOException("invalid null character in command");
            }
        }

        try {
            return ProcessImpl.start(cmdarray, environment, dir, redirects, redirectErrorStream);
        } catch (Exception e) {
            String exceptionInfo = ": " + e.getMessage();
            Throwable cause = e;
            throw new IOException("Cannot run program \"" + prog + "\"" + (dir == null ? "" : " (in directory \"" + dir + "\")") + exceptionInfo, cause);
        }
    }
}

class ProcessImpl {
    public static Process start(String[] cmdarray, Map<String, String> environment, String dir, ProcessBuilder.Redirect[] redirects, boolean redirectErrorStream) {
        return null;
    }
}