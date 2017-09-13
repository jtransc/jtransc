package java.lang.invoke;

import java.util.List;

public final class MethodType implements java.io.Serializable {
	native public static MethodType methodType(Class<?> rtype, Class<?>[] ptypes);

	native public static MethodType methodType(Class<?> rtype, List<Class<?>> ptypes);

	native public static MethodType methodType(Class<?> rtype, Class<?> ptype0, Class<?>... ptypes);

	native public static MethodType methodType(Class<?> rtype);

	native public static MethodType methodType(Class<?> rtype, Class<?> ptype0);

	native public static MethodType methodType(Class<?> rtype, MethodType ptypes);

	native public static MethodType genericMethodType(int objectArgCount, boolean finalArray);

	native public static MethodType genericMethodType(int objectArgCount);

	native public MethodType changeParameterType(int num, Class<?> nptype);

	native public MethodType insertParameterTypes(int num, Class<?>... ptypesToInsert);

	native public MethodType appendParameterTypes(Class<?>... ptypesToInsert);

	native public MethodType insertParameterTypes(int num, List<Class<?>> ptypesToInsert);

	native public MethodType appendParameterTypes(List<Class<?>> ptypesToInsert);

	native public MethodType dropParameterTypes(int start, int end);

	native public MethodType changeReturnType(Class<?> nrtype);

	native public boolean hasPrimitives();

	native public boolean hasWrappers();

	native public MethodType erase();

	native public MethodType generic();

	native public MethodType wrap();

	native public MethodType unwrap();

	native public Class<?> parameterType(int num);

	native public int parameterCount();

	native public Class<?> returnType();

	native public List<Class<?>> parameterList();

	native public Class<?>[] parameterArray();

	native public static MethodType fromMethodDescriptorString(String descriptor, ClassLoader loader) throws IllegalArgumentException, TypeNotPresentException;

	native public String toMethodDescriptorString();

	native public boolean equals(Object x);

	native public int hashCode();

	native public String toString();
}
