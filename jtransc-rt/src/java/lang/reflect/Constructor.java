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
import java.util.Arrays;

@SuppressWarnings({"unchecked", "unused"})
public final class Constructor<T> extends MethodConstructor implements Member, GenericDeclaration {
	@JTranscSync
	public Constructor(Class<?> containingClass, MemberInfo info) {
		super(containingClass, info);
	}

	@JTranscSync
	public Class<T> getDeclaringClass() {
		return (Class<T>) clazz;
	}

	@JTranscSync
	public String getName() {
		return getDeclaringClass().getName();
	}

	@Override
	@JTranscSync
	protected boolean isConstructor() {
		return true;
	}

	@JTranscSync
	native public TypeVariable<Constructor<T>>[] getTypeParameters();

	@JTranscSync
	public Class<?>[] getParameterTypes() {
		return (Class<?>[]) methodType().args;
	}

	@JTranscSync
	public Type[] getGenericParameterTypes() {
		return genericMethodType().args;
	}

	@JTranscSync
	public Class<?>[] getExceptionTypes() {
		return Arrays.copyOf(exceptionTypes, exceptionTypes.length);
	}

	@JTranscSync
	native public Type[] getGenericExceptionTypes();

	@JTranscSync
	native public boolean equals(Object obj);

	@JTranscSync
	public int hashCode() {
		return getDeclaringClass().getName().hashCode();
	}

	@JTranscSync
	native public String toGenericString();

	@JTranscSync
	public <TA extends Annotation> TA getAnnotation(Class<TA> annotationClass) {
		return super.getAnnotation(annotationClass);
	}

	@JTranscSync
	public Annotation[] getDeclaredAnnotations() {
		return super.getDeclaredAnnotations();
	}

	@JTranscSync
	public Annotation[][] getParameterAnnotations() {
		return super.getParameterAnnotations();
	}

	@JTranscAsync
	public T newInstance(Object... initargs) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (T) ProgramReflection.dynamicNew(this.clazz.id, this.slot, initargs);
	}
}
