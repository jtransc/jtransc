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

import com.jtransc.annotation.JTranscAsync;
import com.jtransc.annotation.JTranscKeep;
import com.jtransc.annotation.JTranscSync;
import com.jtransc.ds.FastIntMap;
import com.jtransc.io.JTranscConsole;
import j.MemberInfo;
import j.ProgramReflection;

import java.lang.annotation.Annotation;

@SuppressWarnings({"ConstantConditions", "unused", "StatementWithEmptyBody"})
public final class Field extends AccessibleObject implements Member {
	protected int typeId;
	public Class<?> clazz;
	public String name;
	//private Class<?> type = null;
	protected int modifiers;
	public int slot;

	public transient String signature;
	public transient String genericSignature;
	public byte[] annotations;
	//private transient FieldRepository genericInfo;

	private static final FastIntMap<FastIntMap<Annotation[]>> _annotationsCache = new FastIntMap<FastIntMap<Annotation[]>>();

	@JTranscSync
	public Annotation[] getDeclaredAnnotations() {
		Annotation[] cache;
		FastIntMap<Annotation[]> map = _annotationsCache.get(clazz.id);
		if (map != null) {
			cache = map.get(info.id);
			if (cache != null) {
				return cache;
			}
		}
		if (map == null) {
			map = new FastIntMap<Annotation[]>();
			_annotationsCache.set(clazz.id, map);
		}
		cache = ProgramReflection.getFieldAnnotations(clazz.id, info.id);
		if (cache == null) {
			cache = new Annotation[0];
		}
		map.set(info.id, cache);
		return cache;
	}

	@JTranscSync
	public Field(Class<?> containingClass, MemberInfo info) {
		super(info);
		this.clazz = containingClass;
		this.slot = info.id;
		this.name = info.name;
		this.signature = info.desc;
		this.genericSignature = info.genericDesc;
		this.modifiers = info.modifiers;
	}

	@JTranscSync
	public Field() {
	}

	@JTranscSync
	public Class<?> getDeclaringClass() {
		return clazz;
	}

	@JTranscSync
	public String getName() {
		return name;
	}

	@JTranscSync
	public int getModifiers() {
		return modifiers;
	}

	@JTranscSync
	public boolean isEnumConstant() {
		return (getModifiers() & Modifier.ENUM) != 0;
	}

	@JTranscSync
	public boolean isSynthetic() {
		return (getModifiers() & Modifier.SYNTHETIC) != 0;
	}

	@JTranscSync
	boolean isStatic() {
		return (getModifiers() & Modifier.STATIC) != 0;
	}

	@JTranscSync
	public Class<?> getType() {
		//return type;
		try {
			return Class.forName(signature);
		} catch (ClassNotFoundException e) {
			JTranscConsole.syncPrintStackTrace(e);
		}
		return null;
	}

	private Type genericType;

	@JTranscSync
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

	@JTranscSync
	public boolean equals(Object obj) {
		/*
		if (obj != null && obj instanceof Field) {
			Field other = (Field) obj;
			return (getDeclaringClass() == other.getDeclaringClass()) && (getName().equals(other.getName())) && (getType() == other.getType());
		}
		return false;
		*/
		return this == obj;
	}

	@JTranscSync
	public int hashCode() {
		return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
	}

	//@JTranscSync
	@JTranscAsync
	public String toString() {
		int mod = getModifiers();
		return (((mod == 0) ? "" : (Modifier.toString(mod) + " ")) + _InternalUtils.getTypeName(getType()) + " " + _InternalUtils.getTypeName(getDeclaringClass()) + "." + getName());
	}

	@JTranscSync
	public Object get(Object obj) throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = getType();
		if (type == null) {
			return null;
			//} else if (type.isPrimitive()) {
			//	if (type == Void.TYPE) return null;
			//	if (type == Boolean.TYPE) return this.getBoolean(obj);
			//	if (type == Byte.TYPE) return this.getByte(obj);
			//	if (type == Short.TYPE) return this.getShort(obj);
			//	if (type == Character.TYPE) return this.getChar(obj);
			//	if (type == Integer.TYPE) return this.getInt(obj);
			//	if (type == Long.TYPE) return this.getLong(obj);
			//	if (type == Float.TYPE) return this.getFloat(obj);
			//	if (type == Double.TYPE) return this.getDouble(obj);
			//	return null;
		} else {
			return _getObject(obj);
		}
	}

	@JTranscSync
	private void _setObject(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException {
		ProgramReflection.dynamicSet(this.clazz.id, slot, obj, value);
	}

	@JTranscSync
	public Object _getObject(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return ProgramReflection.dynamicGet(this.clazz.id, slot, obj);
	}

	////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////

	@JTranscSync
	public boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Boolean) get(obj);
	}

	@JTranscSync
	public byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Byte) get(obj);
	}

	@JTranscSync
	public char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Character) get(obj);
	}

	@JTranscSync
	public short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Short) get(obj);
	}

	@JTranscSync
	public int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Integer) get(obj);
	}

	@JTranscSync
	public long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Long) get(obj);
	}

	@JTranscSync
	public float getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Float) get(obj);
	}

	@JTranscSync
	public double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Double) get(obj);
	}


	//@HaxeMethodBody("Reflect.setField(p0, this._internalName, N.unbox(p1));")
	//native private void _setUnboxed(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException;

	//@HaxeMethodBody("return N.str(this._internalName);")
	@JTranscSync
	native String getInternalName();

	@JTranscSync
	public void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = getType();
		if (type == null) {
			//} else if (type.isPrimitive()) {
			//	if (type == Void.TYPE) {
			//	} else if (type == Boolean.TYPE) {
			//		this.setBoolean(obj, (Boolean) value);
			//	} else if (type == Byte.TYPE) {
			//		this.setByte(obj, (Byte) value);
			//	} else if (type == Short.TYPE) {
			//		this.setShort(obj, (Short) value);
			//	} else if (type == Character.TYPE) {
			//		this.setChar(obj, (Character) value);
			//	} else if (type == Integer.TYPE) {
			//		this.setInt(obj, (Integer) value);
			//	} else if (type == Long.TYPE) {
			//		this.setLong(obj, (Long) value);
			//	} else if (type == Float.TYPE) {
			//		this.setFloat(obj, (Float) value);
			//	} else if (type == Double.TYPE) {
			//		this.setDouble(obj, (Double) value);
			//	}
		} else {
			this._setObject(obj, value);
		}
	}

	@JTranscSync
	public void setBoolean(Object obj, boolean v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	@JTranscSync
	public void setByte(Object obj, byte v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	@JTranscSync
	public void setChar(Object obj, char v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	@JTranscSync
	public void setShort(Object obj, short v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	@JTranscSync
	public void setInt(Object obj, int v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	@JTranscSync
	public void setLong(Object obj, long v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	@JTranscSync
	public void setFloat(Object obj, float v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	@JTranscSync
	public void setDouble(Object obj, double v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	@JTranscSync
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return super.getAnnotation(annotationClass);
	}
}