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

	//@HaxeMethodBody("return R.newInstance(this, p0);")
	//@JTranscMethodBody(target = "js", value = {
	//	"var constructor = this, args = p0;",
	//	"var argsArray = (args != null) ? args.data : [];",
	//	"if (constructor == null) throw 'Invalid R.newInstance : constructor == null';",
	//	"var parameters = constructor['{% METHOD java.lang.reflect.Constructor:getParameterTypes %}']().toArray();",
	//	"var argsUnboxed = argsArray.map(function(v, index) {",
	//	"	return N.unboxWithTypeWhenRequired(parameters[index], v);",
	//	"});",
	//	"var clazz = constructor['{% FIELD java.lang.reflect.Constructor:clazz %}']._jsClass;",
	//	"var obj = new clazz();",
	//	"obj[constructor._internalName].apply(obj, argsUnboxed);",
	//	"return obj;",
	//})
	//@JTranscMethodBody(target = "cpp", value = {
	//	"auto table = TYPE_TABLE::TABLE[this->{% FIELD java.lang.reflect.Constructor:typeId %}];",
	//	"std::vector<SOBJ> args = N::getVectorOrEmpty(p0);",
	//	"return table.dynamicNew ? table.dynamicNew(this->{% FIELD java.lang.reflect.Constructor:slot %}, args) : SOBJ(NULL);",
	//	//"return TYPE_TABLE::TABLE[this->{% FIELD java.lang.reflect.Constructor:typeId %}].constructors[this->{% FIELD java.lang.reflect.Constructor:slot %}].call(p0);",
	//})
	public T newInstance(Object... initargs) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (T) ProgramReflection.dynamicNew(this.slot, initargs);
	}
}
