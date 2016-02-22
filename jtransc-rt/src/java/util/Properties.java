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

package java.util;

import java.io.*;

public class Properties extends Hashtable<Object, Object> {
	protected Properties defaults;

	public Properties() {
		this(null);
	}

	public Properties(Properties defaults) {
		this.defaults = defaults;
	}

	public synchronized Object setProperty(String key, String value) {
		return put(key, value);
	}

	native public synchronized void load(Reader reader) throws IOException;

	native public synchronized void load(InputStream inStream) throws IOException;

	@Deprecated
	native public void save(OutputStream out, String comments);

	native public void store(Writer writer, String comments);

	native public void store(OutputStream out, String comments) throws IOException;

	native public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException;

	native public void storeToXML(OutputStream os, String comment) throws IOException;

	native public void storeToXML(OutputStream os, String comment, String encoding) throws IOException;

	native public String getProperty(String key);

	native public String getProperty(String key, String defaultValue);

	native public Enumeration<?> propertyNames();

	native public Set<String> stringPropertyNames();

	native public void list(PrintStream out);

	native public void list(PrintWriter out);
}
