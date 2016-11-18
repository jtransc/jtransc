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

@SuppressWarnings({"unchecked", "unused"})
public final class Constructor<T> extends MethodConstructor implements Member, GenericDeclaration {
	public Constructor(Class<?> containingClass, MemberInfo info) {
		super(containingClass, info);
	}

	public Class<T> getDeclaringClass() {
		return (Class<T>) clazz;
	}

	public String getName() {
		return getDeclaringClass().getName();
	}

	@Override
	protected boolean isConstructor() {
		return true;
	}

	native public TypeVariable<Constructor<T>>[] getTypeParameters();

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

	native public String toGenericString();

	public <TA extends Annotation> TA getAnnotation(Class<TA> annotationClass) {
		return super.getAnnotation(annotationClass);
	}

	public Annotation[] getDeclaredAnnotations() {
		return super.getDeclaredAnnotations();
	}

	public Annotation[][] getParameterAnnotations() {
		return super.getParameterAnnotations();
	}

	public T newInstance(Object... initargs) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (T) ProgramReflection.dynamicNew(this.clazz.id, this.slot, initargs);
	}
}
