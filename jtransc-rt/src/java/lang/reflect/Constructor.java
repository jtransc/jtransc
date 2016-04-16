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

import jtransc.annotation.JTranscInvisible;
import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

import java.lang.annotation.Annotation;

@HaxeAddMembers({
	"public var _parameterAnnotations = [];",
	"private function _getClass() { var clazz = this.clazz._hxClass; var SI = Reflect.field(clazz, 'SI'); if (SI != null) Reflect.callMethod(clazz, SI, []); return clazz; }",
	"private function _getObjectOrClass(obj:Dynamic) { return (obj != null) ? obj : _getClass(); }",
})
public final class Constructor<T> extends AccessibleObject implements Member, GenericDeclaration {
	private Class<T> clazz;
	private int slot;
	private Class<?>[] parameterTypes;
	private Class<?>[] exceptionTypes;
	private int modifiers;
	// Generics and annotations support
	private transient String signature;
	private transient String genericSignature;
	// generic info repository; lazily initialized
	private byte[] annotations;
	private byte[] parameterAnnotations;

	public Class<T> getDeclaringClass() {
		return clazz;
	}

	public String getName() {
		return getDeclaringClass().getName();
	}

	public int getModifiers() {
		return modifiers;
	}

	native public TypeVariable<Constructor<T>>[] getTypeParameters();

	@JTranscInvisible
	private MethodTypeImpl methodType;
	@JTranscInvisible
	private MethodTypeImpl genericMethodType;

	@JTranscInvisible
	private MethodTypeImpl methodType() {
		if (methodType == null) methodType = _InternalUtils.parseMethodType(signature, null);
		return methodType;
	}

	@JTranscInvisible
	private MethodTypeImpl genericMethodType() {
		if (genericMethodType == null) {
			if (genericSignature != null) {
				genericMethodType = _InternalUtils.parseMethodType(genericSignature, null);
			} else {
				genericMethodType = methodType();
			}
		}
		return genericMethodType;
	}

	public Class<?>[] getParameterTypes() {
		return (Class<?>[]) methodType().args;
	}

	public int getParameterCount() {
		return methodType().args.length;
	}

	public Type[] getGenericParameterTypes() {
		return genericMethodType().args;
	}

	public Class<?>[] getExceptionTypes() {
		return exceptionTypes.clone();
	}

	native public Type[] getGenericExceptionTypes();

	native public boolean equals(Object obj);

	public int hashCode() {
		return getDeclaringClass().getName().hashCode();
	}

	native public String toString();

	native public String toGenericString();

	@HaxeMethodBody(
		"//trace('dynamic newInstance : ' + this._internalName);\n" +
		"var instance = HaxeNatives.newInstance(this.clazz._internalName);\n" +
		"Reflect.callMethod(instance, Reflect.field(instance, this._internalName), p0.data.toArray());\n" +
		"return instance;"
	)
	native public T newInstance(Object... initargs) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	public boolean isVarArgs() {
		return (getModifiers() & Modifier.VARARGS) != 0;
	}

	public boolean isSynthetic() {
		return (getModifiers() & Modifier.SYNTHETIC) != 0;
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return super.getAnnotation(annotationClass);
	}

	public Annotation[] getDeclaredAnnotations() {
		return super.getDeclaredAnnotations();
	}

	@HaxeMethodBody("return HaxeArray.fromArray2(_parameterAnnotations, '[[Ljava.lang.Annotation;');")
	native public Annotation[][] getParameterAnnotations();
}
