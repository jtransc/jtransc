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
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.lang.annotation.Annotation;

@HaxeAddMembers({
	"private function _getClass() { var clazz = this.{% FIELD java.lang.reflect.Field:clazz %}._hxClass; var SI = Reflect.field(clazz, 'SI'); if (SI != null) Reflect.callMethod(clazz, SI, []); return clazz; }",
	"private function _getObjectOrClass(obj:Dynamic):Dynamic { return (obj != null) ? obj : _getClass(); }",
})
public final class Field extends AccessibleObject implements Member {
	protected int typeId;
	private Class<?> clazz;
	private String name;
	//private Class<?> type = null;
	protected int modifiers;
	private int slot;
	private transient String signature;
	private transient String genericSignature;
	private byte[] annotations;
	//private transient FieldRepository genericInfo;

	public Annotation[] getDeclaredAnnotations() {
		return super.getDeclaredAnnotations();
	}

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
		/*
		if (obj != null && obj instanceof Field) {
			Field other = (Field) obj;
			return (getDeclaringClass() == other.getDeclaringClass()) && (getName().equals(other.getName())) && (getType() == other.getType());
		}
		return false;
		*/
		return this == obj;
	}

	public int hashCode() {
		return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
	}

	public String toString() {
		int mod = getModifiers();
		return (((mod == 0) ? "" : (Modifier.toString(mod) + " ")) + _InternalUtils.getTypeName(getType()) + " " + _InternalUtils.getTypeName(getDeclaringClass()) + "." + getName());
	}

	public Object get(Object obj) throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = getType();
		if (type == null) {
			return null;
		} else if (type.isPrimitive()) {
			if (type == Void.TYPE) return null;
			if (type == Boolean.TYPE) return this.getBoolean(obj);
			if (type == Byte.TYPE) return this.getByte(obj);
			if (type == Short.TYPE) return this.getShort(obj);
			if (type == Character.TYPE) return this.getChar(obj);
			if (type == Integer.TYPE) return this.getInt(obj);
			if (type == Long.TYPE) return this.getLong(obj);
			if (type == Float.TYPE) return this.getFloat(obj);
			if (type == Double.TYPE) return this.getDouble(obj);
			return null;
		} else {
			return _getObject(obj);
		}
	}

	@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	@JTranscMethodBody(target = "cpp", value = "*(SOBJ *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	native private void _setObject(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	@JTranscMethodBody(target = "cpp", value = "return *(SOBJ *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	native public Object _getObject(Object obj) throws IllegalArgumentException, IllegalAccessException;

	////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////

	@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	@JTranscMethodBody(target = "cpp", value = "return *(bool *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	native public boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	@JTranscMethodBody(target = "cpp", value = "return *(int8_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	native public byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	@JTranscMethodBody(target = "cpp", value = "return *(int16_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	native public char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	@JTranscMethodBody(target = "cpp", value = "return *(int16_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	native public short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	@JTranscMethodBody(target = "cpp", value = "return *(int32_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	native public int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	@JTranscMethodBody(target = "cpp", value = "return *(int64_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	native public long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	@JTranscMethodBody(target = "cpp", value = "return *(float32_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	native public float getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	@JTranscMethodBody(target = "cpp", value = "return *(float64_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	native public double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException;


	//@HaxeMethodBody("Reflect.setField(p0, this._internalName, HaxeNatives.unbox(p1));")
	//native private void _setUnboxed(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("return HaxeNatives.str(this._internalName);")
	native String getInternalName();

	public void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = getType();
		if (type == null) {
		} else if (type.isPrimitive()) {
			if (type == Void.TYPE) {
			} else if (type == Boolean.TYPE) {
				this.setBoolean(obj, (Boolean) value);
			} else if (type == Byte.TYPE) {
				this.setByte(obj, (Byte) value);
			} else if (type == Short.TYPE) {
				this.setShort(obj, (Short) value);
			} else if (type == Character.TYPE) {
				this.setChar(obj, (Character) value);
			} else if (type == Integer.TYPE) {
				this.setInt(obj, (Integer) value);
			} else if (type == Long.TYPE) {
				this.setLong(obj, (Long) value);
			} else if (type == Float.TYPE) {
				this.setFloat(obj, (Float) value);
			} else if (type == Double.TYPE) {
				this.setDouble(obj, (Double) value);
			}
		} else {
			this._setObject(obj, value);
		}
	}

	@JTranscMethodBody(target = "cpp", value = {
		"auto field = GET_OBJECT(java_lang_reflect_Field, p0);",
		"auto obj = GET_OBJECT(java_lang_Object, p1);",
		"auto clazz = GET_OBJECT(java_lang_Class, field->{% FIELD java.lang.reflect.Field:clazz %});",
		"auto slot = field->{% FIELD java.lang.reflect.Field:slot %};",
		"auto clazzId = clazz->{% FIELD java.lang.Class:id %};",
		"auto out = TYPE_TABLE::TABLE[clazzId].dynamicFieldPtr(slot, p1);",
		//"printf(\"Field::getFieldPtr:%p\\n\", (void*)out);",
		"if (out == NULL) N::throwNpe();",
		"return (int64_t)out;",
	})
	native static private long getFieldPtr(Field field, Object obj);

	@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	@JTranscMethodBody(target = "cpp", value = "*(bool *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	native public void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	@JTranscMethodBody(target = "cpp", value = "*(int8_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	native public void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	@JTranscMethodBody(target = "cpp", value = "*(int16_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	native public void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	@JTranscMethodBody(target = "cpp", value = "*(int16_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	native public void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	@JTranscMethodBody(target = "cpp", value = "*(int32_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	native public void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	@JTranscMethodBody(target = "cpp", value = "*(int64_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	native public void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	@JTranscMethodBody(target = "cpp", value = "*(float32_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	native public void setFloat(Object obj, float f) throws IllegalArgumentException, IllegalAccessException;

	@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	@JTranscMethodBody(target = "cpp", value = "*(float64_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	native public void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException;

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return super.getAnnotation(annotationClass);
	}

}
