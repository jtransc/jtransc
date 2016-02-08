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

import java.util.Map;
import java.util.Set;

public final class Security {
	private Security() {
	}

	@Deprecated
	public native static String getAlgorithmProperty(String algName, String propName);

	public native static synchronized int insertProviderAt(Provider provider, int position);

	public native static int addProvider(Provider provider);

	public native static synchronized void removeProvider(String name);

	public native static Provider[] getProviders();

	public native static Provider getProvider(String name);

	public native static Provider[] getProviders(String filter);

	public native static Provider[] getProviders(Map<String, String> filter);

	static native Object[] getImpl(String algorithm, String type, String provider) throws NoSuchAlgorithmException, NoSuchProviderException;

	static native Object[] getImpl(String algorithm, String type, String provider, Object params) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException;

	static native Object[] getImpl(String algorithm, String type, Provider provider) throws NoSuchAlgorithmException;

	static native Object[] getImpl(String algorithm, String type, Provider provider, Object params) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

	public native static String getProperty(String key);

	public native static void setProperty(String key, String datum);

	static native String[] getFilterComponents(String filterKey, String filterValue);

	public native static Set<String> getAlgorithms(String serviceName);
}
