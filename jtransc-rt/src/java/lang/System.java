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

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBodyPre;
import com.jtransc.io.JTranscConsolePrintStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
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

	public static long currentTimeMillis() {
		return (long) JTranscSystem.fastTime();
	}

	public static long nanoTime() {
		return currentTimeMillis() * 1000000L;
	}

	@HaxeMethodBody("HaxeNatives.arraycopy(p0, p1, p2, p3, p4);")
	public static native void arraycopy(Object src, int srcPos, Object dest, int destPos, int length);

	@HaxeMethodBody("return p0.__ID__ | 0;")
	public static native int identityHashCode(Object x);

	public static Properties getProperties() {
		return getProps();
	}

	public static void setProperties(Properties props) {
		Properties myprops = getProps();
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			myprops.put(entry.getKey(), entry.getValue());
		}
	}

	public static String getProperty(String prop) {
		return getProps().getProperty(prop);
	}

	public static String getProperty(String key, String def) {
		return getProps().getProperty(key, def);
	}

	static private Properties _props;

	static private void _setProperty(String key, String value) {
		_props.put(key, value);
	}

	static private Properties getProps() {
		if (_props == null) {
			_props = new Properties();

			_setProperty("os.arch", JTranscSystem.getArch());
			_setProperty("os.name", JTranscSystem.getOS());
			_setProperty("os.version", "0.1");
			_setProperty("java.runtime.name", JTranscSystem.getRuntimeName());
			_setProperty("java.vm.version", "1.7.0");
			_setProperty("java.runtime.version", "1.7.0");
			_setProperty("file.separator", JTranscSystem.fileSeparator());
			_setProperty("line.separator", JTranscSystem.lineSeparator());
			_setProperty("path.separator", JTranscSystem.pathSeparator());
			_setProperty("file.encoding", "UTF-8");
			_setProperty("java.home", JTranscSystem.getJavaHome());
			_setProperty("java.specification.name", JTranscSystem.getRuntimeName());
			_setProperty("java.specification.vendor", "jtransc");
			_setProperty("java.specification.version", "1.7");
			_setProperty("java.vendor", "jtransc");
			_setProperty("java.vendor.url", "http://github.com/jtransc/jtransc");
			_setProperty("java.vn.name", "haxe");
			_setProperty("java.vm.specification.name", "Jtransc/Haxe JVM emulator");
			_setProperty("java.vm.specification.vendor", "jtransc-haxe");
			_setProperty("java.vm.specification.version", "0.1");
			_setProperty("java.io.tmpdir", getenvs(new String[]{"TMPDIR", "TEMP", "TMP"}, "/tmp"));
			_setProperty("user.home", getenvs(new String[]{"HOME"}, "/tmp"));
			_setProperty("user.dir", getenvs(new String[]{"HOME"}, "/tmp"));
			_setProperty("user.name", getenvs(new String[]{"USERNAME", "USER"}, "username"));
		}
		return _props;
	}

	public static String setProperty(String key, String value) {
		return (String) getProps().setProperty(key, value);
	}

	public static String clearProperty(String key) {
		String old = getProperty(key);
		getProps().remove(key);
		return old;
	}

	@HaxeMethodBodyPre("var key = p0._str;")
	@HaxeMethodBody(target = "sys", value = "return HaxeNatives.str(Sys.getEnv(key));")
	@HaxeMethodBody(target = "js", value = "return HaxeNatives.str(untyped __js__(\"(typeof process != 'undefined') ? process.env[key] : null\"));")
	@HaxeMethodBody("return HaxeNatives.str(null);")
	@JTranscMethodBody(target = "js", value = "return N.str((typeof process != 'undefined') ? process.env[p0] : null);")
	native public static String getenv(String name);

	private static String getenvs(String[] names, String defaultValue) {
		for (String name : names) {
			String out = getenv(name);
			if (out != null) return out;
		}
		return defaultValue;
	}

	@HaxeMethodBody(target = "sys", value = "return HaxeNatives.hashMap(Sys.environment());")
	@HaxeMethodBody(target = "js", value = "return HaxeNatives.hashMap(untyped __js__(\"(typeof process != 'undefined') ? process.env : {}\"));")
	@HaxeMethodBody("return HaxeNatives.hashMap({});")
	native public static java.util.Map<String, String> getenv();

	@HaxeMethodBody(target = "sys", value = "Sys.exit(p0);")
	@HaxeMethodBody(target = "js", value = "untyped __js__(\"if (typeof process != 'undefined') process.exit(p0);\");")
	@HaxeMethodBody("throw 'EXIT!';")
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

	public static SecurityManager getSecurityManager() {
		return null;
	}
}
