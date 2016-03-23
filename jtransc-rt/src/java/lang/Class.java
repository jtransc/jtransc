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

package java.lang;

import jtransc.FastStringMap;
import jtransc.annotation.JTranscKeep;
import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.lang.AnnotatedElement;

@HaxeAddMembers({
	"public var _hxClass:Class<Dynamic> = null;",
	"public var _hxProxyClass:Class<Dynamic> = null;",
	"public var _internalName = '';",
	"public var _parent:String = null;",
	"public var _interfaces:Array<String> = [];",
	"public var _fields = [];",
	"public var _modifiers = 0;",
	"public var _methods:Array<java_.lang.reflect.Method_> = [];",
	"public var _constructors = [];",
	"public var _annotations = [];",
	"public var _methodsById = null;",
	"" +
		"public function populateMethodsById() {\n" +
		"  if (_methodsById != null) return;\n" +
		"  _methodsById = new Map<Int, java_.lang.reflect.Method_>();\n" +
		"  function populate(clazz:java_.lang.Class_) {\n" +
		"    for (m in clazz._methods) _methodsById.set(m.id, m);\n" +
		"    if (clazz._parent != null) populate(HaxeNatives.resolveClass(clazz._parent));\n" +
		"    for (i in clazz._interfaces) populate(HaxeNatives.resolveClass(i));\n" +
		"  }\n" +
		"  populate(this);\n" +
		"}\n",
	"public function locateMethodById(id:Int) { populateMethodsById(); return _methodsById.get(id); }",
})
public final class Class<T> implements java.io.Serializable, Type, GenericDeclaration, AnnotatedElement {
	private static final int ANNOTATION = 0x00002000;
	private static final int ENUM = 0x00004000;
	private static final int SYNTHETIC = 0x00001000;

	// Returns the Class representing the superclass of the entity (class, interface, primitive type or void) represented by this Class. If this Class represents either the Object class, an interface, a primitive type, or void, then null is returned. If this object represents an array class then the Class object representing the Object class is returned.

	// Returns an array of Field objects reflecting all the fields declared by the class or interface represented by this Class object. This includes public, protected, default (package) access, and private fields, but excludes inherited fields. The elements in the array returned are not sorted and are not in any particular order. This method returns an array of length 0 if the class or interface declares no fields, or if this Class object represents a primitive type, an array class, or void.
	// Returns an array of Field objects reflecting all the fields declared by the class or interface represented by this Class object. This includes public, protected, default (package) access, and private fields, but excludes inherited fields. The elements in the array returned are not sorted and are not in any particular order. This method returns an array of length 0 if the class or interface declares no fields, or if this Class object represents a primitive type, an array class, or void.
	@HaxeMethodBody("return HaxeArray.fromArray(_fields, '[Ljava.lang.reflect.Field;');")
	native public Field[] getDeclaredFields() throws SecurityException;

	@HaxeMethodBody("return HaxeArray.fromArray(_methods, '[Ljava.lang.reflect.Method;');")
	native public Method[] getDeclaredMethods() throws SecurityException;

	@HaxeMethodBody("return HaxeArray.fromArray(_constructors, '[Ljava.lang.reflect.Constructor;');")
	native public Constructor<?>[] getDeclaredConstructors() throws SecurityException;

	@HaxeMethodBody("return (_parent != null) ? HaxeNatives.resolveClass(_parent) : null;")
	native public Class<? super T> getSuperclass();

	@HaxeMethodBody("return HaxeArray.fromArray(Lambda.array(Lambda.map(_interfaces, function(i) { return HaxeNatives.resolveClass(i); })), '[Ljava.lang.Class;');")
	native public Class<?>[] getInterfaces();

	@HaxeMethodBody("return HaxeArray.fromArray(_annotations, '[Ljava.lang.Annotation;');")
	native public Annotation[] getDeclaredAnnotations();

	@HaxeMethodBody("return _modifiers;")
	native public int getModifiers();

	@HaxeMethodBody("return HaxeNatives.newInstance(this._internalName);")
	native public T newInstance() throws InstantiationException, IllegalAccessException;

	native public Class<?>[] getDeclaredClasses() throws SecurityException;

	native public Method getEnclosingMethod();

	native public Constructor<?> getEnclosingConstructor() throws SecurityException;

	native public java.net.URL getResource(String name);

	@HaxeMethodBody("return Std.is(p0, _hxClass);")
	public native boolean isInstance(Object obj);

	native public InputStream getResourceAsStream(String name);

	public ClassLoader getClassLoader() {
		return _ClassInternalUtils.getSystemClassLoader();
	}

	native public TypeVariable<Class<T>>[] getTypeParameters();

	native public Type getGenericSuperclass();

	native public Package getPackage();

	native public Type[] getGenericInterfaces();

	public native Class<?> getComponentType();

	public native Object[] getSigners();

	native public Class<?> getDeclaringClass() throws SecurityException;

