package java.lang.invoke;

import java.io.Serializable;

public final class SerializedLambda implements Serializable {
	public SerializedLambda(
		Class<?> capturingClass,
		String functionalInterfaceClass,
		String functionalInterfaceMethodName,
		String functionalInterfaceMethodSignature,
		int implMethodKind,
		String implClass,
		String implMethodName,
		String implMethodSignature,
		String instantiatedMethodType,
		Object[] capturedArgs
	) {
	}

	native public String getCapturingClass();

	native public String getFunctionalInterfaceClass();

	native public String getFunctionalInterfaceMethodName();

	native public String getFunctionalInterfaceMethodSignature();

	native public String getImplClass();

	native public String getImplMethodName();

	native public String getImplMethodSignature();

	native public int getImplMethodKind();

	native public final String getInstantiatedMethodType();

	native public int getCapturedArgCount();

	native public Object getCapturedArg(int i);

	native public String toString();
}
