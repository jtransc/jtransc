package java.lang.invoke;

public class ConstantCallSite extends CallSite {
	public ConstantCallSite(MethodHandle target) {
		super(target);
	}

	protected ConstantCallSite(MethodType targetType, MethodHandle createTargetHook) throws Throwable {
		super(targetType, createTargetHook);
	}

	native public final MethodHandle getTarget();

	native public final void setTarget(MethodHandle ignore);

	native public final MethodHandle dynamicInvoker();
}
