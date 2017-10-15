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
import com.jtransc.annotation.JTranscSync;
import j.MemberInfo;
import j.ProgramReflection;

import java.lang.annotation.Annotation;

public final class Method extends MethodConstructor implements Member, GenericDeclaration {
	@JTranscSync
	public Method(Class<?> containingClass, MemberInfo info) {
		super(containingClass, info);
	}

	private Class<?> returnType;

	@Override
	@JTranscSync
	protected boolean isConstructor() {
		return false;
	}

	@JTranscSync
	public Annotation[] getDeclaredAnnotations() {
		return super.getDeclaredAnnotations();
	}

	@JTranscSync
	public Annotation[][] getParameterAnnotations() {
		return super.getParameterAnnotations();
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
	native public TypeVariable<Method>[] getTypeParameters();

	@JTranscSync
	public Class<?> getReturnType() {
		return (Class<?>) methodType().rettype;
	}

	@JTranscSync
	public Type getGenericReturnType() {
		return genericMethodType().rettype;
	}

	@JTranscSync
	public Class<?>[] getParameterTypes() {
		return (Class<?>[]) methodType().args;
	}

	@JTranscSync
	public int getParameterCount() {
		return methodType().args.length;
	}

	@JTranscSync
	public Type[] getGenericParameterTypes() {
		return genericMethodType().args;
	}

	@JTranscSync
	native public Type[] getGenericExceptionTypes();

	@JTranscSync
	native public boolean equals(Object obj);

	@JTranscSync
	public int hashCode() {
		return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
	}

	@JTranscSync
	native public String toGenericString();

	@JTranscAsync
	public Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return ProgramReflection.dynamicInvoke(this.clazz.id, id, obj, args);
	}

	@JTranscSync
	public boolean isBridge() {
		return (getModifiers() & Modifier.BRIDGE) != 0;
	}

	@JTranscSync
	public Object getDefaultValue() {
		// @TODO:
		return null;
	}

	@JTranscSync
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return super.getAnnotation(annotationClass);
	}
}
