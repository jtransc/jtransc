/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package java.util;

@SuppressWarnings("unchecked")
public final class ServiceLoader<S> implements Iterable<S> {
	private final Class<S> service;
	private final List<S> list;

	private ServiceLoader(Class<S> service) {
		Objects.requireNonNull(service);
		this.service = service;
		this.list = (List<S>) Arrays.asList(getInstances(service.getName()));
		reload();
	}

	private Object[] getInstances(String fqname) {
		// @NOTE: This method body will be replaced by com.jtransc.plugin.service.ServiceLoaderJTranscPlugin
		return new Object[0];
	}

	public void reload() {
	}

	public Iterator<S> iterator() {
		return list.iterator();
	}

	public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader classLoader) {
		return load(service);
	}

	public static <S> ServiceLoader<S> load(Class<S> service) {
		//return ServiceLoader.load(service, Thread.currentThread().getContextClassLoader());
		//if (classLoader == null) classLoader = ClassLoader.getSystemClassLoader();
		return new ServiceLoader<S>(service);
	}

	public static <S> ServiceLoader<S> loadInstalled(Class<S> service) {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		if (cl != null) while (cl.getParent() != null) cl = cl.getParent();
		return ServiceLoader.load(service, cl);
	}

	public static <S> S loadFromSystemProperty(final Class<S> service) {
		try {
			final String className = System.getProperty(service.getName());
			if (className != null) {
				return (S) ClassLoader.getSystemClassLoader().loadClass(className).newInstance();
			}
			return null;
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	@Override
	public String toString() {
		return "ServiceLoader for " + service.getName();
	}
}
