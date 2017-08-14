package java.lang.invoke;

public class MethodHandleProxies {

	private MethodHandleProxies() {
	}

	native public static <T> T asInterfaceInstance(final Class<T> intfc, final MethodHandle target);

	native public static boolean isWrapperInstance(Object x);

	native public static MethodHandle wrapperInstanceTarget(Object x);

	native public static Class<?> wrapperInstanceType(Object x);

}
