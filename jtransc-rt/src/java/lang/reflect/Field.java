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

import j.MemberInfo;
import j.ProgramReflection;

import java.lang.annotation.Annotation;
import java.lang.jtransc.JTranscCoreReflection;

@SuppressWarnings({"ConstantConditions", "unused", "StatementWithEmptyBody"})
//@HaxeAddMembers({
//	"private function _getClass() { var clazz = this.{% FIELD java.lang.reflect.Field:clazz %}._hxClass; var SI = Reflect.field(clazz, 'SI'); if (SI != null) Reflect.callMethod(clazz, SI, []); return clazz; }",
//	"private function _getObjectOrClass(obj:Dynamic):Dynamic { return (obj != null) ? obj : _getClass(); }",
//})
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

	public Annotation[] getDeclaredAnnotations() {
		Annotation[]out = ProgramReflection.getFieldAnnotations(clazz.id, info.id);
		return (out != null) ? out : new Annotation[0];
	}

	public Field(Class<?> containingClass, MemberInfo info) {
		super(info);
		this.clazz = containingClass;
		this.slot = info.id;
		this.name = info.name;
		this.signature = info.desc;
		this.genericSignature = info.genericDesc;
		this.modifiers = info.modifiers;
	}

	public Field() {
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

	//@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	//@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	//@JTranscMethodBody(target = "cpp", value = "*(SOBJ *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	private void _setObject(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException {
		ProgramReflection.dynamicSet(this.clazz.id, slot, obj, value);
	}

	//@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	//@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	//@JTranscMethodBody(target = "cpp", value = "return *(SOBJ *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	public Object _getObject(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return ProgramReflection.dynamicGet(this.clazz.id, slot, obj);
	}

	////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////

	//@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	//@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	//@JTranscMethodBody(target = "cpp", value = "return *(bool *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	public boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Boolean) get(obj);
	}

	//@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	//@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	//@JTranscMethodBody(target = "cpp", value = "return *(int8_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	public byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Byte) get(obj);
	}

	//@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	//@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	//@JTranscMethodBody(target = "cpp", value = "return *(int16_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	public char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Character) get(obj);
	}

	//@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	//@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	//@JTranscMethodBody(target = "cpp", value = "return *(int16_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	public short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Short) get(obj);
	}

	//@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	//@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	//@JTranscMethodBody(target = "cpp", value = "return *(int32_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	public int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Integer) get(obj);
	}

	//@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	//@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	//@JTranscMethodBody(target = "cpp", value = "return *(int64_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	public long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Long) get(obj);
	}

	//@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	//@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	//@JTranscMethodBody(target = "cpp", value = "return *(float32_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	public float getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Float) get(obj);
	}

	//@HaxeMethodBody("return Reflect.field(_getObjectOrClass(p0), this._internalName);")
	//@JTranscMethodBody(target = "js", value = "return R.getField(this, p0);")
	//@JTranscMethodBody(target = "cpp", value = "return *(float64_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0));")
	public double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return (Double) get(obj);
	}


	//@HaxeMethodBody("Reflect.setField(p0, this._internalName, HaxeNatives.unbox(p1));")
	//native private void _setUnboxed(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException;

	//@HaxeMethodBody("return HaxeNatives.str(this._internalName);")
	native String getInternalName();

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

	//@JTranscMethodBody(target = "cpp", value = {
	//	"auto field = GET_OBJECT(java_lang_reflect_Field, p0);",
	//	"auto obj = GET_OBJECT(java_lang_Object, p1);",
	//	"auto clazz = GET_OBJECT(java_lang_Class, field->{% FIELD java.lang.reflect.Field:clazz %});",
	//	"auto slot = field->{% FIELD java.lang.reflect.Field:slot %};",
	//	"auto clazzId = clazz->{% FIELD java.lang.Class:id %};",
	//	"auto out = TYPE_TABLE::TABLE[clazzId].dynamicFieldPtr(slot, p1);",
	//	//"printf(\"Field::getFieldPtr:%p\\n\", (void*)out);",
	//	"if (out == NULL) N::throwNpe();",
	//	"return (int64_t)out;",
	//})
	//static private long getFieldPtr(Field field, Object obj) {
	//	return ProgramReflection.dynamicFieldPtr(field.slot, obj);
	//}

	//@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	//@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	//@JTranscMethodBody(target = "cpp", value = "*(bool *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	public void setBoolean(Object obj, boolean v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	//@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	//@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	//@JTranscMethodBody(target = "cpp", value = "*(int8_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	public void setByte(Object obj, byte v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	//@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	//@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	//@JTranscMethodBody(target = "cpp", value = "*(int16_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	public void setChar(Object obj, char v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	//@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	//@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	//@JTranscMethodBody(target = "cpp", value = "*(int16_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	public void setShort(Object obj, short v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	//@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	//@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	//@JTranscMethodBody(target = "cpp", value = "*(int32_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	public void setInt(Object obj, int v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	//@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	//@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	//@JTranscMethodBody(target = "cpp", value = "*(int64_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	public void setLong(Object obj, long v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	//@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	//@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	//@JTranscMethodBody(target = "cpp", value = "*(float32_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	public void setFloat(Object obj, float v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	//@HaxeMethodBody("Reflect.setField(_getObjectOrClass(p0), this._internalName, p1);")
	//@JTranscMethodBody(target = "js", value = "R.setField(this, p0, p1);")
	//@JTranscMethodBody(target = "cpp", value = "*(float64_t *)(this->{% METHOD java.lang.reflect.Field:getFieldPtr %}(this->sptr(), p0)) = p1;")
	public void setDouble(Object obj, double v) throws IllegalArgumentException, IllegalAccessException {
		set(obj, v);
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return super.getAnnotation(annotationClass);
	}

}
