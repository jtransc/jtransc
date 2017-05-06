package java.lang.reflect;

public class Parameter {
	private final MethodConstructor mc;
	private final int index;

	Parameter(MethodConstructor mc, int index) {
		this.mc = mc;
		this.index = index;
	}

	public boolean isNamePresent() {
		return false;
	}

	public Class<?> getType() {
		return mc.getParameterTypes()[index];
	}
}
