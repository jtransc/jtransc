package java.lang.invoke;

import java.lang.reflect.*;
import java.util.List;

public class MethodHandles {
	private MethodHandles() {
	}

	native public static Lookup lookup();

	native public static Lookup publicLookup();

	native public static <T extends Member> T reflectAs(Class<T> expected, MethodHandle target);

	native public static MethodHandle arrayElementGetter(Class<?> arrayClass) throws IllegalArgumentException;

	native public static MethodHandle arrayElementSetter(Class<?> arrayClass) throws IllegalArgumentException;

	native static public MethodHandle spreadInvoker(MethodType type, int leadingArgCount);

	native static public MethodHandle exactInvoker(MethodType type);

	native static public MethodHandle invoker(MethodType type);

	native public static MethodHandle explicitCastArguments(MethodHandle target, MethodType newType);

	native public static MethodHandle permuteArguments(MethodHandle target, MethodType newType, int... reorder);

	native public static MethodHandle constant(Class<?> type, Object value);

	native public static MethodHandle identity(Class<?> type);

	native public static MethodHandle insertArguments(MethodHandle target, int pos, Object... values);

	native public static MethodHandle dropArguments(MethodHandle target, int pos, List<Class<?>> valueTypes);

	native public static MethodHandle dropArguments(MethodHandle target, int pos, Class<?>... valueTypes);

	native public static MethodHandle filterArguments(MethodHandle target, int pos, MethodHandle... filters);

	native public static MethodHandle collectArguments(MethodHandle target, int pos, MethodHandle filter);

	native public static MethodHandle filterReturnValue(MethodHandle target, MethodHandle filter);

	native public static MethodHandle foldArguments(MethodHandle target, MethodHandle combiner);

	native public static MethodHandle guardWithTest(MethodHandle test, MethodHandle target, MethodHandle fallback);

	native public static MethodHandle catchException(MethodHandle target, Class<? extends Throwable> exType, MethodHandle handler);

	native public static MethodHandle throwException(Class<?> returnType, Class<? extends Throwable> exType);

	public static final class Lookup {
		public static final int PUBLIC = Modifier.PUBLIC;
		public static final int PRIVATE = Modifier.PRIVATE;
		public static final int PROTECTED = Modifier.PROTECTED;
		public static final int PACKAGE = Modifier.STATIC;

		native public Class<?> lookupClass();

		native public int lookupModes();

		native public Lookup in(Class<?> requestedLookupClass);

		native public String toString();

		native public MethodHandle findStatic(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException;

		native public MethodHandle findVirtual(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException;

		native public MethodHandle findConstructor(Class<?> refc, MethodType type) throws NoSuchMethodException, IllegalAccessException;

		native public MethodHandle findSpecial(Class<?> refc, String name, MethodType type, Class<?> specialCaller) throws NoSuchMethodException, IllegalAccessException;

		native public MethodHandle findGetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException;

		native public MethodHandle findSetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException;

		native public MethodHandle findStaticGetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException;

		native public MethodHandle findStaticSetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException;

		native public MethodHandle bind(Object receiver, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException;

		native public MethodHandle unreflect(Method m) throws IllegalAccessException;

		native public MethodHandle unreflectSpecial(Method m, Class<?> specialCaller) throws IllegalAccessException;

		native public MethodHandle unreflectConstructor(Constructor<?> c) throws IllegalAccessException;

		native public MethodHandle unreflectGetter(Field f) throws IllegalAccessException;

		native public MethodHandle unreflectSetter(Field f) throws IllegalAccessException;

		native public MethodHandleInfo revealDirect(MethodHandle target);
	}
}
