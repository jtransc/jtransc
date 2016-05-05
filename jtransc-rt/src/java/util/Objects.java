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

import com.jtransc.annotation.haxe.HaxeMethodBody;

public final class Objects {
	private Objects() {
		throw new AssertionError("No java.util.Objects instances for you!");
	}

	@HaxeMethodBody("return (p0 == p1) || (p0 != null && p0.#METHOD:java.lang.Object:equals:(Ljava.lang.Object;)Z#(p1));")
	public static boolean equals(Object a, Object b) {
		return (a == b) || (a != null && a.equals(b));
	}

	public static boolean deepEquals(Object a, Object b) {
		if (a == b) return true;
		if (a == null || b == null) return false;
		return Arrays.deepEquals0(a, b);
	}

	@HaxeMethodBody("return (p0 != null) ? p0.#METHOD:java.lang.Object:hashCode:()I#() : 0;")
	public static int hashCode(Object o) {
		return o != null ? o.hashCode() : 0;
	}

	public static int hash(Object... values) {
		return Arrays.hashCode(values);
	}

	public static String toString(Object o) {
		return toString(o, "null");
	}

	public static String toString(Object o, String nullDefault) {
		return (o != null) ? o.toString() : nullDefault;
	}

	public static <T> int compare(T a, T b, Comparator<? super T> c) {
		return (a == b) ? 0 : c.compare(a, b);
	}

	public static <T> T requireNonNull(T obj) {
		if (obj == null) throw new NullPointerException();
		return obj;
	}

	public static <T> T requireNonNull(T obj, String message) {
		if (obj == null) throw new NullPointerException(message);
		return obj;
	}

	public static boolean isNull(Object obj) {
		return obj == null;
	}

	public static boolean nonNull(Object var0) {
		return var0 != null;
	}
}
