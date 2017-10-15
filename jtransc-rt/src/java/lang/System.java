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
import com.jtransc.annotation.JTranscSync;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBodyPre;
import com.jtransc.io.JTranscConsolePrintStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("ManualArrayCopy")
public class System {
	static public InputStream in = new InputStream() {
		@Override
		public int read() throws IOException {
			throw new Error("Not implemented System.in.read()!");
		}
	};
	static public PrintStream out = new JTranscConsolePrintStream(false);
	static public PrintStream err = new JTranscConsolePrintStream(true);

	@JTranscSync
	public static void setIn(InputStream in) {
		System.in = in;
	}

	@JTranscSync
	public static void setOut(PrintStream out) {
		System.out = out;
	}

	@JTranscSync
	public static void setErr(PrintStream err) {
		System.err = err;
	}

	@JTranscMethodBody(target = "d", value = "return N.currentTimeMillis();")
	public static long currentTimeMillis() {
		return (long) JTranscSystem.fastTime();
	}

	@JTranscMethodBody(target = "d", value = "return N.nanoTime();")
	public static long nanoTime() {
		return JTranscSystem.nanoTime();
	}

	@HaxeMethodBody("N.arraycopy(p0, p1, p2, p3, p4);")
	@JTranscMethodBody(target = "js", value = "N.arraycopy(p0, p1, p2, p3, p4);")
	@JTranscMethodBody(target = "cpp", value = "JA_0::copy((JA_0*)p0, p1, (JA_0*)p2, p3, p4);")
	@JTranscMethodBody(target = "d", value = "N.arraycopy(p0, p1, p2, p3, p4);")
	@JTranscMethodBody(target = "cs", value = "N.arraycopy(p0, p1, p2, p3, p4);")
	@JTranscMethodBody(target = "as3", value = "N.arraycopy(p0, p1, p2, p3, p4);")
	@JTranscMethodBody(target = "dart", value = "N.arraycopy(p0, p1, p2, p3, p4);")
	@JTranscMethodBody(target = "php", value = "N::arraycopy($p0, $p1, $p2, $p3, $p4);")
	@JTranscSync
	public static void arraycopy(Object src, int srcPos, Object dst, int dstPos, int length) {
		boolean overlapping = (src == dst && dstPos > srcPos);
		if (src instanceof boolean[])
			arraycopy((boolean[]) src, srcPos, (boolean[]) dst, dstPos, length, overlapping);
		else if (src instanceof byte[]) arraycopy((byte[]) src, srcPos, (byte[]) dst, dstPos, length, overlapping);
		else if (src instanceof char[]) arraycopy((char[]) src, srcPos, (char[]) dst, dstPos, length, overlapping);
		else if (src instanceof short[]) arraycopy((short[]) src, srcPos, (short[]) dst, dstPos, length, overlapping);
		else if (src instanceof int[]) arraycopy((int[]) src, srcPos, (int[]) dst, dstPos, length, overlapping);
		else if (src instanceof long[]) arraycopy((long[]) src, srcPos, (long[]) dst, dstPos, length, overlapping);
		else if (src instanceof float[]) arraycopy((float[]) src, srcPos, (float[]) dst, dstPos, length, overlapping);
		else if (src instanceof double[])
			arraycopy((double[]) src, srcPos, (double[]) dst, dstPos, length, overlapping);
		else arraycopy((Object[]) src, srcPos, (Object[]) dst, dstPos, length, overlapping);
	}

	@JTranscSync
	static private void arraycopy(boolean[] src, int srcPos, boolean[] dst, int dstPos, int length, boolean overlapping) {
		if (overlapping) {
			int n = length;
			while (--n >= 0) dst[dstPos + n] = src[srcPos + n];
		} else {
			for (int n = 0; n < length; n++) dst[dstPos + n] = src[srcPos + n];
		}
	}

	@JTranscSync
	static private void arraycopy(byte[] src, int srcPos, byte[] dst, int dstPos, int length, boolean overlapping) {
		if (overlapping) {
			int n = length;
			while (--n >= 0) dst[dstPos + n] = src[srcPos + n];
		} else {
			for (int n = 0; n < length; n++) dst[dstPos + n] = src[srcPos + n];
		}
	}

	@JTranscSync
	static private void arraycopy(char[] src, int srcPos, char[] dst, int dstPos, int length, boolean overlapping) {
		if (overlapping) {
			int n = length;
			while (--n >= 0) dst[dstPos + n] = src[srcPos + n];
		} else {
			for (int n = 0; n < length; n++) dst[dstPos + n] = src[srcPos + n];
		}
	}

	@JTranscSync
	static private void arraycopy(short[] src, int srcPos, short[] dst, int dstPos, int length, boolean overlapping) {
		if (overlapping) {
			int n = length;
			while (--n >= 0) dst[dstPos + n] = src[srcPos + n];
		} else {
			for (int n = 0; n < length; n++) dst[dstPos + n] = src[srcPos + n];
		}
	}

