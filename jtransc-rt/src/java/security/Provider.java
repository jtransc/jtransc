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

package java.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public abstract class Provider extends Properties {
	private String name;
	private String info;
	private double version;

	protected Provider(String name, double version, String info) {
		this.name = name;
		this.version = version;
		this.info = info;
	}

	public String getName() {
		return name;
	}

	public double getVersion() {
		return version;
	}

	public String getInfo() {
		return info;
	}

	public String toString() {
		return name + " version " + version;
	}

	@Override
	public native synchronized void clear();

	@Override
	public native synchronized void load(InputStream inStream) throws IOException;

	@Override
	native public synchronized void putAll(Map<?, ?> t);

	@Override
	public native synchronized Set<Map.Entry<Object, Object>> entrySet();

	@Override
	public native Set<Object> keySet();

	@Override
	public native Collection<Object> values();

	@Override
	public native synchronized Object put(Object key, Object value);

	@Override
	public native synchronized Object remove(Object key);

	@Override
	public native Object get(Object key);

	@Override
	public native Enumeration<Object> keys();

	@Override
	public native Enumeration<Object> elements();

	public native String getProperty(String key);

	public synchronized native Service getService(String type, String algorithm);

	public native synchronized Set<Service> getServices();

	protected native synchronized void putService(Service s);

	protected native synchronized void removeService(Service s);

	public static class Service {
		public Service(Provider provider, String type, String algorithm, String className, List<String> aliases, Map<String, String> attributes) {
		}

		public native final String getType();

		public native final String getAlgorithm();

		public native final Provider getProvider();

		public native final String getClassName();

		public native final String getAttribute(String name);

		public native Object newInstance(Object constructorParameter) throws NoSuchAlgorithmException;

		public native boolean supportsParameter(Object parameter);

		public native String toString();
	}
}
