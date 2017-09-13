package java.lang.invoke;

public class VolatileCallSite extends CallSite {
	public VolatileCallSite(MethodType type) {
		super(type);
	}

	public VolatileCallSite(MethodHandle target) {
		super(target);
	}

	native public final MethodHandle getTarget();

	native public void setTarget(MethodHandle newTarget);

	native public final MethodHandle dynamicInvoker();
}
