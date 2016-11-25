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
import com.jtransc.JTranscSystemProperties;
import com.jtransc.JTranscVersion;
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
			throw new Error("Not implemented System.in.read()!");
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
		return JTranscSystem.nanoTime();
	}

	@HaxeMethodBody("N.arraycopy(p0, p1, p2, p3, p4);")
	@JTranscMethodBody(target = "js", value = "N.arraycopy(p0, p1, p2, p3, p4);")
	@JTranscMethodBody(target = "d", value = "N.arraycopy(p0, p1, p2, p3, p4);")
	@JTranscMethodBody(target = "cpp", value = "JA_0::copy((JA_0*)p0.get(), p1, (JA_0*)p2.get(), p3, p4);")
	public static native void arraycopy(Object src, int srcPos, Object dest, int destPos, int length);

	//@JTranscMethodBody(target = "cpp", value = "return N::identityHashCode(p0);")
	public static int identityHashCode(Object x) {
		return (x != null) ? x.$$id : 0;
	}

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
		_setProperty(key, value, "");
	}

	static private void _setProperty(String key, String value, String defaultValue) {
		if (key == null) key = "";
		if (value == null) value = defaultValue;
		_props.put(key, value);
	}

	static private Properties getProps() {
		if (_props == null) {
			_props = new Properties();
			_setProperty("os.arch", JTranscSystem.getArch(), "unknown");
			_setProperty("os.name", JTranscSystem.getOS(), "unknown");
			_setProperty("os.version", "0.1");
			_setProperty("java.runtime.name", JTranscSystem.getRuntimeName(), "jtransc-unknown");
			_setProperty("java.vm.version", "1.7.0");
			_setProperty("java.runtime.version", "1.7.0");
			_setProperty("file.separator", JTranscSystem.fileSeparator(), "/");
			_setProperty("line.separator", JTranscSystem.lineSeparator(), "\n");
			_setProperty("path.separator", JTranscSystem.pathSeparator(), ":");
			_setProperty("file.encoding", JTranscSystemProperties.fileEncoding());
			_setProperty("java.home", JTranscSystem.getJavaHome(), "/");
			_setProperty("java.specification.name", JTranscSystem.getRuntimeName(), "jtransc-unknown");
			_setProperty("java.specification.vendor", "jtransc");
			_setProperty("java.specification.version", "1.7");
			_setProperty("java.vendor", "jtransc");
			_setProperty("java.vendor.url", "http://github.com/jtransc/jtransc");
			_setProperty("java.vm.name", "haxe");
			_setProperty("java.vm.specification.name", "Jtransc JVM emulator");
			_setProperty("java.vm.specification.vendor", "jtransc");
			_setProperty("java.vm.specification.version", JTranscVersion.getVersion());
			_setProperty("java.io.tmpdir", JTranscSystemProperties.tmpdir());
			_setProperty("user.home", JTranscSystemProperties.userHome());
			_setProperty("user.dir", JTranscSystemProperties.userDir());
			_setProperty("user.name", JTranscSystemProperties.userName());
			_setProperty("user.language", JTranscSystemProperties.userLanguage());
			_setProperty("user.region", JTranscSystemProperties.userRegion());
			_setProperty("user.variant", JTranscSystemProperties.userVariant());
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
	@HaxeMethodBody(target = "sys", value = "return N.str(Sys.getEnv(key));")
	@HaxeMethodBody(target = "js", value = "return N.str(untyped __js__(\"(typeof process != 'undefined') ? process.env[key] : null\"));")
	@HaxeMethodBody("return N.str(null);")
	@JTranscMethodBody(target = "js", value = "return N.str((typeof process != 'undefined') ? process.env[p0] : null);")
	@JTranscMethodBody(target = "cpp", value = {
		"auto str = N::istr3(p0);",
		"return N::str(std::getenv(str.c_str()));"
	})
	public static String getenv(String name) {
		return null;
	}

	@HaxeMethodBody(target = "sys", value = "return N.hashMap(Sys.environment());")
	@HaxeMethodBody(target = "js", value = "return N.hashMap(untyped __js__(\"(typeof process != 'undefined') ? process.env : {}\"));")
	@HaxeMethodBody("return N.hashMap({});")
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

	public static String lineSeparator() {
		return JTranscSystem.lineSeparator();
	}

}
