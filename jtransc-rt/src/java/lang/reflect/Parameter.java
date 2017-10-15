package java.lang.reflect;

import com.jtransc.annotation.JTranscSync;

public class Parameter {
	private final MethodConstructor mc;
	private final int index;

	@JTranscSync
	Parameter(MethodConstructor mc, int index) {
		this.mc = mc;
		this.index = index;
	}

	@JTranscSync
	public boolean isNamePresent() {
		return false;
	}

	@JTranscSync
	public Class<?> getType() {
		return mc.getParameterTypes()[index];
	}
}
