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

package java.lang.reflect;

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.util.Objects;

public class Proxy implements java.io.Serializable {
	protected InvocationHandler h;

	protected Proxy(InvocationHandler h) {
		Objects.requireNonNull(h);
		this.h = h;
	}

	native public static Class<?> getProxyClass(ClassLoader loader, Class<?>... interfaces) throws IllegalArgumentException;

	// @NOTE: This class will be overwritten by the ProxyPlugin
	private static Object newProxyInstance(Class<?> ifc, InvocationHandler h) {
		return null;
	}

	public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h) throws IllegalArgumentException {
		if (interfaces.length != 1) throw new RuntimeException("JTransc just supports Proxies with 1 interface");
		return newProxyInstance(interfaces[0], h);
	}

	public static boolean isProxyClass(Class<?> cl) {
		return Proxy.class.isAssignableFrom(cl);
	}

	public static InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException {
		try {
			return ((Proxy) proxy).h;
		} catch (NullPointerException e) {
			throw new IllegalArgumentException();
		}
	}
}
