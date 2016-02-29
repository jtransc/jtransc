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

import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

import java.lang.annotation.Annotation;

@HaxeAddMembers({
        "public var _internalName = '';",
        "public var _annotations = [];"
})
public final class Field extends AccessibleObject implements Member {
	private Class<?> clazz;
	private String name;
	//private Class<?> type = null;
	protected int modifiers;
	private int slot;
	private transient String signature;
	private transient String genericSignature;
	private byte[] annotations;
	//private transient FieldRepository genericInfo;

	@HaxeMethodBody("return HaxeArray.fromArray(_annotations, '[Ljava.lang.Annotation;');")
	native public Annotation[] getDeclaredAnnotations();

	private Field() {
	}

	public Class<?> getDeclaringClass() {
		return clazz;
	}

	public String getName() {
		return name;
	}

	public int getModifiers() {
		return modifiers;
	}

	public boolean isEnumConstant() {
		return (getModifiers() & Modifier.ENUM) != 0;
	}

	public boolean isSynthetic() {
		return (getModifiers() & Modifier.SYNTHETIC) != 0;
	}

	public Class<?> getType() {
		//return type;
		try {
			return Class.forName(signature);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Type genericType;

	public Type getGenericType() {
		if (genericType == null) {
			if (genericSignature != null) {
				genericType = _InternalUtils.parseType(genericSignature, null);
			} else {
				genericType = getType();
			}
		}
		return genericType;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Field) {
			Field other = (Field) obj;
			return (getDeclaringClass() == other.getDeclaringClass()) && (getName().equals(other.getName())) && (getType() == other.getType());
		}
		return false;
	}

	public int hashCode() {
		return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
	}

	public String toString() {
		int mod = getModifiers();
		return (((mod == 0) ? "" : (Modifier.toString(mod) + " ")) + _InternalUtils.getTypeName(getType()) + " " + _InternalUtils.getTypeName(getDeclaringClass()) + "." + getName());
	}

    @HaxeMethodBody("return HaxeNatives.box(Reflect.field(p0, this._internalName));")
	native public Object get(Object obj) throws IllegalArgumentException, IllegalAccessException;

	native public boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException;

	native public byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException;

	native public char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException;

	native public short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException;

	native public int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException;

	native public long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException;

	native public float getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException;

	native public double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException;

    @HaxeMethodBody("Reflect.setField(p0, this._internalName, p1);")
	native public void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException;

	native public void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException;

	native public void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException;

	native public void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException;

	native public void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException;

	native public void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException;

	native public void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException;

	native public void setFloat(Object obj, float f) throws IllegalArgumentException, IllegalAccessException;

	native public void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException;

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return super.getAnnotation(annotationClass);
	}

}
