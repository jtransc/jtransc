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

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscSync;

@SuppressWarnings({"UnnecessaryBoxing", "WeakerAccess", "unchecked", "SimplifiableIfStatement"})
public final class Boolean implements java.io.Serializable, Comparable<Boolean> {
	public static final Boolean TRUE = new Boolean(true);
	public static final Boolean FALSE = new Boolean(false);

	public static final Class<Boolean> TYPE = (Class<Boolean>) Class.getPrimitiveClass("boolean");

	private final boolean value;

	@JTranscSync
	public Boolean(boolean value) {
		this.value = value;
	}

	@JTranscSync
	public Boolean(String value) {
		this.value = parseBoolean(value);
	}

	@JTranscSync
	public static boolean parseBoolean(String value) {
		return (value != null) && (value.compareToIgnoreCase("true") == 0);
	}

	@JTranscSync
	public boolean booleanValue() {
		return value;
	}

	@JTranscSync
	public static Boolean valueOf(boolean value) {
		return value ? TRUE : FALSE;
	}

	@JTranscSync
	public static Boolean valueOf(String value) {
		return valueOf(parseBoolean(value));
	}

	@JTranscSync
	public static String toString(boolean value) {
		return value ? "true" : "false";
	}

	@JTranscSync
	public String toString() {
		return toString(value);
	}

	@Override
	@JTranscSync
	public int hashCode() {
		return hashCode(value);
	}

	@JTranscSync
	public static int hashCode(boolean value) {
		return value ? 1 : 0;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) return true;
		if (that == null) return false;
		if (getClass() != that.getClass()) return false;
		return value == ((Boolean) that).value;
	}

	public static boolean getBoolean(String name) {
		return parseBoolean(System.getProperty(name));
	}

	@JTranscSync
	public int compareTo(Boolean that) {
		return (that != null) ? compare(this.value, that.value) : compare(this.value, false);
	}

	@JTranscSync
	@JTranscMethodBody(target = "js", value = "return (p0 == p1) ? 0 : ((!p0) ? -1 : +1);")
	public static int compare(boolean l, boolean r) {
		return (l == r) ? 0 : ((!l) ? -1 : +1);
	}

	@JTranscSync
	public static boolean logicalAnd(boolean l, boolean r) {
		return l & r;
	}

	@JTranscSync
	public static boolean logicalOr(boolean l, boolean r) {
		return l | r;
	}

	@JTranscSync
	public static boolean logicalXor(boolean l, boolean r) {
		return l ^ r;
	}
}