	native public Class<?> getEnclosingClass() throws SecurityException;

	native public Class<?>[] getClasses();

	//native public java.security.ProtectionDomain getProtectionDomain();


	@JTranscKeep
	private Class() {
	}

	@JTranscKeep
	private String name;
	@JTranscKeep
	private boolean primitive = false;

	@JTranscKeep
	Class(String name) throws ClassNotFoundException {
		this.name = name;
		this.primitive = false;
		if (!_check()) throw new ClassNotFoundException("Can't find class " + name);
	}

	@JTranscKeep
	Class(String name, boolean primitive) {
		this.name = name;
		this.primitive = primitive;
	}

	@JTranscKeep
	@HaxeMethodBody("return HaxeReflectionInfo.__initClass(this);")
	native private boolean _check();

	@JTranscKeep
	public String getName() {
		return this.name;
	}

	@JTranscKeep
	static Class<?> getPrimitiveClass(String name) {
		return new Class<Object>(name, true);
	}

	public String toString() {
		return (isInterface() ? "interface " : (isPrimitive() ? "" : "class ")) + getName();
	}

	native public String toGenericString();

	private static FastStringMap<Class<?>> _classCache = new FastStringMap<Class<?>>();

	static Class<?> forName0(String className) {
		try {
			return forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	@JTranscKeep
	public static Class<?> forName(String className) throws ClassNotFoundException {
		if (className.length() == 1) {
			switch (className.charAt(0)) {
				case 'V':
					return Void.TYPE;
				case 'Z':
					return Boolean.TYPE;
				case 'B':
					return Byte.TYPE;
				case 'C':
					return Character.TYPE;
				case 'S':
					return Short.TYPE;
				case 'D':
					return Double.TYPE;
				case 'F':
					return Float.TYPE;
				case 'I':
					return Integer.TYPE;
				case 'J':
					return Long.TYPE;
			}
		}
		if (className.startsWith("L") && className.endsWith(";")) {
			return forName(className.substring(1, className.length() - 1).replace('/', '.'));
		}
		if (!_classCache.has(className)) {
			_classCache.set(className, new Class<Object>(className));
		}
		return _classCache.get(className);
	}

	public static Class<?> forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
		return forName(name);
	}

	private HashSet<Class<?>> allRelatedClasses = null;

	private HashSet<Class<?>> getAllRelatedClasses() {
		if (allRelatedClasses == null) {
			allRelatedClasses = new HashSet<>();
			allRelatedClasses.add(this);
			for (Class<?> i : this.getInterfaces()) {
				allRelatedClasses.addAll(i.getAllRelatedClasses());
			}
			Class<? super T> superclass = this.getSuperclass();
			if (superclass != null) {
				allRelatedClasses.addAll(superclass.getAllRelatedClasses());
			}
		}
		return allRelatedClasses;
	}

	public boolean isAssignableFrom(Class<?> cls) {
		return cls.getAllRelatedClasses().contains(this);
	}

	public boolean isInterface() {
		return Modifier.isInterface(getModifiers());
	}

	public boolean isArray() {
		return this.name.startsWith("[");
	}

	public boolean isPrimitive() {
		return primitive;
	}

	public boolean isAnnotation() {
		return (this.getModifiers() & ANNOTATION) != 0;
	}

	public boolean isSynthetic() {
		return (this.getModifiers() & SYNTHETIC) != 0;
	}

	public boolean isEnum() {
		return (this.getModifiers() & ENUM) != 0;
	}

	public String getSimpleName() {
		String out = "";
		char separator = (this.name.indexOf('$') > 0) ? '$' : '.';
		out += this.name.substring(this.name.lastIndexOf(separator) + 1);
		if (isArray()) out += "[]";
		return out;
	}

	native public String getTypeName();

	//public String getCanonicalName() {
	//return this.name.replace('.', '/');
	//}
	public String getCanonicalName() {
		return this.name.replace('$', '.');
	}

	public boolean isAnonymousClass() {
		return "".equals(getSimpleName());
	}

	public boolean isLocalClass() {
		return isLocalOrAnonymousClass() && !isAnonymousClass();
	}

	private boolean isLocalOrAnonymousClass() {
		return getEnclosingMethodInfo() != null;
	}

	public boolean isMemberClass() {
		return getSimpleBinaryName() != null && !isLocalOrAnonymousClass();
	}

	native private Object getEnclosingMethodInfo();
	//private EnclosingMethodInfo getEnclosingMethodInfo() {
	//	Object[] enclosingInfo = getEnclosingMethod0();
	//	if (enclosingInfo == null)
	//		return null;
	//	else {
	//		return new EnclosingMethodInfo(enclosingInfo);
	//	}
	//}

	private String getSimpleBinaryName() {
		Class<?> enclosingClass = getEnclosingClass();
		if (enclosingClass == null) // top level class
			return null;
		// Otherwise, strip the enclosing class' name
		try {
			return getName().substring(enclosingClass.getName().length());
		} catch (IndexOutOfBoundsException ex) {
			throw new InternalError("Malformed class name", ex);
		}
	}

	public boolean desiredAssertionStatus() {
		return false;
	}

	public T[] getEnumConstants() {
		T[] values = getEnumConstantsShared();
		if (values == null) {
			System.out.println("Class " + this + " is not an enum (" + isEnum() + ")!");

			try {
				final Method valuesMethod = getMethod("values");
				System.out.println("values method:" + valuesMethod);
			} catch (NoSuchMethodException e) {
				throw new Error(e);
			}
		}
		return (values != null) ? values.clone() : null;
	}

	T[] getEnumConstantsShared() {
		if (enumConstants == null) {
			if (!isEnum()) return null;
			try {
				final Method valuesMethod = getMethod("values");
				T[] temporaryConstants = (T[]) valuesMethod.invoke(null);
				enumConstants = temporaryConstants;
			} catch (Exception ex) {
				return null;
			}
		}
		return enumConstants;
	}

	private volatile transient T[] enumConstants = null;

	public T cast(Object obj) {
		if (obj != null && !isInstance(obj)) throw new ClassCastException(cannotCastMsg(obj));
		return (T) obj;
	}

	private String cannotCastMsg(Object obj) {
		return "Cannot cast " + obj.getClass().getName() + " to " + getName();
	}

	public <U> Class<? extends U> asSubclass(Class<U> clazz) {
		if (!clazz.isAssignableFrom(this)) throw new ClassCastException(this.toString());
		return (Class<? extends U>) this;
	}

	private FastStringMap<Field> _fieldsByName;
	private FastStringMap<Field> _declaredFieldsByName;

	// Returns an array containing Field objects reflecting all the accessible public fields of the class or interface represented by this Class object. The elements in the array returned are not sorted and are not in any particular order. This method returns an array of length 0 if the class or interface has no accessible public fields, or if it represents an array class, a primitive type, or void.
	public Field[] getFields() throws SecurityException {
		return getDeclaredFields(); // @TODO: Filter just public!
	}

	public Field getField(String name) throws NoSuchFieldException, SecurityException {
		if (_fieldsByName == null) {
			_fieldsByName = new FastStringMap<Field>();
			for (Field f : this.getFields()) _fieldsByName.set(f.getName(), f);
		}
		return _fieldsByName.get(name);
	}

	public Field getDeclaredField(String name) throws NoSuchFieldException, SecurityException {
		if (_declaredFieldsByName == null) {
			_declaredFieldsByName = new FastStringMap<Field>();
			for (Field f : this.getDeclaredFields()) _declaredFieldsByName.set(f.getName(), f);
		}
		return _declaredFieldsByName.get(name);
	}

	public Method[] getMethods() throws SecurityException {
		return this.getDeclaredMethods(); // @TODO: Filter just public!
	}

	public Annotation[] getAnnotations() {
		return this.getDeclaredAnnotations(); // @TODO: Filter just public!
	}

	public Constructor<?>[] getConstructors() throws SecurityException {
		return this.getDeclaredConstructors(); // @TODO: Filter just public!
	}

	public Method getMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
		return getDeclaredMethod(name, parameterTypes);
	}

