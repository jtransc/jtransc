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

import jtransc.rt.StdioInputStream;
import jtransc.rt.StdioStream;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

public class System {
    public final static InputStream in = new StdioInputStream();
    static public final PrintStream out = new StdioStream();
    static public final PrintStream err = out;

    native public static void setIn(InputStream in);

    native public static void setOut(PrintStream out);

    native public static void setErr(PrintStream err);

    //native public static Console console();
    //native public static Channel inheritedChannel() throws IOException;

    //native public static void setSecurityManager(final SecurityManager s);
    //native public static SecurityManager getSecurityManager();

    public static native long currentTimeMillis();

    public static long nanoTime() {
        return currentTimeMillis() * 1000000L;
    }

    public static native void arraycopy(Object src, int srcPos, Object dest, int destPos, int length);

    public static native int identityHashCode(Object x);

    native public static Properties getProperties();

    native public static String lineSeparator();

    native public static void setProperties(Properties props);

    native public static String getProperty(String key);

    native public static String getProperty(String key, String def);

    native public static String setProperty(String key, String value);

    native public static String clearProperty(String key);

    native public static String getenv(String name);

    native public static java.util.Map<String, String> getenv();

    native public static void exit(int status);

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
