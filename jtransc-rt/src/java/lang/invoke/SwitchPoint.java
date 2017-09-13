package java.lang.invoke;

public class SwitchPoint {
	public SwitchPoint() {
	}

	native public boolean hasBeenInvalidated();

	native public MethodHandle guardWithTest(MethodHandle target, MethodHandle fallback);

	native public static void invalidateAll(SwitchPoint[] switchPoints);
}
