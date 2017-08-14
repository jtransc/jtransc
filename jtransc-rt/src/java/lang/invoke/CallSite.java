package java.lang.invoke;

abstract
public class CallSite {
	MethodHandle target;    // Note: This field is known to the JVM.  Do not change.

	CallSite(MethodType type) {
	}

	CallSite(MethodHandle target) {
	}

	CallSite(MethodType targetType, MethodHandle createTargetHook) throws Throwable {
	}

	native public MethodType type();

	public abstract MethodHandle getTarget();

	public abstract void setTarget(MethodHandle newTarget);

	public abstract MethodHandle dynamicInvoker();
}
