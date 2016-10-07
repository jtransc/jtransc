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
import com.jtransc.annotation.haxe.HaxeMethodBody;
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

	@HaxeMethodBody("return R.invokeMethod(this, p0, p1);")
	@JTranscMethodBody(target = "js", value = {
		"var method = this, obj = p0, args = p1;",
		"var methodClass = method['{% FIELD java.lang.reflect.MethodConstructor:clazz %}'];",
		"if (!methodClass) { console.log(method); throw 'Invalid reflect.Method'; }",
		"var methodClassName = methodClass['{% FIELD java.lang.Class:name %}'];",
		"if (!methodClassName) { console.log(method); throw 'Invalid reflect.Method'; }",
		"var obj2 = (obj == null) ? jtranscClasses[N.istr(methodClassName)] : obj;",
		"var parameters = method['{% METHOD java.lang.reflect.Method:getParameterTypes %}']().toArray();",
		"var argsUnboxed = args.data.map(function(v, index) {",
		"	return N.unboxWithTypeWhenRequired(parameters[index], v);",
		"});",
		"var result = obj2[method._internalName].apply(obj2, argsUnboxed);",
		"return N.boxWithType(method['{% METHOD java.lang.reflect.Method:getReturnType %}'](), result);",
		"return R.invokeMethod(this, p0, p1);",
	})
	//@JTranscMethodBody(target = "cpp", value = {
	//	//"printf(\"Method.invoke[1]\\n\");",
	//	"auto table = TYPE_TABLE::TABLE[this->{% FIELD java.lang.reflect.Constructor:typeId %}];",
	//	//"printf(\"Method.invoke[2]\\n\");",
	//	"auto obj = p0;",
	//	"std::vector<SOBJ> args = N::getVectorOrEmpty(p1);",
	//	//"printf(\"Method.invoke[3]\\n\");",
	//	"return table.dynamicInvoke ? table.dynamicInvoke(this->{% FIELD java.lang.reflect.Constructor:slot %}, obj, args) : SOBJ(NULL);"
	//})
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
