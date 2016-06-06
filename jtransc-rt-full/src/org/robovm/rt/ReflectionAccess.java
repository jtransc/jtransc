package org.robovm.rt;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface ReflectionAccess {
	Field clone(Field f);
	Constructor<?> clone(Constructor<?> c);
	Method clone(Method m);
	Field[] clone(Field[] f);
	Constructor<?>[] clone(Constructor<?>[] c);
	Method[] clone(Method[] m);
	boolean equals(Method m1, Method m2);
	boolean matchParameterTypes(Constructor<?> c, Class<?>[] parameterTypes);
	boolean matchParameterTypes(Method m, Class<?>[] parameterTypes);
}
