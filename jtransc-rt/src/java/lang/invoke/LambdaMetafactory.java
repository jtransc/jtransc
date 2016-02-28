package java.lang.invoke;

public class LambdaMetafactory {
	native public static CallSite metafactory(
		MethodHandles.Lookup caller,
		String invokedName,
		MethodType invokedType,
		MethodType samMethodType,
		MethodHandle implMethod,
		MethodType instantiatedMethodType
	);
}
