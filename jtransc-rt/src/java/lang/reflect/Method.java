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

import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscKeep;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.lang.annotation.Annotation;

@JTranscKeep
@HaxeAddMembers({
	"public var _parameterAnnotations = [];",
	"private function _getClass() { var clazz = this.{% FIELD java.lang.reflect.Method:clazz %}._hxClass; var SI = Reflect.field(clazz, 'SI'); if (SI != null) Reflect.callMethod(clazz, SI, []); return clazz; }",
	"private function _getObjectOrClass(obj:Dynamic):Dynamic { return (obj != null) ? obj : _getClass(); }",
})
public final class Method extends AccessibleObject implements Member, GenericDeclaration {
	@JTranscKeep
	private int id;

	@JTranscKeep
	private Class<?> clazz;

	@JTranscKeep
	private int slot;

	// This is guaranteed to be interned by the VM in the 1.4
	// reflection implementation
	@JTranscKeep
	private String name;

	@JTranscKeep
	private Class<?> returnType;

	@JTranscKeep
	private Class<?>[] parameterTypes;

	@JTranscKeep
	private Class<?>[] exceptionTypes;

	@JTranscKeep
	private int modifiers;

	// Generics and annotations support
	@JTranscKeep
	private transient String signature;

	@JTranscKeep
	private transient String genericSignature;

	// generic info repository; lazily initialized
	//private transient MethodRepository genericInfo;
	@JTranscKeep
	private byte[] annotations;

	@JTranscKeep
	private byte[] parameterAnnotations;

	@JTranscKeep
	private byte[] annotationDefault;
	//private volatile MethodAccessor methodAccessor;

	@HaxeMethodBody("return HaxeArrayAny.fromArray(_annotations, '[Ljava.lang.Annotation;');")
	native public Annotation[] getDeclaredAnnotations();

	@HaxeMethodBody("return HaxeArrayAny.fromArray2(_parameterAnnotations, '[[Ljava.lang.Annotation;');")
	native public Annotation[][] getParameterAnnotations();

	private Method() {

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

	native public TypeVariable<Method>[] getTypeParameters();

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

	public Class<?> getReturnType() {
		return (Class<?>) methodType().rettype;
	}

	public Type getGenericReturnType() {
		return genericMethodType().rettype;
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
		return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
	}

	public String toString() {
		int mod = getModifiers();
		String out = "";
		if (mod != 0) out += Modifier.toString(mod) + " ";
		out += _InternalUtils.getTypeName(getReturnType()) + " ";
		out += _InternalUtils.getTypeName(getDeclaringClass()) + "." + getName();
		out += "(";
		boolean first = true;
		for (Class<?> param : getParameterTypes()) {
			if (!first) out += ",";
			out += _InternalUtils.getTypeName(param);
			first = false;
		}
		out += ")";
		return out;
	}

	native public String toGenericString();

	@HaxeMethodBody("" +
		"var obj:Dynamic = _getObjectOrClass(p0);\n" +
		"return HaxeNatives.boxWithType(this.{% METHOD java.lang.reflect.Method:getReturnType %}(), Reflect.callMethod(obj, Reflect.field(obj, this._internalName), p1.toArray()));\n"
	)
	native public Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	public boolean isBridge() {
		return (getModifiers() & Modifier.BRIDGE) != 0;
	}

	public boolean isVarArgs() {
		return (getModifiers() & Modifier.VARARGS) != 0;
	}

	public boolean isSynthetic() {
		return (getModifiers() & Modifier.SYNTHETIC) != 0;
	}

	native public Object getDefaultValue();

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return super.getAnnotation(annotationClass);
	}
}
