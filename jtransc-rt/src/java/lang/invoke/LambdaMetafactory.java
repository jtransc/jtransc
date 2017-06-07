package java.lang.invoke;

public class LambdaMetafactory {
	public static final int FLAG_SERIALIZABLE = 1;
	public static final int FLAG_MARKERS = 2;
	public static final int FLAG_BRIDGES = 4;

	public LambdaMetafactory() {
	}

	native public static CallSite metafactory(
		MethodHandles.Lookup caller,
		String invokedName,
		MethodType invokedType,
		MethodType samMethodType,
		MethodHandle implMethod,
		MethodType instantiatedMethodType
	);

	native public static CallSite altMetafactory(MethodHandles.Lookup var0, String var1, MethodType var2, Object... var3) throws LambdaConversionException;
}
