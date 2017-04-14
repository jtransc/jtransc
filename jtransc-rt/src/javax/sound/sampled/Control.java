package javax.sound.sampled;

@SuppressWarnings("WeakerAccess")
public abstract class Control {
	private final Type type;

	protected Control(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public static class Type {
		private String name;

		protected Type(String name) {
			this.name = name;
		}

		public final String toString() {
			return name;
		}
	}
}