package java.lang.invoke;

public class MutableCallSite extends CallSite {
	public MutableCallSite(MethodType type) {
		super(type);
	}

	public MutableCallSite(MethodHandle target) {
		super(target);
	}

	native public final MethodHandle getTarget();

	native public void setTarget(MethodHandle newTarget);

	native public final MethodHandle dynamicInvoker();

	native public static void syncAll(MutableCallSite[] sites);
}
