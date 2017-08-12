package javax.sound.sampled;

public class LineEvent extends java.util.EventObject {
	public LineEvent(Line line, Type type, long position) {
		super(line);
		throw new RuntimeException("Not implemented");
	}

	public final Line getLine() {
		return (Line) getSource();
	}

	native public final Type getType();

	native public final long getFramePosition();

	native public String toString();

	public static class Type {
		private final String name;

		protected Type(String name) {
			this.name = name;
		}

		native public final boolean equals(Object obj);

		native public final int hashCode();

		native public String toString();

		public static final Type OPEN = new Type("Open");
		public static final Type CLOSE = new Type("Close");
		public static final Type START = new Type("Start");
		public static final Type STOP = new Type("Stop");
	}
}
