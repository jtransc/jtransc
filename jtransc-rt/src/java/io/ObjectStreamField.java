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

package java.io;

import java.lang.reflect.Field;
import java.util.Objects;

public class ObjectStreamField implements Comparable<Object> {
	private final String name;
	private final String signature;
	private final Class<?> type;
	private final boolean unshared;
	private final Field field;
	private int offset = 0;

	public ObjectStreamField(String name, Class<?> type) {
		this(name, type, false);
	}

	public ObjectStreamField(String name, Class<?> type, boolean unshared) {
		Objects.requireNonNull(name);
		this.name = name;
		this.type = type;
		this.unshared = unshared;
		signature = getClassSignature(type).intern();
		field = null;
	}

	ObjectStreamField(String name, String signature, boolean unshared) {
		Objects.requireNonNull(name);
		this.name = name;
		this.signature = signature.intern();
		this.unshared = unshared;
		field = null;

		switch (signature.charAt(0)) {
			case 'Z':
				type = Boolean.TYPE;
				break;
			case 'B':
				type = Byte.TYPE;
				break;
			case 'C':
				type = Character.TYPE;
				break;
			case 'S':
				type = Short.TYPE;
				break;
			case 'I':
				type = Integer.TYPE;
				break;
			case 'J':
				type = Long.TYPE;
				break;
			case 'F':
				type = Float.TYPE;
				break;
			case 'D':
				type = Double.TYPE;
				break;
			case 'L':
			case '[':
				type = Object.class;
				break;
			default:
				throw new IllegalArgumentException("illegal signature");
		}
	}

	ObjectStreamField(Field field, boolean unshared, boolean showType) {
		this.field = field;
		this.unshared = unshared;
		name = field.getName();
		Class<?> ftype = field.getType();
		type = (showType || ftype.isPrimitive()) ? ftype : Object.class;
		signature = getClassSignature(ftype).intern();
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public char getTypeCode() {
		return signature.charAt(0);
	}

	public String getTypeString() {
		return isPrimitive() ? null : signature;
	}

	public int getOffset() {
		return offset;
	}

	protected void setOffset(int offset) {
		this.offset = offset;
	}

	public boolean isPrimitive() {
		return ((signature.charAt(0) != 'L') && (signature.charAt(0) != '['));
	}

	public boolean isUnshared() {
		return unshared;
	}

	public int compareTo(Object obj) {
		ObjectStreamField other = (ObjectStreamField) obj;
		boolean isPrim = isPrimitive();
		if (isPrim != other.isPrimitive()) return isPrim ? -1 : 1;
		return name.compareTo(other.name);
	}

	public String toString() {
		return signature + ' ' + name;
	}

	Field getField() {
		return field;
	}

	String getSignature() {
		return signature;
	}

	private static String getClassSignature(Class<?> cl) {
		return cl.getName();
	}
}
