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

import jtransc.annotation.haxe.HaxeMethodBody;
import jtransc.io.JTranscConsolePrintStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class System {
	static public final InputStream in = new InputStream() {
		@Override
		public int read() throws IOException {
			throw new Error("Not implemented!");
		}
	};
	static public final PrintStream out = new JTranscConsolePrintStream(false);
	static public final PrintStream err = new JTranscConsolePrintStream(true);

	native public static void setIn(InputStream in);

	native public static void setOut(PrintStream out);

	native public static void setErr(PrintStream err);

	//native public static Console console();
	//native public static Channel inheritedChannel() throws IOException;

	//native public static void setSecurityManager(final SecurityManager s);
	//native public static SecurityManager getSecurityManager();

    @HaxeMethodBody(
            "#if sys\n" +
            "return HaxeNatives.floatToLong(Sys.time() * 1000);\n" +
            "#else\n" +
            "return HaxeNatives.floatToLong(Date.now().getTime());\n" +
            "#end\n"
    )
    //@JTranscMethodBody({
    //        "haxe-sys", "return HaxeNatives.floatToLong(Sys.time() * 1000);",
    //        "haxe", "return HaxeNatives.floatToLong(Date.now().getTime());"
    //})
	public static native long currentTimeMillis();

	public static long nanoTime() {
		return currentTimeMillis() * 1000000L;
	}

    @HaxeMethodBody("HaxeNatives.arraycopy(p0, p1, p2, p3, p4);")
	public static native void arraycopy(Object src, int srcPos, Object dest, int destPos, int length);

    @HaxeMethodBody("return p0.__ID__ | 0;")
	public static native int identityHashCode(Object x);

	native public static Properties getProperties();

	native public static String lineSeparator();

	native public static void setProperties(Properties props);

    @HaxeMethodBody("return HaxeNatives.str(HaxeNatives.getProperty(p0._str));")
	native public static String getProperty(String key);

	native public static String getProperty(String key, String def);

	native public static String setProperty(String key, String value);

	native public static String clearProperty(String key);

	native public static String getenv(String name);

	native public static java.util.Map<String, String> getenv();

	native public static void exit(int status);

    @HaxeMethodBody("")
    native public static void gc();

	native public static void runFinalization();

	@Deprecated
	native public static void runFinalizersOnExit(boolean value);

	public static void load(String filename) {

	}

	public static void loadLibrary(String libname) {

	}

	public static String mapLibraryName(String libname) {
		return libname;
	}
}