	public Constructor<T> getConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
		return getDeclaredConstructor(parameterTypes);
	}

	public Method getDeclaredMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
		for (Method m : getDeclaredMethods()) {
			if (Objects.equals(m.getName(), name) && Arrays.equals(m.getParameterTypes(), parameterTypes)) {
				return m;
			}
		}
		throw new NoSuchMethodException(name);
	}

	public Constructor<T> getDeclaredConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
		for (Constructor c : getDeclaredConstructors()) {
			if (Arrays.equals(c.getParameterTypes(), parameterTypes)) {
				return c;
			}
		}
		throw new NoSuchMethodException();
	}

	@JTranscKeep
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return getDeclaredAnnotation(annotationClass) != null;
	}

	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		for (Annotation a : getAnnotations()) {
			if (a.getClass() == annotationClass) return (A) a;
		}
		return null;
	}

	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
		ArrayList<A> out = new ArrayList<A>();
		for (Annotation a : getAnnotations()) {
			if (a.getClass() == annotationClass) out.add((A) a);
		}
		return (A[]) out.toArray(new Annotation[0]);
	}

	public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
		for (Annotation a : getDeclaredAnnotations()) {
			if (a.getClass() == annotationClass) return (A) a;
		}
		return null;
	}

	public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
		ArrayList<A> out = new ArrayList<A>();
		for (Annotation a : getDeclaredAnnotations()) {
			if (a.getClass() == annotationClass) out.add((A) a);
		}
		return (A[]) out.toArray(new Annotation[0]);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Class<?> aClass = (Class<?>) o;

		if (isPrimitive() != aClass.isPrimitive()) return false;
		return name != null ? name.equals(aClass.name) : aClass.name == null;

	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}

