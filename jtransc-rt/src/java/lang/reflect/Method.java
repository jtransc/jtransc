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

public final class Method extends MethodConstructor implements Member, GenericDeclaration {
	public Method(Class<?> containingClass, MemberInfo info) {
		super(containingClass, info);
	}

	private Class<?> returnType;

	@Override
	protected boolean isConstructor() {
		return false;
	}

	public Annotation[] getDeclaredAnnotations() {
		return super.getDeclaredAnnotations();
	}

	public Annotation[][] getParameterAnnotations() {
		return super.getParameterAnnotations();
	}


	public String getName() {
		return name;
	}

	public int getModifiers() {
		return modifiers;
	}

	native public TypeVariable<Method>[] getTypeParameters();

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

	native public Type[] getGenericExceptionTypes();

	native public boolean equals(Object obj);

	public int hashCode() {
		return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
	}

	native public String toGenericString();

	public Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return ProgramReflection.dynamicInvoke(id, obj, args);
	}

	public boolean isBridge() {
		return (getModifiers() & Modifier.BRIDGE) != 0;
	}

	native public Object getDefaultValue();

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return super.getAnnotation(annotationClass);
	}
}