	@JTranscSync
	static private void arraycopy(int[] src, int srcPos, int[] dst, int dstPos, int length, boolean overlapping) {
		if (overlapping) {
			int n = length;
			while (--n >= 0) dst[dstPos + n] = src[srcPos + n];
		} else {
			for (int n = 0; n < length; n++) dst[dstPos + n] = src[srcPos + n];
		}
	}

	@JTranscSync
	static private void arraycopy(long[] src, int srcPos, long[] dst, int dstPos, int length, boolean overlapping) {
		if (overlapping) {
			int n = length;
			while (--n >= 0) dst[dstPos + n] = src[srcPos + n];
		} else {
			for (int n = 0; n < length; n++) dst[dstPos + n] = src[srcPos + n];
		}
	}

	@JTranscSync
	static private void arraycopy(float[] src, int srcPos, float[] dst, int dstPos, int length, boolean overlapping) {
		if (overlapping) {
			int n = length;
			while (--n >= 0) dst[dstPos + n] = src[srcPos + n];
		} else {
			for (int n = 0; n < length; n++) dst[dstPos + n] = src[srcPos + n];
		}
	}

	@JTranscSync
	static private void arraycopy(double[] src, int srcPos, double[] dst, int dstPos, int length, boolean overlapping) {
		if (overlapping) {
			int n = length;
			while (--n >= 0) dst[dstPos + n] = src[srcPos + n];
		} else {
			for (int n = 0; n < length; n++) dst[dstPos + n] = src[srcPos + n];
		}
	}

	@JTranscSync
	static private void arraycopy(Object[] src, int srcPos, Object[] dst, int dstPos, int length, boolean overlapping) {
		if (overlapping) {
			int n = length;
			while (--n >= 0) dst[dstPos + n] = src[srcPos + n];
		} else {
			for (int n = 0; n < length; n++) dst[dstPos + n] = src[srcPos + n];
		}
	}

	@JTranscSync
	public static int identityHashCode(Object x) {
		return SystemInt.identityHashCode(x);
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
		getProps().put(key, value);
	}

	static private Properties getProps() {
		if (_props == null) {
			_props = new Properties();
			_setProperty("os.arch", JTranscSystem.getArch(), "unknown");
			_setProperty("os.name", JTranscSystem.getOS(), "unknown");
			_setProperty("os.version", "0.1");
			_setProperty("java.runtime.name", JTranscSystem.getRuntimeName(), "jtransc-unknown");
			_setProperty("java.version", "1.8.0_51");
			_setProperty("java.vm.version", "25.51-b03");
			_setProperty("java.runtime.version", "1.8.0_51-b16");
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

	@HaxeMethodBodyPre("var key = N.istr(p0);")
	@HaxeMethodBody(target = "sys", value = "return N.str(Sys.getEnv(key));")
	@HaxeMethodBody(target = "js", value = "return N.str(untyped __js__(\"(typeof process != 'undefined') ? process.env[N.istr(p0)] : null\"));")
	@HaxeMethodBody("return N.str(null);")
	@JTranscMethodBody(target = "js", value = "return N.str((typeof process != 'undefined') ? process.env[N.istr(p0)] : null);")
	@JTranscMethodBody(target = "cpp", value = {
		"auto str = N::istr3(p0);",
		"return N::str(std::getenv(str.c_str()));"
	})
	@JTranscMethodBody(target = "d", value = "return N.str(std.process.environment.get(N.istr2(p0)));")
	@JTranscMethodBody(target = "cs", value = "return N.str(Environment.GetEnvironmentVariable(N.istr(p0)));")
	@JTranscMethodBody(target = "as3", value = "return N.str(null);")
	@JTranscMethodBody(target = "dart", value = "return N.str(Platform.environment[N.istr(p0)]);")
	public static String getenv(String name) {
		return getenv().get(name);
	}

	@HaxeMethodBody(target = "sys", value = "return N.hashMap(Sys.environment());")
	@HaxeMethodBody(target = "js", value = "return N.hashMap(untyped __js__(\"(typeof process != 'undefined') ? process.env : {}\"));")
	@HaxeMethodBody("return N.hashMap({});")
	public static java.util.Map<String, String> getenv() {
		return new HashMap<>();
	}

	public static void exit(int status) {
		Runtime.getRuntime().exit(status);
	}

	public static void gc() {
		Runtime.getRuntime().gc();
	}

	public static void runFinalization() {
		Runtime.getRuntime().runFinalization();
	}

	@Deprecated
	public static void runFinalizersOnExit(boolean value) {
		Runtime.runFinalizersOnExit(value);
	}

	public static void load(String filename) {
		Runtime.getRuntime().load(filename);
	}

	public static void loadLibrary(String libname) {
		Runtime.getRuntime().loadLibrary(libname);
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
