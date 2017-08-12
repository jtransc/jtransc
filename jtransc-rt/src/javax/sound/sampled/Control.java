package javax.sound.sampled;

public abstract class Control {
	private final Type type;

	protected Control(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public String toString() {
		return getType() + " Control";
	}

	public static class Type {
		private final String name;

		protected Type(String name) {
			this.name = name;
		}

		public final boolean equals(Object obj) {
			return obj == this;
		}

		public final int hashCode() {
			return name.hashCode();
		}

		public final String toString() {
			return name;
		}
	}
}
